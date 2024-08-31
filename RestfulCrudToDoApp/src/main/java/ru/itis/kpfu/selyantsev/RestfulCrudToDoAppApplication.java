package ru.itis.kpfu.selyantsev;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition
public class RestfulCrudToDoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestfulCrudToDoAppApplication.class, args);
    }

}
