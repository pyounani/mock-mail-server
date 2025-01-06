package com.pyounani.mailrequestor.service;

import com.pyounani.mailrequestor.exception.BusinessLogicException;
import com.pyounani.mailrequestor.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class UserService {

    private final MailService mailService;

    /**
     * 인증 코드 검증을 위한 이메일 전송
     */
    public void sendCodeToEmail(String toEmail, Long loadTestResultId) {
        String title = "StoryTeller 이메일 인증 번호";
        String authCode = this.createCode();
        mailService.sendEmail(toEmail, title, authCode, loadTestResultId);
    }


    private String createCode() {
        int len = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < len; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessLogicException(ErrorCode.NO_SUCH_ALGORITHM);
        }
    }
}
