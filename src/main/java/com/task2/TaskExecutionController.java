package com.task2;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodStatus;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.util.ClientBuilder;

@RestController
@RequestMapping("/tasks")
public class TaskExecutionController {

    public static class TaskExecutionRequest {
        // Accept either a String ("echo hi") or an array ( ["echo","hi"] )
        public Object command;

        public Object getCommand() { return command; }
        public void setCommand(Object command) { this.command = command; }
    }

    public static class TaskExecutionResult {
        public String podName;
        public String status;
        public String logs;

        public TaskExecutionResult(String podName, String status, String logs) {
            this.podName = podName;
            this.status = status;
            this.logs = logs;
        }

        public String getPodName() { return podName; }
        public String getStatus() { return status; }
        public String getLogs() { return logs; }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> runTask(@PathVariable String id, @RequestBody TaskExecutionRequest req) {
        if (req == null || req.command == null) {
            return ResponseEntity.badRequest().body("Missing 'command' in request body");
        }

        String namespace = System.getenv().getOrDefault("K8S_NAMESPACE", "default");
        String podName = "task-exec-" + id + "-" + UUID.randomUUID().toString().substring(0,6);

        try {
            ApiClient client = ClientBuilder.standard().build();
            io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
            CoreV1Api api = new CoreV1Api(client);

            // Build pod manifest using busybox
        // Build container command: support both string and array inputs
        java.util.List<String> containerCommand;
        if (req.command instanceof java.util.List) {
        // assume array of strings
        containerCommand = new java.util.ArrayList<>();
        for (Object o : (java.util.List<?>) req.command) {
            containerCommand.add(String.valueOf(o));
        }
        } else {
        // treat as single shell command
        String cmd = String.valueOf(req.command);
        containerCommand = java.util.List.of("sh", "-c", cmd);
        }

        V1Pod pod = new V1Pod()
            .metadata(new V1ObjectMeta().name(podName).putLabelsItem("app", "task-exec"))
            .spec(new V1PodSpec()
                .containers(java.util.List.of(new V1Container()
                    .name("task")
                    .image("busybox:1.36")
                    .command(containerCommand)
                ))
                .restartPolicy("Never")
            );

            api.createNamespacedPod(namespace, pod, null, null, null, null);

            // Wait for completion
            V1PodStatus status = null;
            int attempts = 0;
            int maxAttempts = 120; // wait up to ~2 minutes
            while (attempts < maxAttempts) {
                Thread.sleep(1000);
                V1Pod current = api.readNamespacedPod(podName, namespace, null);
                status = current.getStatus();
                String phase = status != null && status.getPhase() != null ? status.getPhase() : "";
                if ("Succeeded".equalsIgnoreCase(phase) || "Failed".equalsIgnoreCase(phase)) {
                    break;
                }
                attempts++;
            }

            String phase = status != null && status.getPhase() != null ? status.getPhase() : "Unknown";

            // Do not call readNamespacedPodLog here because client method signatures vary between versions.
            // Users can fetch logs via kubectl: kubectl logs -n <ns> <podName>

            // Delete pod (cleanup)
            try {
                api.deleteNamespacedPod(podName, namespace, null, null, null, null, null, (V1DeleteOptions) null);
            } catch (Exception e) {
                // ignore cleanup failures
            }

            TaskExecutionResult result = new TaskExecutionResult(podName, phase, null);
            return ResponseEntity.ok(result);

        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Kubernetes API error: " + e.getResponseBody());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Interrupted while waiting for pod");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}
