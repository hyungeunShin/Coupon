package com.example.couponcore.service;

import com.example.couponcore.dto.CouponRedisEntity;
import com.example.couponcore.exception.CouponIssueException;
import com.example.couponcore.exception.ErrorCode;
import com.example.couponcore.repository.redis.RedisRepository;
import com.example.couponcore.util.CouponRedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponIssueRedisService {
    private final RedisRepository redisRepository;

    public void checkCouponIssueQuantity(CouponRedisEntity entity, Long userId) {
        if(!availableUserIssueQuantity(entity.id(), userId)) {
            throw new CouponIssueException(ErrorCode.DUPLICATED_COUPON_ISSUE, "이미 발급된 쿠폰입니다.");
        }

        if(!availableTotalIssueQuantity(entity.totalQuantity(), entity.id())) {
            throw new CouponIssueException(ErrorCode.INVALID_COUPON_ISSUE_QUANTITY, "발급 가능한 수량을 초과합니다.");
        }
    }

    public Boolean availableTotalIssueQuantity(Integer totalQuantity, Long couponId) {
        if(totalQuantity == null) {
            return true;
        }

        String key = CouponRedisUtil.getIssueRequestKey(couponId);
        return totalQuantity > redisRepository.sCard(key);
    }

    public Boolean availableUserIssueQuantity(Long couponId, Long userId) {
        return !redisRepository.sIsMember(CouponRedisUtil.getIssueRequestKey(couponId), String.valueOf(userId));
    }
}
