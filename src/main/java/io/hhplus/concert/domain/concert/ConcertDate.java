package io.hhplus.concert.domain.concert;

import static io.hhplus.concert.domain.concert.ConcertSeat.*;
import static io.hhplus.concert.interfaces.api.user.CommonErrorCode.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.hhplus.concert.domain.common.BaseEntity;
import io.hhplus.concert.domain.reservation.Reservation;
import io.hhplus.concert.interfaces.api.common.BusinessException;
import io.hhplus.concert.interfaces.api.common.validators.EmptyStringValidator;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Builder;
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

	/**
	 * 정적 팩토리 메소드
	 * @param concert
	 * @param progressDate
	 * @param isAvailable
	 * @param place
	 */
	@Builder
	private ConcertDate(Concert concert, LocalDate progressDate, boolean isAvailable, String place) {
		this.concert = concert;
		this.progressDate = progressDate;
		this.isAvailable = isAvailable;
		this.place = place;
	}
	public static ConcertDate of(Concert concert, LocalDate progressDate, boolean isAvailable, String place) {
		if(concert == null) throw new BusinessException(NOT_NULLABLE);
		if(progressDate == null) throw new BusinessException(NOT_NULLABLE);
		if(EmptyStringValidator.isEmptyString(place)) throw new BusinessException(SHOULD_NOT_EMPTY);

		return ConcertDate.builder()
			.concert(concert)
			.progressDate(progressDate)
			.isAvailable(isAvailable)
			.place(place)
			.build();
	}

	/**
	 * 연관관계
	 */
	// 콘서트날짜:콘서트=N:1
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name ="concert_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private Concert concert;
	// 콘서트날짜:좌석=1:N
	@OneToMany(mappedBy = "concertDate", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ConcertSeat> seats = new ArrayList<>();
	// 콘서트날짜:예약=1:N
	@OneToMany(mappedBy = "concertDate")
	private List<Reservation> reservations = new ArrayList<>();

	/**
	 * 해당 공연에서 신규 공연날짜 추가할때 사용되며,
	 * 해당 날짜의 공연좌석 50개 정보를 초기화 및 추가.
	 *
	 * @param concert - 공연 도메인 엔티티
	 * @param price - 좌석 가격 초기값
	 */
	public void initializeSeats(Concert concert, long price) {
		// 콘서트 좌석 50개를 만든다
		for(int seatNumber = MIN_SEAT_NUMBER; seatNumber <= MAX_SEAT_NUMBER ; seatNumber++) {
			ConcertSeat concertSeat = ConcertSeat.of(concert, this, seatNumber, price , true);
			this.seats.add(concertSeat);
		}
	}
	/**
	 * 특정 날짜에 해당되는 공연좌석 정보들을 조회가 가능하다
	 */
	public List<ConcertSeat> concertSeats() {
		return this.seats;
	}
	/**
	 * 특정 날짜에 해당되는 예약 가능한 좌석 개수 조회할 수 있다
	 */
	public int countAvailableSeats() {
		List<ConcertSeat> availableSeats = this.seats.stream()
			.filter(concertSeat -> concertSeat.isAvailable())
			.collect(Collectors.toList());
		return availableSeats.size();
	}
	/**
	 * 좌석이 매진되면 일정의 상태값을 변경한다
	 */
	public void soldOut() {
		if( this.countAvailableSeats() == 0 ) {
			this.isAvailable = false;
		}
	}

	/**
	 * 비즈니스 정책
	 */
	public static int MINIMUM_LENGTH_OF_PLACE_NAME = 2;
	public static int MAXIMUM_LENGTH_OF_PLACE_NAME = 50;

}
