package com.pyounani.mailrequestor.exception;

import com.pyounani.mailrequestor.response.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BusinessLogicException extends RuntimeException{
    private final ErrorCode errorCode;
}
