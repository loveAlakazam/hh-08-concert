package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.common.exceptions.CommonExceptionMessage.*;
import static io.hhplus.concert.domain.concert.ConcertExceptionMessage.*;

import java.time.LocalDate;
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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Getter
@Table(name ="concert_dates")
@RequiredArgsConstructor
public class ConcertDate extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id; // 날짜 PK

	@Column(name="progress_date", nullable = false)
	private LocalDate progressDate; // 공연날짜

	@Column(name= "is_available", nullable = false)
	private boolean isAvailable = true; // 예약가능여부

	@Column(name = "place", nullable = false)
	private String place; // 공연장소

	// TODO
	// @Column(name ="rest_of_seats", nullable = false)
	// private int RestOfSeats; // 남은좌석개수

	/**
	 * 생성자
	 */
	public ConcertDate(long id, LocalDate progressDate, boolean isAvailable, String place) {
		super();
		validateId(id);
		validateProgressDate(progressDate);
		validatePlace(place);

		this.id = id;
		this.progressDate = progressDate;
		this.place = place;
		this.isAvailable = isAvailable;
	}

	/**
	 * 연관관계
	 */
	// 콘서트날짜:콘서트=N:1
	@ManyToOne
	@JoinColumn(name ="concert_id")
	private Concert concert;
	// 콘서트날짜:좌석=1:N
	@OneToMany(mappedBy = "concertDate")
	private List<ConcertSeat> seats = new ArrayList<>();
	// 콘서트날짜:예약=1:N
	@OneToMany(mappedBy = "concertDate")
	private List<Reservation> reservations = new ArrayList<>();

	/**
	 * 비즈니스 정책
	 */
	public static int MINIMUM_LENGTH_OF_PLACE_NAME = 2;
	public static int MAXIMUM_LENGTH_OF_PLACE_NAME = 50;

	// 비즈니스 책임
	/**
	 * 장소명 유효성검사
	 *
	 * @param place - 장소명
	 */
	public static void validatePlace(String place) {
		if(place == null)
			throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		String nameRemovedWhiteSpaces = BaseEntity.getRegexRemoveWhitespace(place);
		if(nameRemovedWhiteSpaces.isEmpty())
			throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		if(nameRemovedWhiteSpaces.length() < MINIMUM_LENGTH_OF_PLACE_NAME)
			throw new InvalidValidationException(LENGTH_OF_PLACE_SHOULD_BE_MORE_THAN_MINIMUM_LENGTH);

		if(nameRemovedWhiteSpaces.length() > MAXIMUM_LENGTH_OF_PLACE_NAME)
			throw new InvalidValidationException(LENGTH_OF_PLACE_SHOULD_BE_LESS_THAN_MAXIMUM_LENGTH);
	}
	/**
	 * 공연 진행날짜 유효성검사
	 *
	 * @param progressDate
	 */
	public static void validateProgressDate(LocalDate progressDate) {
		if(progressDate == null) throw new InvalidValidationException(SHOULD_NOT_EMPTY);

		// progressDate 가 과거의 날짜라면 예외발생
		if(isPastDate(progressDate)) throw new InvalidValidationException(PAST_DATE_NOT_NOT_AVAILABLE);
	}

}
