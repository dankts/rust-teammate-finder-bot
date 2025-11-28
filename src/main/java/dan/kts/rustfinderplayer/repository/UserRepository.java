package dan.kts.rustfinderplayer.repository;

import dan.kts.rustfinderplayer.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> getUserByChatId(Long chatId);

    boolean existsUserByChatId(Long chatId);

    @Query("SELECT u.chatId FROM User u WHERE u.isBanned = false")
    List<Long> findChatIdsByBannedIsFalse();
}
