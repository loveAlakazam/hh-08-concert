package io.hhplus.concert.interfaces;

import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.common.PaginationUtils;

public class PaginationUtilsTest {
	@Test
	void page가_1미만이면_InvalidValidationException_예외발생() {
		List<String> list = new ArrayList<>();
		int page = 0; // invalid

		InvalidValidationException ex = assertThrows(
			InvalidValidationException.class,
			() -> PaginationUtils.toPage(list, page)
		);
		assertEquals(INVALID_PAGE.getMessage(), ex.getMessage());
	}
	@Test
	void 만일_리스트의_데이터가_10개미만이면_1페이지만_보여준다() {
		List<String> list = new ArrayList<>();
		list.add("재밋는");
		list.add("tdd");

		int page =1;
		Page<String> result = assertDoesNotThrow(() -> PaginationUtils.toPage(list, page));
		assertEquals(2, result.getTotalElements());
		assertEquals(1, result.getTotalPages());
		assertEquals(2, result.getContent().size()); // 1페이지에는 2개의 결과가 있다
		assertEquals("tdd", result.getContent().get(1));
	}
	@Test
	void 만일_리스트의_데이터가_10개미만이면_2페이지를_볼_수없다() {
		List<String> list = new ArrayList<>();
		list.add("재밋는");
		list.add("tdd");

		int page = 2;
		Page<String> result = assertDoesNotThrow(() -> PaginationUtils.toPage(list, page));
		assertEquals(2, result.getTotalElements());
		assertEquals(1, result.getTotalPages());
		assertEquals(0, result.getContent().size()); // 2페이지는 결과가 없다
	}
	@Test
	void 만일_리스트의_데이터가_10개이상이라면_1페이지의_최대개수는_10개다() {
		List<String> list = new ArrayList<>();
		for( int i = 0; i< 15; i++) {
			list.add("tdd"+(i+1));
		}
		int page = 1;
		Page<String> result = assertDoesNotThrow(() -> PaginationUtils.toPage(list, page));
		assertEquals(15, result.getTotalElements());
		assertEquals(2, result.getTotalPages());
		assertEquals(10, result.getNumberOfElements());
		assertEquals(10, result.getContent().size()); // 1페이지에는 10개의 결과가 있다
		assertEquals("tdd"+1, result.getContent().get(0));
		assertEquals("tdd"+2, result.getContent().get(1));
		assertEquals("tdd"+10, result.getContent().get(9));
	}
	@Test
	void 만일_리스트의_데이터가_10개이상이라면_2페이지의_최대개수는_5개다() {
		List<String> list = new ArrayList<>();
		for( int i = 0; i< 15; i++) {
			list.add("tdd"+(i+1));
		}
		int page = 2;
		Page<String> result = assertDoesNotThrow(() -> PaginationUtils.toPage(list, page));
		assertEquals(15, result.getTotalElements());
		assertEquals(2, result.getTotalPages());
		assertEquals(5, result.getNumberOfElements());
		assertEquals(5, result.getContent().size()); // 2페이지에는 5개의 결과가 있다
		assertEquals("tdd"+11, result.getContent().get(0));
		assertEquals("tdd"+12, result.getContent().get(1));
		assertEquals("tdd"+15, result.getContent().get(4));
	}

}
