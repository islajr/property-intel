package io.propertyintel.api.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Property Intel API",
        version = "1.0",
        description = "A REST-ful API that provides a macro-level view of the Nigerian Residential Real-estate market"
))
public class OpenAPIConfig {
}
