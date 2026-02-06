package com.example.virtualthread.controller;

import com.example.virtualthread.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    /**
     * 동기 처리 테스트 엔드포인트
     */
    @GetMapping("/sync")
    public ResponseEntity<String> testSync(@RequestParam(defaultValue = "100") int delay) {
        log.info("동기 작업 요청 - 지연: {}ms", delay);

        LocalDateTime start = LocalDateTime.now();
        String result = taskService.processSyncTask(1, delay);
        Duration duration = Duration.between(start, LocalDateTime.now());

        return ResponseEntity.ok(String.format("%s (소요시간: %dms)", result, duration.toMillis()));
    }

    /**
     * 단일 비동기 작업 테스트
     */
    @GetMapping("/async")
    public ResponseEntity<CompletableFuture<String>> testAsync(@RequestParam(defaultValue = "100") int delay) {
        log.info("비동기 작업 요청 - 지연: {}ms", delay);

        CompletableFuture<String> future = taskService.processAsyncTask(1, delay);
        return ResponseEntity.ok(future);
    }

    /**
     * 병렬 비동기 작업 테스트
     */
    @GetMapping("/parallel")
    public ResponseEntity<Map<String, Object>> testParallel(
            @RequestParam(defaultValue = "10") int count,
            @RequestParam(defaultValue = "100") int delay) {
        log.info("병렬 작업 요청 - 개수: {}, 지연: {}ms", count, delay);

        LocalDateTime start = LocalDateTime.now();
        List<String> results = taskService.processParallelTasks(count, delay);
        Duration duration = Duration.between(start, LocalDateTime.now());

        Map<String, Object> response = Map.of(
                "totalTasks", count,
                "delayPerTask", delay,
                "totalTime", duration.toMillis() + "ms",
                "results", results,
                "efficiency", String.format("%.2f%%",
                        (double) (count * delay) / duration.toMillis() * 100)
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 직접 가상 스레드 생성 테스트
     */
    @GetMapping("/virtual-threads")
    public ResponseEntity<Map<String, Object>> testVirtualThreads(
            @RequestParam(defaultValue = "20") int count,
            @RequestParam(defaultValue = "50") int delay) {
        log.info("직접 가상 스레드 생성 요청 - 개수: {}", count);

        LocalDateTime start = LocalDateTime.now();
        List<String> results = taskService.createVirtualThreadsDirectly(count, delay);
        Duration duration = Duration.between(start, LocalDateTime.now());

        Map<String, Object> response = Map.of(
                "virtualThreadsCreated", count,
                "delayPerTask", delay,
                "totalTime", duration.toMillis() + "ms",
                "results", results
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 복잡한 블로킹 작업 테스트
     */
    @GetMapping("/complex")
    public ResponseEntity<CompletableFuture<String>> testComplexOperation(
            @RequestParam(defaultValue = "1") int taskId) {
        log.info("복잡한 블로킹 작업 요청 - 작업ID: {}", taskId);

        return ResponseEntity.ok(taskService.complexBlockingOperation(taskId));
    }

    /**
     * 현재 활성화된 스레드 작업 정보 조회
     */
    @GetMapping("/threads")
    public ResponseEntity<Map<String, Object>> getActiveThreads() {
        ConcurrentHashMap<Long, String> activeTasks = taskService.getActiveThreadTasks();

        Map<String, Object> response = Map.of(
                "activeThreadCount", activeTasks.size(),
                "threadTasks", activeTasks,
                "currentPlatformThread", Thread.currentThread().toString(),
                "isVirtual", Thread.currentThread().isVirtual()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 성능 비교 테스트 - 동기 vs 비동기
     */
    @GetMapping("/benchmark")
    public ResponseEntity<Map<String, Object>> benchmark(
            @RequestParam(defaultValue = "50") int taskCount,
            @RequestParam(defaultValue = "100") int delay) {

        log.info("성능 벤치마크 시작 - 작업수: {}, 지연: {}ms", taskCount, delay);

        // 동기 처리 측정
        LocalDateTime syncStart = LocalDateTime.now();
        for (int i = 0; i < taskCount; i++) {
            taskService.processSyncTask(i + 1000, delay);
        }
        Duration syncDuration = Duration.between(syncStart, LocalDateTime.now());

        // 비동기 처리 측정
        LocalDateTime asyncStart = LocalDateTime.now();
        taskService.processParallelTasks(taskCount, delay);
        Duration asyncDuration = Duration.between(asyncStart, LocalDateTime.now());

        Map<String, Object> response = Map.of(
                "testConfig", Map.of(
                        "taskCount", taskCount,
                        "delayPerTask", delay
                ),
                "syncProcessing", Map.of(
                        "totalTime", syncDuration.toMillis() + "ms",
                        "throughput", String.format("%.2f tasks/sec",
                                (double) taskCount / syncDuration.toMillis() * 1000)
                ),
                "asyncProcessing", Map.of(
                        "totalTime", asyncDuration.toMillis() + "ms",
                        "throughput", String.format("%.2f tasks/sec",
                                (double) taskCount / asyncDuration.toMillis() * 1000)
                ),
                "improvement", Map.of(
                        "timeReduction", String.format("%.2f%%",
                                (double) (syncDuration.toMillis() - asyncDuration.toMillis()) /
                                        syncDuration.toMillis() * 100),
                        "throughputIncrease", String.format("%.2fx",
                                (double) syncDuration.toMillis() / asyncDuration.toMillis())
                )
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Health Check 엔드포인트
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "javaVersion", System.getProperty("java.version"),
                "virtualThreadsSupported", true,
                "timestamp", LocalDateTime.now().toString()
        ));
    }
}
