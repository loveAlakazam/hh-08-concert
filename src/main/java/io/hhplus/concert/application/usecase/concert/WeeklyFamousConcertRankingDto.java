package io.hhplus.concert.application.usecase.concert;

public record WeeklyFamousConcertRankingDto (
	long id, // 콘서트 아이디
	String name, // 콘서트 이름
	String artistName // 콘서트 아티스트명
){ }
