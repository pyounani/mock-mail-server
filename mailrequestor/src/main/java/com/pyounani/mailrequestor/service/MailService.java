package com.pyounani.mailrequestor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final WebClient.Builder webClientBuilder;

    @Retryable(interceptor = "mailRetryInterceptor")
    @Async("mailServiceTaskExecutor")
    public void sendEmail(String toEmail,
                          String title,
                          String text) {

        WebClient webClient = webClientBuilder.baseUrl("http://localhost:2525").build();

        try {
            webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/send-email")
                            .queryParam("email", toEmail)
                            .build())
                    .retrieve()
                    .toBodilessEntity()
                    .block();

        } catch (WebClientResponseException  e) {
            log.error("Error occurred: {}", e.getResponseBodyAsString(), e);
        }
    }
}
