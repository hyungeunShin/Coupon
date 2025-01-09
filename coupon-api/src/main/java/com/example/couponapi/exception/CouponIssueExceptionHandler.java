package com.example.couponapi.exception;

import com.example.couponapi.controller.dto.CouponIssueResponseDTO;
import com.example.couponcore.exception.CouponIssueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CouponIssueExceptionHandler {
    @ExceptionHandler(CouponIssueException.class)
    public CouponIssueResponseDTO exceptionHandler(CouponIssueException e) {
        return new CouponIssueResponseDTO(false, e.getErrorCode().message);
    }
}
