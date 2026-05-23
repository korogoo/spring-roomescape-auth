package roomescape.service;

import java.util.Collection;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationCreateRequest;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomEscapeException;
import roomescape.repository.reservation.ReservationRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Reservation save(ReservationCreateRequest request, long memberId) {
        Reservation reservation = new Reservation(
            memberId, request.storeId(), request.date(), request.time(), request.theme());
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAllByMemberId(long memberId) {
        return reservationRepository.findAllByMemberId(memberId);
    }

    public List<Reservation> findAllByManagerId(long managerId) {
        return reservationRepository.findAllByStoreMemberId(managerId);
    }

    public void delete(long reservationId, long managerId) {
        boolean exists = reservationRepository.existsByIdAndStoreMemberId(reservationId, managerId);
        if (!exists) {
            throw new RoomEscapeException(ErrorCode.RESERVATION_NOT_FOUND);
        }
        reservationRepository.deleteByIdAndStoreMemberId(reservationId, managerId);
    }
}
