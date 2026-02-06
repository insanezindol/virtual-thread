package com.example.virtualthread.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableAsync
public class WebConfig {

    /**
     * Tomcat이 가상 스레드를 사용하도록 설정
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        log.info("Tomcat 가상 스레드 Executor 설정 활성화");
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            log.info("Tomcat이 가상 스레드 Executor로 설정됨");
        };
    }

    /**
     * Spring MVC Async 작업을 위한 가상 스레드 Executor 설정
     */
    @Bean
    public AsyncTaskExecutor applicationTaskExecutor() {
        log.info("Spring MVC Async Task Executor를 가상 스레드로 설정");
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    /**
     * 일반적인 비동기 작업을 위한 가상 스레드 Executor
     */
    @Bean(name = "virtualThreadExecutor")
    public AsyncTaskExecutor virtualThreadExecutor() {
        log.info("가상 스레드 Executor 생성");
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }
}
