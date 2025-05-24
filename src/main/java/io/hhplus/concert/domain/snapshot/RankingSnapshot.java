package io.hhplus.concert.domain.snapshot;

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
public class RankingSnapshot {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDate date;

	@Lob
	@Column(nullable = false)
	private String jsonData;

	public static RankingSnapshot of(LocalDate date,  String jsonData) {
		RankingSnapshot snapshot = new RankingSnapshot();
		snapshot.date = date;
		snapshot.jsonData = jsonData;
		return snapshot;
	}
}
