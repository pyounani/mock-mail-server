package com.pyounani.mailrequestor.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    @Retryable(interceptor = "mailRetryInterceptor")
    @Async("mailServiceTaskExecutor")
    public void sendEmail(String toEmail, String title, String text) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:2525")
                .build();

        try {
            restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/send-email")
                            .queryParam("email", toEmail)
                            .build())
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email sent successfully to: {}", toEmail);
        } catch (RestClientResponseException e) {
            log.error("Error occurred : {}", e);
        }
    }
}
