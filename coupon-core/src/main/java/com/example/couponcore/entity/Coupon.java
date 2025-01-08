package com.example.couponcore.entity;

import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "coupons")
public class Coupon extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private CouponType couponType;

    private Integer totalQuantity;

    @Column(nullable = false)
    private int issuedQuantity;

    @Column(nullable = false)
    private int discountAmount;

    @Column(nullable = false)
    private int minAvailableAmount;

    @Column(nullable = false)
    private LocalDateTime dateIssuedStart;

    @Column(nullable = false)
    private LocalDateTime dateIssuedEnd;

    //수량 검증
    public boolean availableIssueQuantity() {
        if(this.totalQuantity == null) {
            return true;
        }

        return this.totalQuantity > this.issuedQuantity;
    }

    //발급 기한 검증
    public boolean availableIssueDate() {
        LocalDateTime now = LocalDateTime.now();
        return this.dateIssuedStart.isBefore(now) && this.dateIssuedEnd.isAfter(now);
    }

    public void issue() {
        if(!this.availableIssueQuantity()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과하였습니다. 총 수량 : %s, 발급 수량 : %s".formatted(totalQuantity, issuedQuantity));
        }

        if(!this.availableIssueDate()) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_DATE, "발급 가능한 일자가 아닙니다. 요청 일자 : %s, 발급 시작 : %s, 발급 종료 : %s".formatted(LocalDateTime.now(), dateIssuedStart, dateIssuedEnd));
        }

        this.issuedQuantity++;
    }
}
