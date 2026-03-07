package group.phorus.exception.handling.bdd

import io.cucumber.spring.CucumberContextConfiguration
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean

@SpringBootTest(classes = [TestApp::class])
@CucumberContextConfiguration
@AutoConfigureWebTestClient
class CucumberSpringConfiguration

@SpringBootApplication(scanBasePackages = ["group.phorus"])
class TestApp {
    @Bean
    fun meterRegistry(): MeterRegistry = SimpleMeterRegistry()
}
