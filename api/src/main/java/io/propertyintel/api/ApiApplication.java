package io.propertyintel.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@OpenAPIDefinition(info = @Info(
        title = "Property Intel API",
        version = "1.0",
        description = "A REST-ful API that provides a macro-level view of the Nigerian Residential Real-estate market"
))
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

}
