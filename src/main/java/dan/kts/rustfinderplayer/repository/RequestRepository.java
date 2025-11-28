package dan.kts.rustfinderplayer.repository;

import dan.kts.rustfinderplayer.entity.Request;
import dan.kts.rustfinderplayer.entity.User;
import dan.kts.rustfinderplayer.entity.enums.RequestStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends CrudRepository<Request, Long> {

    Optional<Request> findAByFromUserAndToUser(User fromUser, User toUser);

    List<Request> findAllByToUserAndStatus(User toUser, RequestStatus status);

    boolean existsByFromUserAndToUserAndStatus(User fromUser, User toUser, RequestStatus status);

    boolean existsByFromUserAndToUser(User fromUser, User toUser);
}
