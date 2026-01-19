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
        String detectedOs = OsUtil.getOs().toString();
        systemReport.add("OS: " + detectedOs);
        String edition = Settings.getSettings().getEditionSelected();
        systemReport.add("Edition: " + edition);
        String systemCheckErrors = OsUtil.systemCheck();
        systemReport.add("System check errors: " + systemCheckErrors);
        systemReport.add(systemCheckErrors);
        List<String> systemSummary = OsUtil.getSystemSummary();
        systemReport.addAll(systemSummary);
        List<String> serviceSummary = OsUtil.getServiceSummary();
        systemReport.addAll(serviceSummary);
        systemReport.add("FreeEed Review: " + reviewAlive());
        systemReport.add("AI Advisor: " + aiAlive());
        return systemReport;
    }

    public static String serverStatus(String serverUrl) {
        String answer = "Not available";
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(serverUrl)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                answer = response.body().string();
                if (!answer.isEmpty()) {
                    answer = "Active at " + serverUrl;
                }
            }
        } catch (Exception e) {
            answer = e.getMessage();
        }
        return answer;
    }
    public static String reviewAlive() {
        return serverStatus(Settings.getSettings().getReviewEndpoint());
    }
    public static String aiAlive() {
        return serverStatus(Settings.getSettings().getAiEndpoint());
    }

}
