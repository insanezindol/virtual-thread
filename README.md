![header](https://capsule-render.vercel.app/api?type=wave&color=auto&height=300&section=header&text=tickets%20queue&fontSize=90)

# Java 21 Virtual Thread Project

이 프로젝트는 Java 21의 핵심 기능인 Virtual Thread(가상 스레드)를 Spring Boot 환경에서 활용하는 방법을 보여주는 예제 애플리케이션입니다.

## 프로젝트 소개

기존의 Java 스레드(Platform Thread)는 OS 스레드와 1:1로 매핑되어 생성 비용이 비싸고 개수에 제한이 있었습니다.
Java 21에서 정식 도입된 Virtual Thread는 JVM 내부에서 관리되는 경량 스레드로, 수백만 개의 스레드를 동시에 생성하여 처리할 수 있게 해줍니다.

이 프로젝트는 다음과 같은 Virtual Thread의 특징들을 테스트하고 확인할 수 있습니다:
- Spring Boot 3.5.10 환경에서의 Virtual Thread 설정 방법
- Tomcat 웹 서버의 Virtual Thread 적용
- 동기 vs 비동기 처리 성능 비교
- 대량의 동시 요청 처리 효율성

## 기술 스택

- **Java**: JDK 21
- **Framework**: Spring Boot 3.5.10
- **Build Tool**: Gradle
- **Dependencies**:
  - Spring Web
  - Lombok

## 설정 방법 (Virtual Thread 활성화)

Spring Boot에서 Virtual Thread를 활성화하기 위해 `WebConfig.java`에 다음과 같은 설정을 적용했습니다:

1. **Tomcat Protocol Handler**: Tomcat이 요청을 처리할 때 Virtual Thread를 사용하도록 설정
2. **Async Task Executor**: `@Async` 어노테이션이 붙은 비동기 작업이 Virtual Thread에서 실행되도록 설정

```java
// WebConfig.java 예시
@Bean
public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
    return protocolHandler -> {
        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    };
}
```

## 테스트 API 가이드

애플리케이션 실행 후 다음 엔드포인트를 통해 Virtual Thread의 동작을 확인할 수 있습니다.

### 1. 동기 작업 테스트
일반적인 블로킹 작업을 수행합니다.
- **URL**: `GET /api/v1/tasks/sync?delay=100`
- **설명**: 100ms 지연 후 응답

### 2. 비동기 작업 테스트 (Virtual Thread)
`@Async`를 사용하여 Virtual Thread에서 비동기로 작업을 수행합니다.
- **URL**: `GET /api/v1/tasks/async?delay=100`
- **설명**: Virtual Thread를 사용하여 논블로킹 방식으로 처리

### 3. 병렬 처리 테스트
여러 개의 작업을 동시에 Virtual Thread로 처리합니다.
- **URL**: `GET /api/v1/tasks/parallel?count=10&delay=100`
- **설명**: 10개의 작업을 병렬로 처리. Platform Thread라면 스레드 풀 제한에 걸릴 수 있지만, Virtual Thread는 효율적으로 처리합니다.

### 4. 직접 Virtual Thread 생성
`Thread.ofVirtual()` API를 사용하여 직접 가상 스레드를 생성하는 예제입니다.
- **URL**: `GET /api/v1/tasks/virtual-threads?count=20`

### 5. 성능 벤치마크 (동기 vs 비동기)
동기 처리와 Virtual Thread를 이용한 비동기 처리의 성능을 비교합니다.
- **URL**: `GET /api/v1/tasks/benchmark?taskCount=50&delay=100`
- **응답 예시**: 처리 시간 단축 및 처리량(Throughput) 증가 확인 가능

### 6. 스레드 상태 확인
현재 실행 중인 스레드가 Virtual Thread인지 확인합니다.
- **URL**: `GET /api/v1/tasks/threads`

## 주요 특징 및 이점

1. **높은 처리량 (High Throughput)**
   - I/O 블로킹이 발생하는 작업(DB 조회, API 호출 등)에서 스레드를 차단하지 않고 다른 작업을 처리할 수 있어 리소스 활용도가 매우 높습니다.

2. **간결한 코드 (Code Simplicity)**
   - 복잡한 Reactive Programming(WebFlux) 없이도 기존의 동기식 코드 스타일(Imperative Style)을 유지하면서 비동기 처리의 이점을 누릴 수 있습니다.

3. **가벼운 리소스 (Lightweight)**
   - 기존 스레드 대비 메모리 사용량이 현저히 적어, 수만~수백만 개의 스레드 생성이 가능합니다.

## 실행 방법

1. Java 21 이상이 설치되어 있어야 합니다.
2. 프로젝트 루트에서 다음 명령어로 실행합니다.

```bash
./gradlew bootRun
```

## 참고 사항

- `VirtualThreadApplication.java`에서 시스템 프로퍼티로 스케줄러 설정을 조정할 수 있습니다.
- 로그를 통해 실행되는 스레드가 `VirtualThread`인지 확인할 수 있습니다. (예: `VirtualThread[#21]/runnable@ForkJoinPool-1-worker-1`)
