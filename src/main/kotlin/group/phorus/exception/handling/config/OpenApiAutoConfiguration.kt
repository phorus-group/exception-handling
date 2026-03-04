package group.phorus.exception.handling.config

import group.phorus.exception.handling.handlers.ApiError
import group.phorus.exception.handling.handlers.ValidationError
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.AutoConfiguration

private const val API_ERROR_SCHEMA_NAME = "ApiError"
private const val API_ERROR_REF = "#/components/schemas/$API_ERROR_SCHEMA_NAME"

@AutoConfiguration
@ConditionalOnClass(name = ["org.springdoc.core.customizers.OpenApiCustomizer"])
class OpenApiAutoConfiguration {

    @Bean
    fun apiErrorSchemaAndResponses(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            val schemas = ModelConverters.getInstance().read(ApiError::class.java)
            schemas.forEach { (name, schema) ->
                openApi.components.addSchemas(name, schema)
            }

            val validationErrorSchemas = ModelConverters.getInstance().read(ValidationError::class.java)
            validationErrorSchemas.forEach { (name, schema) ->
                openApi.components.addSchemas(name, schema)
            }

            val errorSchemaRef = Schema<ApiError>().`$ref`(API_ERROR_REF)

            val errorContent = Content().addMediaType(
                "application/json",
                MediaType().schema(errorSchemaRef)
            )

            val statusDescriptions: Map<String, String> = mapOf(
                "400" to "Bad Request",
                "401" to "Unauthorized",
                "403" to "Forbidden",
                "404" to "Not Found",
                "408" to "Request Timeout",
                "409" to "Conflict",
                "500" to "Internal Server Error",
            )

            openApi.paths?.values?.forEach { pathItem ->
                pathItem.readOperations().forEach { operation ->
                    val responses = operation.responses

                    statusDescriptions.forEach { (code, description) ->
                        if (responses[code] == null) {
                            responses[code] = ApiResponse()
                                .description(description)
                                .content(errorContent)
                        }
                    }
                }
            }
        }
}
