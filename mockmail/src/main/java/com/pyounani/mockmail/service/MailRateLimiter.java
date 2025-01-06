package com.pyounani.mockmail.service;

public interface MailRateLimiter {
    boolean isAllowed(String email);
}
