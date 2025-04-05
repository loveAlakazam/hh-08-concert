package io.hhplus.concert.domain.commons.entity;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import io.hhplus.concert.domain.common.entity.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;

public class BaseEntityTest {
	@Test
	void 식별자가_0이하의값을_가지면_InvalidValidationException_예외발생(){
		// given
		long id = 0;

		// when & then
		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			()-> BaseEntity.validateId(id)
		);
		assertEquals(ID_SHOULD_BE_POSITIVE_NUMBER, ex.getMessage());
	}
}
