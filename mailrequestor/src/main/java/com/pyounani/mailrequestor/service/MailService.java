package com.pyounani.mailrequestor.service;

import com.pyounani.mailrequestor.loadTest.repository.LoadTestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.mail.MailSendException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final LoadTestResultRepository loadTestResultRepository;

    @Retryable(interceptor = "mailRetryInterceptor")
    @Async("mailServiceTaskExecutor")
    public void sendEmail(String toEmail, String title, String text, Long loadTestResultId) {
        requestSendMail(toEmail, title, text);
        loadTestResultRepository.updateFinishTime(loadTestResultId, LocalDateTime.now());
    }

    private void requestSendMail(String toEmail, String title, String text) {
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

            log.info("Email sent successfully to: {}, title: {}, text: {}", toEmail, title, text);

        } catch (RestClientResponseException e) {
            log.error("Error occurred : {}", e);
            HttpStatusCode statusCode = e.getStatusCode();
            if (statusCode.equals(HttpStatus.TOO_MANY_REQUESTS)) {
                throw new MailSendException("잠시 후 다시 시도해 주세요");
            }
        }
    }
}
