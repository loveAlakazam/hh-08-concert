package io.hhplus.concert.domain.user;

import io.hhplus.concert.domain.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable = false, updatable = false)
	private long id; // 유저 PK

	@Column(name="name", nullable = false)
	private String name; // 유저명

	@OneToOne(
		mappedBy = "user",
		cascade = CascadeType.ALL,
		orphanRemoval = true,
		fetch = FetchType.LAZY
	)
	private UserPoint userPoint;


	// 정적팩토리 메소드
	@Builder
	private User(String name){
		this.name = name;
	}
	public static User of(String name) {
		return User
			.builder()
			.name(name)
			.build();
	}

	/**
	 * 정책
	 */
	public final static int MINIMUM_LENGTH_OF_NAME = 2;

}
