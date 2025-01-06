package com.pyounani.mockmail.controller;

import com.pyounani.mockmail.service.MailRateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MockMailController {

    private final MailRateLimiter mailRateLimiter;

    @PostMapping("/mock/emails/verification-requests")
    public ResponseEntity<Void> sendMail(@RequestParam String account) {
        log.info("메일 발송 요청 수신: account={}", account);

        if(!mailRateLimiter.isAllowed(account)) {
            log.warn("메일 발송 제한 초과: account={}", account);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }
        sleep(4000L); // 실제 GMAIL SMTP를 사용했을 때 걸리는 요청 처리 시간

        log.info("메일 발송 완료: account={}", account);
        return ResponseEntity.ok().build();
    }

    private static void sleep(Long millis) {
        try {
            Thread.sleep(millis);
            log.info("메일 처리 대기 시간 종료: {}ms", millis);
        } catch (InterruptedException e) {
            log.error("메일 처리 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("sleep 에러");
        }
    }
}
