package com.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/deploy")
@CrossOrigin(origins = "*")
public class DeployController {

    private final RestTemplate restTemplate = new RestTemplate();

    // ── Create GitHub Release ──
    @PostMapping("/release")
    public ResponseEntity<Map> createRelease(
            @RequestHeader("X-GH-Token") String token,
            @RequestHeader("X-GH-Repo")  String repo,
            @RequestBody Map<String, String> body) {
        try {
            String url = "https://api.github.com/repos/" + repo + "/releases";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + token);
            headers.set("Accept", "application/vnd.github+json");
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> payload = Map.of(
                "tag_name",   body.getOrDefault("tag", "deploy-" + System.currentTimeMillis()),
                "name",       body.getOrDefault("name", "Deploy"),
                "body",       "Uploaded via PWA Dashboard",
                "draft",      false,
                "prerelease", false
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Upload JAR asset to release ──
    @PostMapping("/upload")
    public ResponseEntity<Map> uploadAsset(
            @RequestHeader("X-GH-Token")      String token,
            @RequestHeader("X-Upload-Url")    String uploadUrl,
            @RequestParam("file")             MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "token " + token);
            headers.set("Accept", "application/vnd.github+json");
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

            HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);
            String url = uploadUrl + "?name=app.jar";
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // ── Trigger Render deploy hook ──
    @PostMapping("/trigger")
    public ResponseEntity<Map> triggerDeploy(
            @RequestBody Map<String, String> body) {
        try {
            String hook = body.get("hook");
            if (hook == null || hook.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hook URL provided"));
            }
            restTemplate.postForEntity(hook, null, String.class);
            return ResponseEntity.ok(Map.of("status", "triggered"));
        } catch (Exception e) {
            // Render hooks sometimes return non-200 but still work
            return ResponseEntity.ok(Map.of("status", "triggered", "note", e.getMessage()));
        }
    }
}
