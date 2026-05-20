package roomescape.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.dto.reservation.ReservationCreateRequest;
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
            memberId, request.date(), request.time(), request.theme());
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findAllByMemberId(long memberId) {
        return reservationRepository.findAllByMemberId(memberId);
    }
}
