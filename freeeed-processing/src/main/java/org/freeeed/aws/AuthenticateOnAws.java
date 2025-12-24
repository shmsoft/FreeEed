package org.freeeed.aws;

import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class AuthenticateOnAws {

    private static final String BASE_URL =
            "https://zood0g66rg.execute-api.us-west-2.amazonaws.com/prod";

    // If your API Gateway stage requires an API key, set this (or leave null).
    // The runbook shows CORS allows X-Api-Key. :contentReference[oaicite:4]{index=4}
    private static final String API_KEY = System.getenv("SCAIA_API_KEY");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .build();

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java AuthenticateOnAws <emailOrUsername> <password>");
                System.exit(2);
        }
        Tokens tokens = login(args[0], args[1]);

        // Example protected call (replace path with what you need)
        // Many core APIs require projectId now. :contentReference[oaicite:5]{index=5}
        String mePerms = get("/users/me/permissions", tokens);
        System.out.println("GET /users/me/permissions => " + mePerms);
    }

    public static Tokens login(String emailOrUsername, String password)
            throws IOException, InterruptedException {

        // NOTE: If your /auth/login expects {"username": "..."} instead of {"email": "..."},
        // just change the key below.
        Map<String, Object> body = new HashMap<>();
        body.put("email", emailOrUsername);
        body.put("password", password);

        String requestJson = GSON.toJson(body);

        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .timeout(Duration.ofSeconds(30))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestJson));

        if (API_KEY != null && !API_KEY.isBlank()) {
            req.header("x-api-key", API_KEY);
        }

        HttpResponse<String> resp = HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("Login failed HTTP " + resp.statusCode() + ": " + resp.body());
        }

        TokenResponse tr = GSON.fromJson(resp.body(), TokenResponse.class);

        if (tr.idToken == null || tr.idToken.isBlank()) {
            throw new RuntimeException("Login response missing idToken. Raw: " + resp.body());
        }

        return new Tokens(tr.idToken, tr.accessToken, tr.refreshToken);
    }

    public static String get(String path, Tokens tokens) throws IOException, InterruptedException {
        HttpRequest.Builder req = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + tokens.idToken) // required per runbook :contentReference[oaicite:6]{index=6}
                .GET();

        if (API_KEY != null && !API_KEY.isBlank()) {
            req.header("x-api-key", API_KEY);
        }

        HttpResponse<String> resp = HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("GET " + path + " failed HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return resp.body();
    }

    public static final class Tokens {
        public final String idToken;
        public final String accessToken;
        public final String refreshToken;

        public Tokens(String idToken, String accessToken, String refreshToken) {
            this.idToken = idToken;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    public static final class TokenResponse {
        public String idToken;
        public String accessToken;
        public String refreshToken;
    }
}

