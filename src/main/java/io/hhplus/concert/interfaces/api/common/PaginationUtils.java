package io.hhplus.concert.interfaces.api.common;

import static io.hhplus.concert.interfaces.api.common.validators.PaginationValidator.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.util.Collections;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PaginationUtils {
	public static <T> Page<T> toPage(List<T> list, int page) {
		if(page < 1) throw new InvalidValidationException(INVALID_PAGE);
		// 시작페이지값은 1이고, 페이지네이션 시작점은 0이므로 1을 뺀다
		page = page -1 ;

		if(list == null || list.isEmpty()) {
			return Page.empty(PageRequest.of(page, PAGE_SIZE));
		}

		// 한페이지당 최대 10개를 가져온다.
		int start = (page) * PAGE_SIZE;
		int end = Math.min(start + PAGE_SIZE, list.size());

		if( start >= list.size() )
			return new PageImpl<>(
				Collections.emptyList(),
				PageRequest.of(page, PAGE_SIZE), list.size()
			);

		List<T> pageContent = list.subList(start, end);

		return new PageImpl<>(
			pageContent,
			PageRequest.of(page, PAGE_SIZE),
			list.size()
		);
	}
}
