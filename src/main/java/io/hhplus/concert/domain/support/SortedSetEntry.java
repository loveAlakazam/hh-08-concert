package io.hhplus.concert.domain.support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SortedSetEntry {
	private final Object value;
	private final double score;
}
