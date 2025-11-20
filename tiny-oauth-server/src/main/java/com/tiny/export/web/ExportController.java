package com.tiny.export.web;

import com.tiny.export.core.ExportRequest;
import com.tiny.export.persistence.ExportTaskEntity;
import com.tiny.export.service.ExportService;
import com.tiny.export.service.ExportTaskService;
import com.tiny.export.service.ExportTaskStatus;
import com.tiny.oauthserver.sys.model.SecurityUser;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    private final ExportService exportService;
    private final ExportTaskService exportTaskService;

    public ExportController(ExportService exportService, ExportTaskService exportTaskService) {
        this.exportService = exportService;
        this.exportTaskService = exportTaskService;
    }

    /** 同步导出（阻塞） */
    @PostMapping("/sync")
    public void exportSync(@RequestBody ExportRequest request,
                           HttpServletResponse response) throws Exception {
        String filename = (request.getFileName() == null || request.getFileName().isBlank())
            ? "export.xlsx" : request.getFileName() + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        try (OutputStream out = response.getOutputStream()) {
            exportService.exportSync(request, out, currentUserId());
        }
    }

    /** 异步任务提交 */
    @PostMapping("/async")
    public ResponseEntity<Map<String, String>> submitAsync(@RequestBody ExportRequest request) {
        String uid = currentUserId();
        String taskId = exportService.submitAsync(request, uid);
        return ResponseEntity.accepted()
            .location(URI.create("/api/export/task/" + taskId))
            .body(Map.of("taskId", taskId));
    }

    /** 查询任务状态 */
    @GetMapping("/task/{taskId}")
    public ResponseEntity<?> getTask(@PathVariable String taskId) {
        Optional<ExportTaskEntity> task = exportTaskService.findByTaskId(taskId);
        return task.<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 下载异步结果 */
    @GetMapping("/task/{taskId}/download")
    public void downloadTaskResult(@PathVariable String taskId,
                                   HttpServletResponse response) throws Exception {
        Optional<ExportTaskEntity> taskOpt = exportTaskService.findByTaskId(taskId);
        if (taskOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        ExportTaskEntity task = taskOpt.get();
        Authentication auth = currentAuthentication();
        String requester = currentUserId(auth);
        if (!requester.equals(task.getUserId()) && !hasAdminAuthority(auth)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (task.getStatus() != ExportTaskStatus.SUCCESS) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            response.getWriter().write("Task status: " + task.getStatus());
            return;
        }
        if (task.getFilePath() == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path file = Path.of(task.getFilePath());
        if (!Files.exists(file)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");
        try (InputStream is = Files.newInputStream(file); OutputStream os = response.getOutputStream()) {
            is.transferTo(os);
            os.flush();
        }
    }

    /** Servlet 异步写回（短任务演示） */
    @PostMapping("/async-servlet")
    public void exportAsyncServlet(@RequestBody ExportRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        String uid = currentUserId();
        AsyncContext async = httpRequest.startAsync();
        async.setTimeout(60_000L);
        async.start(() -> {
            try (OutputStream os = response.getOutputStream()) {
                response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                response.setHeader("Content-Disposition", "attachment; filename=\"export-asyncservlet.xlsx\"");
                exportService.exportSync(request, os, uid);
                os.flush();
            } catch (Exception ex) {
                try {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("Export failed: " + ex.getMessage());
                    response.getWriter().flush();
                } catch (Exception ignored) {
                }
            } finally {
                async.complete();
            }
        });
    }

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String currentUserId() {
        return currentUserId(currentAuthentication());
    }

    private String currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "anonymous";
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof SecurityUser securityUser && securityUser.getUserId() != null) {
            return String.valueOf(securityUser.getUserId());
        }
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }
        String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return "anonymous";
        }
        return name;
    }

    private boolean hasAdminAuthority(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(auth -> "ROLE_ADMIN".equalsIgnoreCase(auth) || "ADMIN".equalsIgnoreCase(auth));
    }
}
