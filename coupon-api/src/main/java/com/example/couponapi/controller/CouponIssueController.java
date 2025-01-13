package com.example.couponapi.controller;

import com.example.couponapi.controller.dto.CouponIssueRequestDTO;
import com.example.couponapi.controller.dto.CouponIssueResponseDTO;
import com.example.couponapi.service.CouponIssueRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponIssueController {
    private final CouponIssueRequestService service;

    @PostMapping("/v1/issue")
    public CouponIssueResponseDTO issueV1(@RequestBody CouponIssueRequestDTO dto) {
        service.issueRequestV1(dto);
        return new CouponIssueResponseDTO(true, null);
    }

    @PostMapping("/v1/issue-async")
    public CouponIssueResponseDTO asyncIssueV1(@RequestBody CouponIssueRequestDTO dto) {
        service.asyncIssueRequestV1(dto);
        return new CouponIssueResponseDTO(true, null);
    }

    @PostMapping("/v2/issue-async")
    public CouponIssueResponseDTO asyncIssueV2(@RequestBody CouponIssueRequestDTO dto) {
        service.asyncIssueRequestV2(dto);
        return new CouponIssueResponseDTO(true, null);
    }
}
