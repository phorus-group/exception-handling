package group.phorus.exception.handling.bdd

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [TestApp::class])
@CucumberContextConfiguration
@AutoConfigureWebTestClient
class CucumberSpringConfiguration

@SpringBootApplication(scanBasePackages = ["group.phorus"])
class TestApp