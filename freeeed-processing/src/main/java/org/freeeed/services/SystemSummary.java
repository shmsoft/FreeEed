package org.freeeed.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.freeeed.util.OsUtil;

import java.util.ArrayList;
import java.util.List;

public class SystemSummary {
    public static ArrayList<String> getSystemSummary() {
        ArrayList <String> systemReport = new ArrayList <String>();
        String systemCheckErrors = OsUtil.systemCheck();
        systemReport.add(systemCheckErrors);
        List<String> systemSummary = OsUtil.getSystemSummary();
        systemReport.addAll(systemSummary);
        List<String> serviceSummary = OsUtil.getServiceSummary();
        systemReport.addAll(serviceSummary);
        systemReport.add("FreeEed Review: " + reviewAlive());
        return systemReport;
    }
    public static String reviewAlive() {
        String answer = "Not available";
        try {
            OkHttpClient client = new OkHttpClient();
            String url = Settings.getSettings().getReviewEndpoint();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                answer = response.body().string();
                if (!answer.isEmpty()) {
                    answer = "Active at " + url;
                }
            }
        } catch (Exception e) {
            answer = "Error contacting FreeEed Review: " + e.getMessage();
        }
        return answer;
    }
}
