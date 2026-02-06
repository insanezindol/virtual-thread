package com.example.virtualthread.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Slf4j
@Service
public class TaskService {

    private final AtomicInteger taskCounter = new AtomicInteger(0);
    private final ConcurrentHashMap<Long, String> threadTasks = new ConcurrentHashMap<>();

    /**
     * 동기 처리 - 블로킹 I/O 작업 시뮬레이션
     */
    public String processSyncTask(int taskId, int delayMillis) {
        String threadName = Thread.currentThread().toString();
        log.info("[동기 작업 {}] 시작 - 스레드: {}", taskId, threadName);

        threadTasks.put(Thread.currentThread().threadId(), "Task-" + taskId + " - " + threadName);

        try {
            // 블로킹 작업 시뮬레이션 (DB 조회, API 호출 등)
            Thread.sleep(delayMillis);

            String result = String.format("동기 작업 %d 완료 (지연: %dms) - 스레드: %s", taskId, delayMillis, threadName);

            log.info("[동기 작업 {}] 완료", taskId);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("작업 중단됨", e);
        }
    }

    /**
     * 비동기 처리 - 가상 스레드 사용
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<String> processAsyncTask(int taskId, int delayMillis) {
        String threadName = Thread.currentThread().toString();
        log.info("[비동기 작업 {}] 시작 - 스레드: {}", taskId, threadName);

        threadTasks.put(Thread.currentThread().threadId(), "Async-Task-" + taskId + " - " + threadName);

        try {
            // 블로킹 작업 시뮬레이션
            Thread.sleep(delayMillis);

            String result = String.format("비동기 작업 %d 완료 (지연: %dms) - 스레드: %s", taskId, delayMillis, threadName);

            log.info("[비동기 작업 {}] 완료", taskId);
            return CompletableFuture.completedFuture(result);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 병렬 처리 - 여러 가상 스레드로 동시 실행
     */
    public List<String> processParallelTasks(int count, int delayMillis) {
        log.info("병렬 작업 {}개 시작 (각각 지연: {}ms)", count, delayMillis);

        LocalDateTime startTime = LocalDateTime.now();

        List<CompletableFuture<String>> futures = IntStream.range(0, count)
                .mapToObj(i -> {
                    int taskId = taskCounter.incrementAndGet();
                    return processAsyncTask(taskId, delayMillis);
                })
                .toList();

        // 모든 작업 완료 대기
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        List<String> results = allFutures.thenApply(v ->
                futures.stream()
                        .map(CompletableFuture::join)
                        .toList()
        ).join();

        Duration duration = Duration.between(startTime, LocalDateTime.now());
        log.info("병렬 작업 {}개 완료 (소요시간: {}ms)", count, duration.toMillis());

        return results;
    }

    /**
     * 가상 스레드 직접 생성 예제
     */
    public List<String> createVirtualThreadsDirectly(int count, int delayMillis) {
        log.info("직접 가상 스레드 {}개 생성", count);

        List<Thread> threads = new ArrayList<>();
        List<String> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int taskId = i + 1;
            Thread thread = Thread.ofVirtual()
                    .name("direct-virtual-thread-" + taskId)
                    .start(() -> {
                        String threadName = Thread.currentThread().toString();
                        log.info("[직접 생성 가상 스레드 {}] 시작", taskId);

                        try {
                            Thread.sleep(delayMillis);
                            String result = String.format("직접 생성 작업 %d 완료 - %s",
                                    taskId, threadName);
                            synchronized (results) {
                                results.add(result);
                            }
                            log.info("[직접 생성 가상 스레드 {}] 완료", taskId);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
            threads.add(thread);
        }

        // 모든 스레드 완료 대기
        threads.forEach(thread -> {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        return results;
    }

    /**
     * 현재 활성화된 스레드 정보 조회
     */
    public ConcurrentHashMap<Long, String> getActiveThreadTasks() {
        return threadTasks;
    }

    /**
     * 블로킹 I/O 작업을 포함한 복잡한 작업 처리
     */
    @Async("virtualThreadExecutor")
    public CompletableFuture<String> complexBlockingOperation(int taskId) {
        log.info("[복잡한 작업 {}] 시작", taskId);

        // 여러 블로킹 작업을 순차적으로 실행
        String result1 = simulateDatabaseQuery(taskId);
        String result2 = simulateExternalApiCall(taskId);
        String result3 = simulateFileOperation(taskId);

        String finalResult = String.format("작업 %d 결과: %s | %s | %s",
                taskId, result1, result2, result3);
        log.info("[복잡한 작업 {}] 종료 - {}", taskId, finalResult);

        return CompletableFuture.completedFuture(finalResult);
    }

    private String simulateDatabaseQuery(int taskId) {
        try {
            log.info("DB 쿼리 지연 시뮬레이션 {}", taskId);
            Thread.sleep(1000); // DB 쿼리 지연 시뮬레이션
            return "DB-Query-Success";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "DB-Query-Error";
        }
    }

    private String simulateExternalApiCall(int taskId) {
        try {
            log.info("외부 API 호출 지연 시뮬레이션{}", taskId);
            Thread.sleep(2000); // 외부 API 호출 지연 시뮬레이션
            return "API-Call-Success";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "API-Call-Error";
        }
    }

    private String simulateFileOperation(int taskId) {
        try {
            log.info("파일 작업 지연 시뮬레이션 {}", taskId);
            Thread.sleep(500); // 파일 작업 지연 시뮬레이션
            return "File-Op-Success";
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "File-Op-Error";
        }
    }
}
