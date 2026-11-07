package com.cesde.travelnorth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

import java.net.URI;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
        title = "Travel North API",
        version = "1.0.0",
        description = "API para gestionar destinos turísticos, paquetes y reservas."))
public class TravelNorthApplication {

    public static void main(String[] args) {
        SpringApplication.run(TravelNorthApplication.class, args);
    }

    @RestController
    static class ApiDocumentationRedirectController {

        @GetMapping("/")
        public ResponseEntity<Void> redirectToApiDocumentation() {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/scalar"))
                    .build();
        }
    }
}
