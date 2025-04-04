package com.example.vt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClient;

@SpringBootApplication
public class VtApplication {

    public static void main(String[] args) {
        SpringApplication.run(VtApplication.class, args);
    }


    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}

@Controller
@ResponseBody
class DelayController {

    private final RestClient http;

    DelayController(RestClient http) {
        this.http = http;
    }

    @GetMapping("/delay")
    String delay() {
        return this.http
                .get()
                .uri("http://localhost:9000/delay/5")
                .retrieve()
                .body(String.class);
    }
}