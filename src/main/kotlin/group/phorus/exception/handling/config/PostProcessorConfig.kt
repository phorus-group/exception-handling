package group.phorus.exception.handling.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.springframework.core.env.StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME

/**
 * Allow bean definition overriding by default.
 *
 * Needed to make the [WebfluxValidatorConfig] work, since there's already a Primary "defaultValidator" bean.
 */
@Order(Ordered.LOWEST_PRECEDENCE)
open class PostProcessorConfig : EnvironmentPostProcessor {
    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication?
    ) {
        environment.propertySources.addAfter(SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
            MapPropertySource("prefixed", mapOf(
                "spring.main.allow-bean-definition-overriding" to "true"
            ))
        );
    }
}