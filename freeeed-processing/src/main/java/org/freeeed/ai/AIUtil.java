package org.freeeed.ai;

import okhttp3.*;
import org.freeeed.services.Settings;
import org.freeeed.ui.ProjectUI;
import org.freeeed.util.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.util.Enumeration;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class AIUtil {
    private final static java.util.logging.Logger LOGGER = LogFactory.getLogger(ProjectUI.class.getName());
    public String removeBreakingCharacters(String str){
        // TODO it looks strange to replace and reassign
        str = str.replaceAll("[^\\p{ASCII}]", "");
        str = Normalizer.normalize(str, Normalizer.Form.NFKC);
        str = str.replaceAll("[\\n\\t ]", " ");

        ByteBuffer buffer = StandardCharsets.UTF_8.encode(str);

        str = StandardCharsets.UTF_8.decode(buffer).toString();
        str = str.replace("\"", "");
        str = str.replace("\'", "");
        str =  str.replace("\\", "");

        str = str.trim();

        return str;
    }
    public void putAllIntoPinecone(String namespace, String processedResultsZipFile) {
        LOGGER.info("putIntoPinecone: namespace = " + namespace + ", processedResultsZipFile = " + processedResultsZipFile);
        indexFilesInZip(namespace, processedResultsZipFile);
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
                    putIntoPinecone(namespace, content);
                }
            }
        } catch (IOException e) {
            System.out.println("Error opening zip file" + e);
        }
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
        String answer = "AI sez " + question;
        Settings settings = Settings.getSettings();

        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES) // Set connect timeout
                    .readTimeout(3, TimeUnit.MINUTES) // Set read timeout
                    .build();

            // Prepare the URL and query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "question_case/").newBuilder();
            urlBuilder.addQueryParameter("question", question);
            urlBuilder.addQueryParameter("case_id", "31");
            String url = urlBuilder.build().toString();

            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            // Execute the request and handle the response
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                // Process the response body
                answer = response.body().string();
            }
        } catch (Exception e) {
            LOGGER.severe("Error asking AI");
            e.printStackTrace(System.out);
        }
        return answer;
    }
    public void putIntoPinecone(String namespace, String content) {
        Settings settings = Settings.getSettings();
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(3, TimeUnit.MINUTES) // Set connect timeout
                    .readTimeout(3, TimeUnit.MINUTES) // Set read timeout
                    .build();

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            String bodyContent = "case_id=" + "your_case_id" + "&content=" + content;
            RequestBody body = RequestBody.create(bodyContent, mediaType);
            // Prepare the URL and query parameters
            HttpUrl.Builder urlBuilder = HttpUrl.parse(settings.getAiEndpoint() + "store_content/").newBuilder();
            String url = urlBuilder.build().toString();
            // Build the request
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            // Execute the request and handle the response
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                // Process the response body
                response.body().string();
            }
        } catch (Exception e) {
            LOGGER.severe("Error adding to Pinecone");
            e.printStackTrace(System.out);
        }
    }

}
