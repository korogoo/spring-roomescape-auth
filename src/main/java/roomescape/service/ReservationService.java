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

    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }

    public Reservation save(ReservationCreateRequest request, Member member) {
        Reservation reservation = new Reservation(
            member.getUsername(), request.date(), request.time(), request.theme());
        return reservationRepository.save(reservation);
    }
}
