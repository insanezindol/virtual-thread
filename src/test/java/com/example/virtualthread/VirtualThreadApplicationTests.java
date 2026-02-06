package com.example.virtualthread;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VirtualThreadApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void syncEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/sync?delay=100";
        String response = restTemplate.getForObject(url, String.class);

        assertThat(response).contains("동기 작업");
        assertThat(response).contains("완료");
    }

    @Test
    void asyncEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/async?delay=100";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("done")).isEqualTo(false);
        assertThat(response.get("cancelled")).isEqualTo(false);
        assertThat(response.get("completedExceptionally")).isEqualTo(false);
    }

    @Test
    void parallelEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/parallel?count=5&delay=10";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("delayPerTask")).isEqualTo(10);
        assertThat(response.get("totalTasks")).isEqualTo(5);
    }

    @Test
    void virtualThreadsEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/virtual-threads?count=10&delay=100";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("virtualThreadsCreated")).isEqualTo(10);
        assertThat(response.get("delayPerTask")).isEqualTo(100);
    }

    @Test
    void complexEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/complex";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("done")).isEqualTo(false);
        assertThat(response.get("cancelled")).isEqualTo(false);
        assertThat(response.get("completedExceptionally")).isEqualTo(false);
    }

    @Test
    void threadsEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/threads";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("isVirtual")).isEqualTo(true);
    }

    @Test
    void benchmarkEndpointShouldWork() {
        String url = "http://localhost:" + port + "/api/v1/tasks/benchmark?taskCount=5&delay=10";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("improvement")).isNotNull();
        assertThat(response.get("asyncProcessing")).isNotNull();
        assertThat(response.get("syncProcessing")).isNotNull();
        assertThat(response.get("testConfig")).isNotNull();
    }

    @Test
    void healthEndpointShouldReturnUp() {
        String url = "http://localhost:" + port + "/api/v1/tasks/health";
        var response = restTemplate.getForObject(url, Map.class);

        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("UP");
        assertThat(response.get("virtualThreadsSupported")).isEqualTo(true);
    }

}
