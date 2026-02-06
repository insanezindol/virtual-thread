package com.example.virtualthread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class VirtualThreadApplication {

    public static void main(String[] args) {
        log.info("가상 스레드 데모 애플리케이션 시작...");

        // 가상 스레드 팩토리 설정
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "100");
        System.setProperty("jdk.virtualThreadScheduler.parallelism", "100");
        System.setProperty("jdk.virtualThreadScheduler.maxPoolSize", "1000");

        // 가상 스레드 이름 패턴 설정
        System.setProperty("jdk.virtualThreadScheduler.threadFactory.virtual.thread-prefix", "virtual-");

        SpringApplication.run(VirtualThreadApplication.class, args);

        log.info("Java 21 가상 스레드 지원: {}", Runtime.version().feature() >= 21 ? "Yes" : "No");
    }

}
