package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.ConcertSeat.*;
import static io.hhplus.concert.interfaces.api.concert.ConcertErrorCode.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.InvalidValidationException;
import io.hhplus.concert.interfaces.api.common.validators.DateValidator;
import io.hhplus.concert.interfaces.api.common.validators.EmptyStringValidator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Builder;
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
	 * 정적팩토리 메소드
	 *
	 * @param name
	 * @param artistName
	 */
	@Builder
	private Concert(String name, String artistName) {
		this.name = name;
		this.artistName = artistName;
	}
	public static Concert of(String name, String artistName) {
		if(EmptyStringValidator.isEmptyString(name)) throw new BusinessException(SHOULD_NOT_EMPTY);
		if(EmptyStringValidator.isEmptyString(artistName)) throw new BusinessException(SHOULD_NOT_EMPTY);

		return Concert.builder()
			.name(name)
			.artistName(artistName)
			.build();
	}

	/**
	 * 연관관계
	 */
	// 콘서트:콘서트날짜 = 1:N
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true )
	@JsonIgnore
	private List<ConcertDate> dates = new ArrayList<>();

	// 콘서트:콘서트좌석 = 1:N
	@OneToMany(mappedBy = "concert", cascade = CascadeType.ALL, orphanRemoval = true )
	@JsonIgnore
	private List<ConcertSeat> seats = new ArrayList<>();

	// 콘서트:예약 = 1:N
	@OneToMany(mappedBy = "concert")
	@JsonIgnore
	private List<Reservation> reservations = new ArrayList<>();

	/**
	 * 콘서트 정보와 콘서트일정, 좌석정보 추가한다
	 * @param name - 콘서트명
	 * @param artistName - 콘서트 아티스트명
	 * @param progressDate - 진행날짜
	 * @param place - 장소
	 * @param price - 좌석 가격
	 * @return Concert
	 */
	public static Concert create(String name, String artistName, LocalDate progressDate, String place, long price) {
		if(progressDate == null) throw new InvalidValidationException(NOT_NULLABLE);
		if(!DateValidator.isAvailableDate(progressDate)) throw new InvalidValidationException(PAST_DATE_NOT_AVAILABLE);
		if(EmptyStringValidator.isEmptyString(place)) throw new InvalidValidationException(SHOULD_NOT_EMPTY);
		if(price <= 0) throw new InvalidValidationException(PRICE_SHOULD_BE_POSITIVE_NUMBER);
		if(price < MINIMUM_SEAT_PRICE) throw new InvalidValidationException(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE);
		if(price > MAXIMUM_SEAT_PRICE) throw new InvalidValidationException(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE);

		// 콘서트 생성
		Concert concert = Concert.of(name, artistName);
		concert.addConcertDate(progressDate, place, price);
		return concert;
	}
	/**
	 * 콘서트 일정과 좌석을 추가한다
	 *
	 * @param progressDate - 진행날짜
	 * @param place - 공연 장소
	 * @param price - 초기 좌석 가격
	 */
	public void addConcertDate(LocalDate progressDate, String place, long price) {
		if(progressDate == null) throw new InvalidValidationException(NOT_NULLABLE);
		if(price <= 0) throw new InvalidValidationException(PRICE_SHOULD_BE_POSITIVE_NUMBER);
		if(price < MINIMUM_SEAT_PRICE) throw new InvalidValidationException(PRICE_SHOULD_BE_MORE_THAN_MINIMUM_PRICE);
		if(price > MAXIMUM_SEAT_PRICE) throw new InvalidValidationException(PRICE_SHOULD_BE_LESS_THAN_MAXIMUM_PRICE);

		// 공연날짜 정보 추가
		ConcertDate newConcertDate = ConcertDate.of(this, progressDate, true, place);
		// 해당날짜의 좌석 50개 초기화
		newConcertDate.initializeSeats(this, price);
		// 리스트에 날짜정보 추가
		this.dates.add(newConcertDate);
	}
	/**
	 * 현재날짜를 기준으로 예약이 가능한 날짜목록을 조회한다
	 */
	public List<ConcertDate> availableConcertDates() {
		return dates.stream()
			.filter( concertDate -> DateValidator.isAvailableDate(concertDate.getProgressDate()) )
			.filter( concertDate -> concertDate.isAvailable() )
			.collect(Collectors.toList());
	}
	/**
	 * 비즈니스 정책
	 */
	public static int MINIMUM_LENGTH_OF_CONCERT_NAME= 2;
	public static int MAXIMUM_LENGTH_OF_CONCERT_NAME= 100;

	public static int MINIMUM_LENGTH_OF_ARTIST_NAME = 2;
	public static int MAXIMUM_LENGTH_OF_ARTIST_NAME = 30;
	public static int MINIMUM_LENGTH_OF_PLACE_NAME = 2;
	public static int MAXIMUM_LENGTH_OF_PLACE_NAME= 100;

}
