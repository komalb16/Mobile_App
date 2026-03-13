package com.demo;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

@RestController
@RequestMapping("/api/deploy")
@CrossOrigin(origins = "*")
public class DeployController {

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("/release")
    public ResponseEntity<Map> createRelease(
            @RequestHeader("X-GH-Token") String token,
            @RequestHeader("X-GH-Repo")  String repo,
            @RequestBody Map<String, String> body) {
        try {
            HttpHeaders headers = githubHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> payload = Map.of(
                "tag_name",   body.getOrDefault("tag", "deploy-" + System.currentTimeMillis()),
                "name",       body.getOrDefault("name", "Deploy"),
                "body",       "Uploaded via PWA Dashboard",
                "draft",      false,
                "prerelease", false
            );
            ResponseEntity<Map> res = restTemplate.exchange(
                "https://api.github.com/repos/" + repo + "/releases",
                HttpMethod.POST, new HttpEntity<>(payload, headers), Map.class);
            return ResponseEntity.ok(res.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<Map> uploadAsset(
            @RequestHeader("X-GH-Token")   String token,
            @RequestHeader("X-Upload-Url") String uploadUrl,
            @RequestParam("file")          MultipartFile file) {
        try {
            HttpHeaders headers = githubHeaders(token);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            ResponseEntity<Map> res = restTemplate.exchange(
                uploadUrl + "?name=app.jar",
                HttpMethod.POST, new HttpEntity<>(file.getBytes(), headers), Map.class);
            return ResponseEntity.ok(res.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/activate")
    public ResponseEntity<Map> activateJar(
            @RequestHeader("X-GH-Token") String token,
            @RequestHeader("X-GH-Repo")  String repo,
            @RequestBody Map<String, String> body) {
        try {
            String downloadUrl = body.get("downloadUrl");
            String label       = body.getOrDefault("label", "unknown");
            if (downloadUrl == null || downloadUrl.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "downloadUrl required"));

            String fileUrl = "https://api.github.com/repos/" + repo + "/contents/active-jar.txt";
            HttpHeaders headers = githubHeaders(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            String sha = null;
            try {
                ResponseEntity<Map> existing = restTemplate.exchange(
                    fileUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                sha = (String) existing.getBody().get("sha");
            } catch (Exception ignored) {}

            String content = Base64.getEncoder().encodeToString(downloadUrl.getBytes());
            Map<String, Object> payload = sha != null
                ? Map.of("message", "Activate: " + label, "content", content, "sha", sha)
                : Map.of("message", "Activate: " + label, "content", content);

            restTemplate.exchange(fileUrl, HttpMethod.PUT, new HttpEntity<>(payload, headers), Map.class);
            return ResponseEntity.ok(Map.of("status", "activated", "label", label));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/trigger")
    public ResponseEntity<Map> triggerDeploy(@RequestBody Map<String, String> body) {
        try {
            String hook = body.get("hook");
            if (hook == null || hook.isBlank())
                return ResponseEntity.badRequest().body(Map.of("error", "No hook URL"));
            restTemplate.postForEntity(hook, null, String.class);
            return ResponseEntity.ok(Map.of("status", "triggered"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("status", "triggered", "note", e.getMessage()));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<Map> getActive(
            @RequestHeader("X-GH-Token") String token,
            @RequestHeader("X-GH-Repo")  String repo) {
        try {
            HttpHeaders headers = githubHeaders(token);
            ResponseEntity<Map> res = restTemplate.exchange(
                "https://api.github.com/repos/" + repo + "/contents/active-jar.txt",
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            String encoded = (String) res.getBody().get("content");
            String url = new String(Base64.getDecoder().decode(encoded.replaceAll("\\s", "")));
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("url", "none"));
        }
    }

    private HttpHeaders githubHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.set("Authorization", "token " + token);
        h.set("Accept", "application/vnd.github+json");
        return h;
    }
}
