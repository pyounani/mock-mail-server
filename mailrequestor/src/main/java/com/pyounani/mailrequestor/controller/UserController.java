package com.pyounani.mailrequestor.controller;

import com.pyounani.mailrequestor.dto.EmailDTO;
import com.pyounani.mailrequestor.response.code.ResponseCode;
import com.pyounani.mailrequestor.response.dto.ResponseDTO;
import com.pyounani.mailrequestor.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 이메일 인증 코드 요청하기
     */
    @PostMapping("/emails/verification-requests")
    public ResponseEntity<ResponseDTO> sendEmailVerification(@RequestBody EmailDTO emailDTO) {

        userService.sendCodeToEmail(emailDTO.getEmail());
        return ResponseEntity
                .status(ResponseCode.SUCCESS_VERIFICATION_REQUEST.getStatus().value())
                .body(new ResponseDTO<>(ResponseCode.SUCCESS_VERIFICATION_REQUEST, null));
    }
}
