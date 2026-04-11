package com.example.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Cucumber + Spring Boot 통합 설정 클래스.
 *
 * <p>역할:
 * <ul>
 *   <li>{@code @CucumberContextConfiguration}: Cucumber가 이 클래스를 Spring 컨텍스트 설정으로 인식</li>
 *   <li>{@code @SpringBootTest}: 실제 서버를 랜덤 포트로 구동하여 E2E 테스트 수행</li>
 *   <li>{@code @ContextConfiguration(initializers)}: Spring 컨텍스트 시작 전에
 *       Testcontainers PostgreSQL URL을 주입 — {@code System.setProperty()} 보다 안정적</li>
 * </ul>
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = CucumberSpringConfiguration.ContainerInitializer.class)
public class CucumberSpringConfiguration {

    /**
     * 테스트 전체에서 한 번만 시작되는 PostgreSQL 컨테이너.
     * {@code static}으로 선언하여 모든 시나리오가 동일한 컨테이너를 재사용합니다.
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
     * Spring 환경(Environment)에 Testcontainers 접속 정보를 주입하는 초기화 클래스.
     *
     * <p>{@code TestPropertyValues}는 Spring의 {@link org.springframework.core.env.Environment}에
     * 직접 프로퍼티를 추가하므로 application.yml의 하드코딩 값보다 우선 적용됩니다.
     */
    public static class ContainerInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + POSTGRES.getJdbcUrl(),
                    "spring.datasource.username=" + POSTGRES.getUsername(),
                    "spring.datasource.password=" + POSTGRES.getPassword()
            ).applyTo(ctx.getEnvironment());
        }
    }
}
