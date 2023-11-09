package org.freeeed.api.tika;

import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Tika-server REST API implementation
 * https://cwiki.apache.org/confluence/display/TIKA/TikaServer
 */

public class RestApiTika {
    OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public static final MediaType MEDIA_TYPE_BINARY
            = MediaType.parse("application/octet-stream");

    static String TIKA_URL = "http://localhost:9998/tika";
    static String METADATA_URL = "http://localhost:9998/meta";
    static String LANGUAGE_URL = "http://localhost:9998/language/string";

    /**
     * curl -X GET http://localhost:9998/tika
     */
    public String helloTika() throws Exception {
        String url = "http://localhost:9998/tika";
        Request request = new Request.Builder()
                .url(url)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    String postTo(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    String getLanguage(String json) throws IOException {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(LANGUAGE_URL)
                .put(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    public HashMap<String, String> getMetadata(File file) throws IOException, CsvValidationException {
        HashMap<String, String> output;
        RequestBody requestBody = RequestBody.create(file, MEDIA_TYPE_BINARY);
        Request request = new Request.Builder()
                .url(METADATA_URL)
                .put(requestBody)
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            output = convertToHashTable(response.body().string());
        }
        return output;
    }


    String putString(String url, String phrase) throws IOException {
        RequestBody body = RequestBody.create(phrase, JSON);
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

    /**
     * Converts CSV string to Hashtable of key-value pairs for metadata fields
     *
     * @param csvContent
     * @return
     * @throws CsvValidationException
     * @throws IOException
     */
    private HashMap<String, String> convertToHashTable(String csvContent) throws CsvValidationException, IOException {
        HashMap<String, String> metadata = new HashMap<>();
        try (CSVReader csvReader = new CSVReader(new StringReader(csvContent))) {
            String[] nextLine;
            while ((nextLine = csvReader.readNext()) != null) {
                if (nextLine.length >= 2) {
                    String key = nextLine[0].trim();
                    String value = nextLine[1].trim();
                    metadata.put(key, value);
                }
            }
        }
        return metadata;
    }

    public String getText(File file) throws Exception {
        String output = "";
        Request request = new Request.Builder()
                .url(TIKA_URL)
                .put(RequestBody.create(file, MEDIA_TYPE_BINARY))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            output = response.body().string();
        }
        return output;
    }
}
