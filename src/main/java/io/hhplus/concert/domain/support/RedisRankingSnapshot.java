package io.hhplus.concert.domain.support;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Getter
@Table(
	name = "redis_ranking_snapshots",
	uniqueConstraints = @UniqueConstraint(columnNames = {"date"}) // 날짜별 1건 제한
)
public class RedisRankingSnapshot {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate date;

	@Lob
	@Column(nullable = false)
	private String jsonData;

	public static RedisRankingSnapshot of(LocalDate date,  String jsonData) {
		RedisRankingSnapshot snapshot = new RedisRankingSnapshot();
		snapshot.date = date;
		snapshot.jsonData = jsonData;
		return snapshot;
	}
}
