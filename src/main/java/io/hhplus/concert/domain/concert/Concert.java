package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static io.hhplus.concert.domain.concert.ConcertExceptionMessage.*;

import java.util.ArrayList;
import java.util.List;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.common.exceptions.InvalidValidationException;
import io.hhplus.concert.domain.reservation.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@Table(name ="concerts")
@RequiredArgsConstructor
public class Concert extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 콘서트 PK

	@Column(name="name", nullable = false)
	private String name; // 콘서트명

	@Column(name="artist_name", nullable = false)
	private String artistName; // 콘서트 아티스트 이름

	/**
	 * 유효성검사 및 생성자 호출
	 *
	 * @param id
	 * @param name
	 * @param artistName
	 */
	public Concert(long id, String name, String artistName) {
		validateId(id);
		validateConcertName(name);
		validateArtistName(artistName);

		this.id = id;
		this.name = name;
		this.artistName = artistName;
	}

	/**
	 * 연관관계
	 */
	// 콘서트:콘서트날짜 = 1:N
	@OneToMany(mappedBy = "concert")
	private List<ConcertDate> dates = new ArrayList<>();
	// 콘서트:콘서트좌석 = 1:N
	@OneToMany(mappedBy = "concert")
	private List<ConcertSeat> seats = new ArrayList<>();
	// 콘서트:예약 = 1:N
	@OneToMany(mappedBy = "concert")
	private List<Reservation> reservations = new ArrayList<>();

	/**
	 * 비즈니스 정책
	 */
	public static int MINIMUM_LENGTH_OF_CONCERT_NAME= 10;
	public static int MAXIMUM_LENGTH_OF_CONCERT_NAME= 100;

	public static int MINIMUM_LENGTH_OF_ARTIST_NAME = 2;
	public static int MAXIMUM_LENGTH_OF_ARTIST_NAME = 30;

	 // 비즈니스 책임
	/**
	 * 콘서트명 유효성 검사
	 *
	 * @param name - 콘서트명
	 */
	public static void validateConcertName(String name) {
		if(name == null)
			throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		String nameRemovedWhiteSpaces = BaseEntity.getRegexRemoveWhitespace(name);
		if(nameRemovedWhiteSpaces.isEmpty())
			throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		if(nameRemovedWhiteSpaces.length() < MINIMUM_LENGTH_OF_CONCERT_NAME)
			throw new InvalidValidationException(LENGTH_OF_CONCERT_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);

		if(nameRemovedWhiteSpaces.length() > MAXIMUM_LENGTH_OF_CONCERT_NAME)
			throw new InvalidValidationException(LENGTH_OF_CONCERT_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH);
	}
	/**
	 * 아티스트명 유효성검사
	 * @param artistName - 아티스트명
	 */
	public static void validateArtistName(String artistName) {
		if(artistName == null)
			throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		String artistNameRemovedWhiteSpaces = BaseEntity.getRegexRemoveWhitespace(artistName);
		if(artistNameRemovedWhiteSpaces.isEmpty())
			throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		if(artistNameRemovedWhiteSpaces.length() < MINIMUM_LENGTH_OF_ARTIST_NAME)
			throw new InvalidValidationException(LENGTH_OF_ARTIST_NAME_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);

		if(artistNameRemovedWhiteSpaces.length() > MAXIMUM_LENGTH_OF_ARTIST_NAME)
			throw new InvalidValidationException(LENGTH_OF_ARTIST_NAME_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH);
	}
}
