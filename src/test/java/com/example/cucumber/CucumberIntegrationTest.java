package com.example.cucumber;

import org.junit.platform.suite.api.*;

import static io.cucumber.junit.platform.engine.Constants.*;

/**
 * Cucumber 통합 테스트 실행 진입점.
 *
 * <p>JUnit Platform Suite API를 사용하여 Cucumber 테스트를 실행합니다.
 *
 * <ul>
 *   <li>{@code @Suite}: JUnit 5 스위트 러너로 동작</li>
 *   <li>{@code @IncludeEngines}: cucumber 엔진만 사용</li>
 *   <li>{@code @SelectClasspathResource}: test/resources/features 폴더의 .feature 파일 탐색</li>
 *   <li>{@code GLUE_PROPERTY_NAME}: Step Definition 및 Hook 클래스가 위치한 패키지</li>
 * </ul>
 *
 * <p>실행 방법:
 * <pre>
 *   ./gradlew test
 * </pre>
 *
 * <p>리포트 위치: {@code build/reports/cucumber/}
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "com.example.cucumber")
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME,
        value = "pretty," +
                "html:build/reports/cucumber/cucumber.html," +
                "json:build/reports/cucumber/cucumber.json," +
                "junit:build/reports/cucumber/cucumber.xml")
public class CucumberIntegrationTest {
    // 이 클래스는 비어 있어야 합니다.
    // JUnit Platform Suite가 @SelectClasspathResource로 지정된 feature 파일을 찾아 실행합니다.
}
