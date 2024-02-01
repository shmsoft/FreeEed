package org.freeeed.ai;

import okhttp3.*;
import org.freeeed.db.DbLocalUtils;
import org.freeeed.services.Project;
import org.freeeed.services.Settings;
import org.freeeed.services.UtilJson;
import org.freeeed.ui.ProjectUI;
import org.freeeed.util.LogFactory;
import org.freeeed.util.ZipCounter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AIUtil {
    // Create a ConnectionPool instance
    static int maxIdleConnections = 1; // Maximum idle connections
    static long keepAliveDuration = 1; // Keep alive duration
    static TimeUnit timeUnit = TimeUnit.MINUTES; // Time unit for keep alive duration

    //static ConnectionPool connectionPool = new ConnectionPool(maxIdleConnections, keepAliveDuration, timeUnit);

    //OkHttpClient client = new OkHttpClient.Builder().connectionPool(connectionPool).build();

    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ProjectUI.class.getName());

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

    public void putAllIntoPinecone(String namespace, String processedResultsZipFile) {
        LOGGER.info("putIntoPinecone: namespace = " + namespace + ", processedResultsZipFile = " + processedResultsZipFile);
        cleanCaseIndex(namespace);
        indexFilesInZip(namespace, processedResultsZipFile);
    }

    public int preparePutInPinecone(String namespace, String processedResultsZipFile) {
        LOGGER.info("preparePutInPinecone: namespace = " + namespace + ", processedResultsZipFile = " + processedResultsZipFile);
        cleanCaseIndex(namespace);
        return new ZipCounter().numberElementsInZip(processedResultsZipFile);
    }

    public void indexFilesInZip(String namespace, String zipFile) {
        try {
            ZipFile zf = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                String zipEntryName = ze.getName();
                if (zipEntryName.startsWith("text/")) {
                    LOGGER.fine(zipEntryName);
                    String content = readTextFromZipEntry(zipFile, zipEntryName);
                    putIntoPinecone(namespace, content, "unknown");
                }
            }
        } catch (IOException e) {
            System.out.println("Error opening zip file" + e);
        }
    }

    public void indexFilesInZip(String namespace, String zipFile, int startEntry, int howManyEntries) {
        int count = 0;
        try {
            ZipFile zf = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = zf.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) entries.nextElement();
                String zipEntryName = ze.getName();
                if (zipEntryName.startsWith("text/")) {
                    ++count;
                    if (count >= startEntry && count < startEntry + howManyEntries) {
                        LOGGER.fine("Sending to Pinecone " + zipEntryName);
                        String content = readTextFromZipEntry(zipFile, zipEntryName);
                        String sourceDoc = zipEntryName.substring(5, 14);
                        putIntoPinecone(namespace, content, sourceDoc);
                    }
                } else if (count >= startEntry + howManyEntries) {
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("Error opening zip file" + e);
        }
    }

    public ArrayList<String> findPiiInZip(String zipFile) {
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
        if (Project.getCurrentProject().getProjectList().length > 0) {
            Project currentProject = Project.getCurrentProject();
            try {
                projectsList = DbLocalUtils.getProjects();
                LOGGER.info("I will ask about " + projectsList.size() + " projects");
                for (Map.Entry<Integer, Project> entry : projectsList.entrySet()) {
                    Project project = entry.getValue();
                    Project.setCurrentProject(project);
                    askOnce(question, answer);
                    LOGGER.info("Project in list " + project.getProjectName());
                }
            } catch (Exception e) {
                LOGGER.severe("Error getting projects");
                return answer.toString();
            }
            Project.setCurrentProject(currentProject);
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

    public void putIntoPinecone(String namespace, String content, String sourceDoc) {
        Settings settings = Settings.getSettings();
        if (content.isEmpty()) {
            return;
        }
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES) // Set connect timeout
                    .readTimeout(3, TimeUnit.MINUTES) // Set read timeout
                    .build();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String bodyContent = "case_id=" + namespace + "&content=" + content + "&source=" + sourceDoc;
            RequestBody body = RequestBody.create(bodyContent, mediaType);
            // Prepare the URL and query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "store_content/").newBuilder();
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
                assert response.body() != null;
                response.body().string();
            }
        } catch (Exception e) {
            LOGGER.severe("Error adding to Pinecone");
            e.printStackTrace(System.out);
        }
    }

    public void cleanCaseIndex(String namespace) {
        LOGGER.info("cleanCaseIndex: namespace = " + namespace);
        Settings settings = Settings.getSettings();
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES) // Set connect timeout
                    .readTimeout(3, TimeUnit.MINUTES) // Set read timeout
                    .build();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String bodyContent = "case_id=" + namespace;
            RequestBody body = RequestBody.create(bodyContent, mediaType);
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
}
