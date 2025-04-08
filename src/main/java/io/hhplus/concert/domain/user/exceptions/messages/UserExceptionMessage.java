package io.hhplus.concert.domain.user.exceptions.messages;

import static io.hhplus.concert.domain.user.entity.User.*;

import io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage;

public interface UserExceptionMessage extends CommonExceptionMessage {
	String POINT_SHOULD_BE_POSITIVE_NUMBER ="포인트값은 0이상의 양수여야 합니다.";
	String AMOUNT_SHOULD_BE_POSITIVE_NUMBER ="금액값은 0보다 큰 양수여야 합니다.";

	String LENGTH_OF_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH = "이름은 최소 "+MINIMUM_LENGTH_OF_NAME+"자 이상이어야합니다.";
	String CHARGE_AMOUNT_SHOULD_BE_MORE_THAN_MINIMUM="충전금액의 최소값("+CHARGE_POINT_MINIMUM+"원)보다 커야 합니다.";
	String CHARGE_AMOUNT_SHOULD_BE_LESS_THAN_MAXIMUM="충전금액의 최대값("+CHARGE_POINT_MAXIMUM+"원)보다 작아야 합니다.";
	String LACK_OF_YOUR_POINT ="잔액이 부족합니다.";

}
