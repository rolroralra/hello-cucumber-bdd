package com.example.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Cucumber + Spring Boot 통합 설정 클래스.
 *
 * <p>역할:
 * <ul>
 *   <li>{@code @CucumberContextConfiguration}: Cucumber가 이 클래스를 Spring 컨텍스트 설정으로 인식</li>
 *   <li>{@code @SpringBootTest}: 실제 서버를 랜덤 포트로 구동하여 E2E 테스트 수행</li>
 *   <li>{@code @DynamicPropertySource}: Spring 컨텍스트 시작 전에
 *       Testcontainers PostgreSQL 접속 정보를 주입</li>
 * </ul>
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    /**
     * 테스트 전체에서 한 번만 시작되는 PostgreSQL 컨테이너.
     * {@code static}으로 선언하여 모든 시나리오가 동일한 컨테이너를 재사용합니다.
     *
     * <p>{@code @Testcontainers}/{@code @Container}는 JUnit Jupiter 확장이라 Cucumber 엔진에서는
     * 동작하지 않으므로, static 블록에서 직접 시작합니다.
     */
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("cucumber_test_db")
                    .withUsername("test_user")
                    .withPassword("test_pass");

    static {
        // Spring 컨텍스트 로드 전에 컨테이너를 미리 시작
        POSTGRES.start();
    }

    /**
     * Spring 환경(Environment)에 Testcontainers 접속 정보를 주입합니다.
     *
     * <p>{@code @DynamicPropertySource}로 등록한 프로퍼티는 가장 높은 우선순위를 가지므로
     * application.yml의 하드코딩 값보다 우선 적용됩니다.
     * cucumber-spring이 이 클래스를 Spring TestContext의 테스트 클래스로 사용하기 때문에
     * Cucumber 환경에서도 동작합니다.
     */
    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
