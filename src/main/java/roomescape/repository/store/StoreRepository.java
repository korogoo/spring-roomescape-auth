package roomescape.repository.store;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Store;

public interface StoreRepository {

    Store save(Store store);

    Optional<Store> findById(long id);

    List<Store> findAllByMemberId(Long memberId);
}
