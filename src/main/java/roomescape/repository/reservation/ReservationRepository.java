package roomescape.repository.reservation;

import java.util.List;
import roomescape.domain.Reservation;

public interface ReservationRepository {

    List<Reservation> findAll();

    Reservation save(Reservation reservation);

    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByStoreMemberId(long managerId);

    boolean existsByIdAndStoreMemberId(long reservationId, long managerId);

    void deleteByIdAndStoreMemberId(long reservationId, long managerId);
}
