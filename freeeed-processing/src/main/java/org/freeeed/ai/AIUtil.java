package org.freeeed.ai;

import okhttp3.*;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.UtilJson;
import org.freeeed.ui.ProjectUI;
import org.freeeed.util.LogFactory;
import org.freeeed.util.ZipCounter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AIUtil {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ProjectUI.class.getName());

    // Indexing can legitimately take minutes. Use a dedicated long-timeout client.
    // OkHttpClient is thread-safe and intended to be reused.
    private static final OkHttpClient LONG_INDEXING_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.MINUTES)
            .callTimeout(30, TimeUnit.MINUTES)
            .build();

    public String removeBreakingCharacters(String str) {
        // TODO it looks strange to replace and reassign
        str = str.replaceAll("[^\\p{ASCII}]", "");
        str = Normalizer.normalize(str, Normalizer.Form.NFKC);
        str = str.replaceAll("[\\n\\t ]", " ");

        ByteBuffer buffer = StandardCharsets.UTF_8.encode(str);

        str = StandardCharsets.UTF_8.decode(buffer).toString();
        str = str.replace("\"", "");
        str = str.replace("\'", "");
        str = str.replace("\\", "");

        str = str.trim();

        return str;
    }

    public int preparePutInPinecone(String namespace, String processedResultsZipFile) {
        LOGGER.info("preparePutInPinecone: namespace = " + namespace + ", processedResultsZipFile = " + processedResultsZipFile);
        deleteIndex(namespace);
        return new ZipCounter().numberElementsInZip(processedResultsZipFile);
    }

    public Map<String, String> fetchAnswersFromAzureOpenAI(String zipFileName, int startEntry, int howManyEntries, List<String> questions) {
        int count = 0;
        Map<String, String> answers = new HashMap<>();
        try {
            ZipFile zf = new ZipFile(zipFileName);
            Enumeration<? extends ZipEntry> entries = zf.entries();

            while (entries.hasMoreElements() && count < (startEntry + howManyEntries)) {
                ZipEntry ze = entries.nextElement();
                String zipEntryName = ze.getName();
                if (zipEntryName.startsWith("text/")) {
                    ++count;
                    if (count >= startEntry) {
                        String content = readTextFromZipEntry(zipFileName, zipEntryName);
                        answers.put(zipEntryName.substring(5), OpenAiAzureClient.sendContentAndQuestionToAzureOpenAI(content, questions));
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error opening zip file" + e);
        }
        return answers;
    }

    public void indexFilesInZip(String namespace, String zipFile, int startEntry, int howManyEntries) {
        int count = 0;
        int batchSize = 10;
        try {
            ZipFile zf = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zf.entries();

            List<String> contents = new ArrayList<>();
            List<String> sourceDocs = new ArrayList<>();

            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                String zipEntryName = ze.getName();
                if (zipEntryName.startsWith("text/")) {
                    ++count;
                    if (count >= startEntry && count < startEntry + howManyEntries) {
                        LOGGER.fine("Sending to Pinecone " + zipEntryName);
                        String content = readTextFromZipEntry(zipFile, zipEntryName);
                        String sourceDoc = zipEntryName.substring(5, 14);
                        contents.add(content);
                        sourceDocs.add(sourceDoc);

                        if (contents.size() == batchSize){
                            indexDocuments(namespace, contents, sourceDocs);
                            contents = new ArrayList<>();
                            sourceDocs = new ArrayList<>();
                        }
                    }
                } else if (count >= startEntry + howManyEntries) {
                    break;
                }
            }

            if(!contents.isEmpty()) {
                indexDocuments(namespace, contents, sourceDocs);
            }
        } catch (IOException e) {
            System.out.println("Error opening zip file" + e);
        }
    }

    public ArrayList<String> findPiiInZip(String zipFile) {
        int counter = 0;
        ArrayList<String> piiList = new ArrayList<>();
        try {
            ZipFile zf = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                String zipEntryName = ze.getName();
                if (zipEntryName.startsWith("text/")) {
                    LOGGER.fine("Finding PII in " + zipEntryName);
                    String content = readTextFromZipEntry(zipFile, zipEntryName);
                    String sourceDoc = zipEntryName.substring(5, 14);
                    String pii = findPii(content);
                    piiList.add(sourceDoc + ": " + pii);
                    if (++counter > 100) {
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error opening zip file" + e);
        }
        return piiList;
    }

    private String readTextFromZipEntry(String zipFilePath, String entryName) throws IOException {
        try (ZipFile zipFile = new ZipFile(zipFilePath)) {
            ZipEntry entry = zipFile.getEntry(entryName);
            if (entry == null) {
                throw new IOException("Entry not found in the zip file");
            }
            return readContent(zipFile.getInputStream(entry));
        }
    }

    private String readContent(java.io.InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public String askAI(String question) {
        StringBuilder answer = new StringBuilder();
        // We only create a project list if that is requested
        Map<Integer, Project> projectsList = null;
        if (Project.getCurrentProject().isMultProject()) {
            askOnce(question, answer);
        } else {
            askOnce(question, answer);
        }
        return answer.toString();
    }

    private void askOnce(String question, StringBuilder wisdomAccumulator) {
        Settings settings = Settings.getSettings();
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    .build();
            // Prepare the URL and query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "question_case/").newBuilder();
            String aiIndexName = Project.getCurrentProject().getAiNamespace();
            urlBuilder.addQueryParameter("case_id", aiIndexName);
            urlBuilder.addQueryParameter("question", question);
            String url = urlBuilder.build().toString();
            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            // Execute the request and handle the response
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Process the response body
                String nuggetOfWisdom = response.body().string();
                UtilJson utilJson = new UtilJson();
                utilJson.parseJson(nuggetOfWisdom);
                Project project = Project.getCurrentProject();
                wisdomAccumulator.append(project.getProjectCode()).append(": ")
                        .append(project.getProjectName()).append("\n")
                        .append(utilJson.getAnswer()).append("\n")
                        .append(utilJson.getSourcesAsString()).append("\n");
            }
        } catch (Exception e) {
            LOGGER.severe("Error asking AI");
            e.printStackTrace(System.out);
        }
    }

    public String findPii(String text) {
        Settings settings = Settings.getSettings();
        StringBuilder answer = new StringBuilder();
        if (text.isBlank()) {
            return "";
        }
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .callTimeout(120, TimeUnit.SECONDS)
                    .build();
            // Prepare the URL and query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "find_pii/").newBuilder();
            urlBuilder.addQueryParameter("text", text);
            String url = urlBuilder.build().toString();
            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            // Execute the request and handle the response
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                String pii = response.body().string();
                answer.append(pii);
            } catch (Exception e) {
                LOGGER.severe("Error asking AI");
                e.printStackTrace(System.out);
            }
        } catch (Exception e) {
            LOGGER.severe("Error asking AI");
            e.printStackTrace(System.out);
        }
        return answer.toString();
    }

    public void indexDocuments(String namespace, List<String> contents, List<String> fileNames) {
        if (contents.isEmpty()) {
            return;
        }
        Settings settings = Settings.getSettings();
        try {
            OkHttpClient client = LONG_INDEXING_CLIENT;

            // IMPORTANT: contents can include '&', '=', unicode, etc.
            // Build a real application/x-www-form-urlencoded body so the server can parse it reliably.
            FormBody.Builder form = new FormBody.Builder(StandardCharsets.UTF_8)
                    .add("case_id", namespace);
            for (String content : contents) {
                form.add("content", content);
            }
            for (String fileName : fileNames) {
                form.add("source", fileName);
            }

            RequestBody body = form.build();

            HttpUrl url = HttpUrl.parse(settings.getAiEndpoint() + "store_contents/");
            if (url == null) {
                throw new IOException("Invalid AI endpoint/url: " + settings.getAiEndpoint());
            }

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    throw new IOException("store_contents failed: HTTP " + response.code() + " " + response.message() + ": " + errBody);
                }
                // Drain body to close the response cleanly.
                if (response.body() != null) {
                    response.body().string();
                }
            }
        } catch (Exception e) {
            LOGGER.severe("Error indexing document: " + e.getMessage());
            e.printStackTrace(System.out);
        }
    }

    public void deleteIndex(String namespace) {
        LOGGER.info("deleteIndex: namespace = " + namespace);
        Settings settings = Settings.getSettings();
        try {
            OkHttpClient client = LONG_INDEXING_CLIENT;
            String bodyContent = "case_id=" + namespace;
            RequestBody body = RequestBody.create(bodyContent, MediaType.get("text/plain; charset=utf-8"));
            // Prepare the URL and query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "clean_case_index/?" + bodyContent).newBuilder();
            String url = urlBuilder.build().toString();
            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            // Execute the request and handle the response
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                // Process the response body
                response.body().string();
            }
        } catch (Exception e) {
            LOGGER.severe("Error cleaning the namespace in Pinecone");
            e.printStackTrace(System.out);
        }
    }

    public int indexIntoAiDB(String namespace, String zipFile) {
        if (namespace == null || namespace.isBlank()) {
            LOGGER.warning("indexIntoAiDB: namespace is blank");
            return 0;
        }
        if (zipFile == null || zipFile.isBlank()) {
            LOGGER.warning("indexIntoAiDB: zipFile is blank");
            return 0;
        }

        File zip = new File(zipFile);
        if (!zip.exists() || !zip.isFile()) {
            LOGGER.warning("indexIntoAiDB: zip file not found: " + zip.getAbsolutePath());
            return 0;
        }

        Settings settings = Settings.getSettings();
        String endpoint = settings.getAiEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            LOGGER.warning("indexIntoAiDB: AI endpoint is not configured");
            return 0;
        }

        // API: POST /store_zip_texts/ (multipart/form-data)
        // form fields: case_id (string), zip_file (binary)
        try {
            OkHttpClient client = LONG_INDEXING_CLIENT;

            MediaType zipMediaType = MediaType.parse("application/zip");
            RequestBody zipBody = RequestBody.create(zip, zipMediaType);

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("case_id", namespace)
                    .addFormDataPart("zip_file", zip.getName(), zipBody)
                    .build();

            HttpUrl url = HttpUrl.parse(endpoint + "store_zip_texts/");
            if (url == null) {
                LOGGER.warning("indexIntoAiDB: invalid AI endpoint/url: " + endpoint);
                return 0;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    throw new IOException("store_zip_texts failed: HTTP " + response.code() + " " + response.message() + ": " + errBody);
                }
                String responseBody = response.body() != null ? response.body().string() : "";
                return parseFilesIndexed(responseBody);
            }
        } catch (Exception e) {
            LOGGER.severe("Error indexing ZIP into AI DB: " + e.getMessage());
            e.printStackTrace(System.out);
            return 0;
        }
    }

    private int parseFilesIndexed(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return 0;
        }

        // We keep parsing lightweight here to avoid new dependencies.
        // Expected: {"files_indexed": <int>, ...}
        try {
            String key = "\"files_indexed\"";
            int keyPos = responseBody.indexOf(key);
            if (keyPos < 0) {
                return 0;
            }
            int colonPos = responseBody.indexOf(':', keyPos + key.length());
            if (colonPos < 0) {
                return 0;
            }

            int i = colonPos + 1;
            while (i < responseBody.length() && Character.isWhitespace(responseBody.charAt(i))) {
                i++;
            }
            int start = i;
            while (i < responseBody.length() && (Character.isDigit(responseBody.charAt(i)) || responseBody.charAt(i) == '-')) {
                i++;
            }
            if (start == i) {
                return 0;
            }
            return Integer.parseInt(responseBody.substring(start, i));
        } catch (Exception ignored) {
            // Fall back to 0 if response is unexpected
            return 0;
        }
    }
}
