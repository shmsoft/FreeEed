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
        LOGGER.info("[DEBUG] indexFilesInZip START - namespace=" + namespace + ", zipFile=" + zipFile + ", startEntry=" + startEntry + ", howManyEntries=" + howManyEntries);
        int count = 0;
        int batchSize = 10;
        try {
            LOGGER.info("[DEBUG] Opening zip file: " + zipFile);
            ZipFile zf = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            LOGGER.info("[DEBUG] Zip file opened successfully");

            List<String> contents = new ArrayList<>();
            List<String> sourceDocs = new ArrayList<>();

            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                String zipEntryName = ze.getName();
                if (zipEntryName.startsWith("text/")) {
                    ++count;
                    if (count >= startEntry && count < startEntry + howManyEntries) {
                        LOGGER.info("[DEBUG] Processing entry #" + count + ": " + zipEntryName);
                        String content = readTextFromZipEntry(zipFile, zipEntryName);
                        String sourceDoc = zipEntryName.substring(5, 14);
                        contents.add(content);
                        sourceDocs.add(sourceDoc);

                        if (contents.size() == batchSize){
                            LOGGER.info("[DEBUG] Batch ready, calling indexDocuments with " + batchSize + " documents");
                            indexDocuments(namespace, contents, sourceDocs);
                            LOGGER.info("[DEBUG] indexDocuments returned for batch ending at entry #" + count);
                            contents = new ArrayList<>();
                            sourceDocs = new ArrayList<>();
                        }
                    }
                } else if (count >= startEntry + howManyEntries) {
                    LOGGER.info("[DEBUG] Reached howManyEntries limit, breaking loop at count=" + count);
                    break;
                }
            }

            if(!contents.isEmpty()) {
                LOGGER.info("[DEBUG] Final batch: calling indexDocuments with " + contents.size() + " remaining documents");
                indexDocuments(namespace, contents, sourceDocs);
                LOGGER.info("[DEBUG] Final indexDocuments call returned");
            }
            LOGGER.info("[DEBUG] indexFilesInZip completed successfully");
        } catch (IOException e) {
            LOGGER.severe("[DEBUG] Error opening zip file: " + e.getMessage());
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
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "advisors/retrieval/question_case/").newBuilder();
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
        LOGGER.info("[DEBUG] indexDocuments START - namespace=" + namespace + ", contents.size=" + contents.size() + ", fileNames.size=" + fileNames.size());
        if (contents.isEmpty()) {
            LOGGER.info("[DEBUG] indexDocuments - contents is empty, returning early");
            return;
        }
        Settings settings = Settings.getSettings();
        try {
            OkHttpClient client = LONG_INDEXING_CLIENT;
            LOGGER.info("[DEBUG] indexDocuments - using LONG_INDEXING_CLIENT with 30-minute timeout");

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

            String endpoint = settings.getAiEndpoint() + "advisors/retrieval/store_contents/";
            LOGGER.info("[DEBUG] indexDocuments - endpoint: " + endpoint);
            HttpUrl url = HttpUrl.parse(endpoint);
            if (url == null) {
                throw new IOException("Invalid AI endpoint/url: " + settings.getAiEndpoint());
            }

            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();

            LOGGER.info("[DEBUG] indexDocuments - sending HTTP POST request now...");
            long startTime = System.currentTimeMillis();
            try (Response response = client.newCall(request).execute()) {
                long elapsed = System.currentTimeMillis() - startTime;
                LOGGER.info("[DEBUG] indexDocuments - HTTP response received after " + elapsed + "ms, status=" + response.code());
                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    throw new IOException("store_contents failed: HTTP " + response.code() + " " + response.message() + ": " + errBody);
                }
                // Drain body to close the response cleanly.
                if (response.body() != null) {
                    String respBody = response.body().string();
                    LOGGER.info("[DEBUG] indexDocuments - response body length: " + respBody.length());
                }
            }
            LOGGER.info("[DEBUG] indexDocuments - completed successfully");
        } catch (Exception e) {
            LOGGER.severe("[DEBUG] Error indexing document: " + e.getMessage());
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
        LOGGER.info("String namespace, String zipFile: " + namespace + ", " + zipFile);
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

            long startTime = System.currentTimeMillis();

            LOGGER.info("indexIntoAiDB: preparing multipart request");
            LOGGER.info("indexIntoAiDB: namespace(case_id)=" + namespace);

            if (zip != null) {
                LOGGER.info("indexIntoAiDB: zip file name=" + zip.getName());
                LOGGER.info("indexIntoAiDB: zip file path=" + zip.getAbsolutePath());
                LOGGER.info("indexIntoAiDB: zip exists=" + zip.exists());
                LOGGER.info("indexIntoAiDB: zip size(bytes)=" + zip.length());
            } else {
                LOGGER.warning("indexIntoAiDB: zip file is NULL");
            }

            MultipartBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("case_id", namespace)
                    .addFormDataPart("zip_file", zip.getName(), zipBody)
                    .build();

            HttpUrl url = HttpUrl.parse(endpoint + "advisors/retrieval/store_zip_texts/");
            LOGGER.info("indexIntoAiDB: resolved endpoint=" + endpoint);
            LOGGER.info("indexIntoAiDB: full URL=" + (url != null ? url.toString() : "NULL"));

            if (url == null) {
                LOGGER.warning("indexIntoAiDB: invalid AI endpoint/url: " + endpoint);
                return 0;
            }

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            LOGGER.info("indexIntoAiDB: executing POST request...");

            try (Response response = client.newCall(request).execute()) {

                long duration = System.currentTimeMillis() - startTime;
                LOGGER.info("indexIntoAiDB: HTTP response received in " + duration + " ms");
                LOGGER.info("indexIntoAiDB: HTTP status=" + response.code() + " " + response.message());

                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "";
                    LOGGER.severe("indexIntoAiDB: FAILED. Response body: " + errBody);
                    throw new IOException("store_zip_texts failed: HTTP "
                            + response.code() + " "
                            + response.message() + ": "
                            + errBody);
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                LOGGER.info("indexIntoAiDB: SUCCESS. Response body length="
                        + responseBody.length());

                LOGGER.fine("indexIntoAiDB: Response body content=" + responseBody);

                int filesIndexed = parseFilesIndexed(responseBody);
                LOGGER.info("indexIntoAiDB: files indexed=" + filesIndexed);

                return filesIndexed;
            }
            catch (Exception e) {
                long duration = System.currentTimeMillis() - startTime;
                LOGGER.severe("indexIntoAiDB: exception after " + duration + " ms");
                LOGGER.severe("indexIntoAiDB: error=" + e.getMessage());
                throw e;
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
