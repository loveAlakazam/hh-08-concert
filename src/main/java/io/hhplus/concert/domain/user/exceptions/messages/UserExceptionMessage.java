package io.hhplus.concert.domain.user.exceptions.messages;

import static io.hhplus.concert.domain.user.entity.User.*;

public interface UserExceptionMessage {
	String AMOUNT_SHOULD_BE_POSITIVE_NUMBER ="금액값은 0보다 큰 양수여야 합니다.";
	String CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM="충전금액의 최소값("+CHARGE_POINT_MINIMUM+"원)보다 커야 합니다.";
	String CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM="충전금액의 최대값("+CHARGE_POINT_MAXIMUM+"원)보다 작아야 합니다.";
	String LACK_OF_YOUR_POINT ="잔액이 부족합니다.";

}
