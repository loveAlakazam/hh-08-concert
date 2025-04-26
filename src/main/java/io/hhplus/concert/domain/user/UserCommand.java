package io.hhplus.concert.domain.user;

import static io.hhplus.concert.domain.user.User.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.UserErrorCode.*;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.common.validators.EmptyStringValidator;
import io.hhplus.concert.interfaces.api.user.UserRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCommand {

	public record CreateNewUser(String name) {
		public static CreateNewUser from(String name) {
			if(EmptyStringValidator.isEmptyString(name)) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
			if(name.length() < MINIMUM_LENGTH_OF_NAME)
				throw new InvalidValidationException(LENGTH_OF_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);
			return new CreateNewUser(name);
		}
	}

	public record Get(long id) {
		public static Get of(long id) {
			return new Get(id);
		}
	}
}
