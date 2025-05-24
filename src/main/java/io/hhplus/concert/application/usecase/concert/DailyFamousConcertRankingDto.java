package io.hhplus.concert.application.usecase.concert;

public record DailyFamousConcertRankingDto(
    String name, // 콘서트명
	String artistName, //  아티스트명
	String concertDate // 콘서트날짜
) { }

