package io.hhplus.concert.application.usecase.concert;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;

public class ConcertCriteria {
	public record SoldOutConcertDate(Long concertId, Long concertDateId) {
		public static  SoldOutConcertDate of(Long concertId, Long concertDateId) {
			if(concertId == null) throw new InvalidValidationException(NOT_NULLABLE);
			if(concertId <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);

			if(concertDateId == null) throw new InvalidValidationException(NOT_NULLABLE);
			if(concertDateId <= 0 ) throw new InvalidValidationException(ID_SHOULD_BE_POSITIVE_NUMBER);

			return new SoldOutConcertDate(concertId, concertDateId);
		}
	}
}
