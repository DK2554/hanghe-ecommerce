package kr.hhplus.be.server.domain.coupon;


import kr.hhplus.be.server.domain.user.User;
import kr.hhplus.be.server.support.ErrorCode;
import kr.hhplus.be.server.support.HangHeaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;

    public List<IssuedCoupon> getByUserId(long userId) {
        return couponRepository.findAllByUserId(userId)
                .orElseThrow(() -> new HangHeaException(ErrorCode.NOT_FOUND_USER_COUPON));
    }
    @Transactional
    public Coupon findCouponForUpdate(long couponId) {
        return couponRepository.findCouponForUpdate(couponId)
                .orElseThrow(() -> new HangHeaException(ErrorCode.INVALID_COUPON));
    }
    @Transactional
    public IssuedCoupon issueCoupon(long couponId, User user) {
        Coupon coupon = this.findCouponForUpdate(couponId);
        //쿠폰 발급처리;
        coupon.incrementIssuedCount();
        couponRepository.save(coupon);
        //쿠폰 발급 저장
        IssuedCoupon issuedCoupon = new IssuedCoupon(user.getId(), coupon.getId());
        couponRepository.save(issuedCoupon);  // 발급된 쿠폰 저장
        return issuedCoupon; // 발급된 쿠폰 반환
    }
    @Transactional
    public IssuedCoupon useCoupon(long issueCouponId, long userId) {
        // 발급된 쿠폰을 찾아오기
        IssuedCoupon issuedCoupon = this.findByIdAndUserId(issueCouponId, userId);
        Coupon coupon = this.findByCouponId(issuedCoupon.getCouponId());
        // 쿠폰 사용 처리
        issuedCoupon.markAsUsed(coupon);  // 쿠폰 검증 및 사용 처리
        couponRepository.save(issuedCoupon);  // 사용된 쿠폰 업데이트
        return issuedCoupon;
    }
    @Transactional
    public IssuedCoupon findByIdAndUserId(long issuedCouponId, long userId) {
        return couponRepository.findByIdAndUserId(issuedCouponId, userId)
                .orElseThrow(() -> new HangHeaException(ErrorCode.NOT_FOUND_USER_COUPON));
    }

    public Coupon findByCouponId(long couponId) {
        return couponRepository.findByCouponId(couponId)
                .orElseThrow(() -> new HangHeaException(ErrorCode.INVALID_COUPON));
    }
}
