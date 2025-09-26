package com.example.bankcards.integration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OpenApiDocsTest {

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void v3ApiDocs_shouldReturn200() {
        String url = "http://localhost:" + port + "/v3/api-docs";
        ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);
        org.assertj.core.api.Assertions.assertThat(resp.getStatusCode().value()).isEqualTo(200);
        org.assertj.core.api.Assertions.assertThat(resp.getBody()).contains("openapi");
    }
}

