package dan.kts.rustfinderplayer.service;

import dan.kts.rustfinderplayer.entity.enums.Role;
import dan.kts.rustfinderplayer.entity.User;
import dan.kts.rustfinderplayer.exceptions.UserNotFoundException;
import dan.kts.rustfinderplayer.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<Long> getUsersChatId() {
        return userRepository.findChatIdsByBannedIsFalse();
    }

    public User getUser(Long chatId) {
        return userRepository.getUserByChatId(chatId).orElseThrow();
    }

    @Transactional
    public void updateRole(Long chatId, Role role) {
        User user = userRepository.getUserByChatId(chatId).orElseThrow(
                () -> new UserNotFoundException("User not found")
        );
        user.setRole(role);
    }

    public boolean getIsFindNow(Long chatId) {
        AtomicBoolean findNow = new AtomicBoolean(false);
        userRepository.getUserByChatId(chatId).ifPresent(user -> {
            findNow.set(user.isFindNow());
        });
        return findNow.get();
    }

    @SneakyThrows
    public String getUserProfile(Long chatId) {
        Optional<User> userByChatId = userRepository.getUserByChatId(chatId);
        User user = userByChatId.orElseThrow(TelegramApiException::new);
        return """
                🎮 <b>Профиль игрока</b> — %s
                
                ──────────────────────
                🆔 <b>Никнейм:</b> <code>%s</code>
                ⏱ <b>Часов в игре:</b> <code>%s</code>
                🔞 <b>Возраст:</b> <code>%s</code>
                🌐 <b>Steam:</b> <a href="%s">%s</a>
                🛠 <b>Роль:</b> <code>%s</code>
                ──────────────────────
                🔍 <b>Поиск команды:</b> %s
                """.formatted(
                user.getNickname(),
                user.getNickname(),
                user.getHours(),
                user.getAge(),
                user.getSteamLink(),
                user.getSteamLink() == null ? "Не указан профиль" : "Открыть профиль",
                user.getRole().getDisplayName(),
                user.isFindNow() ? "✅ Активен" : "⏸ Остановлен"
        );
    }

    public String getTeammateProfile(Long chatId) {
        StringBuilder stringBuilder = new StringBuilder();
        userRepository.getUserByChatId(chatId).ifPresent(user -> {
            stringBuilder.append("""
                    🎮 <b>Профиль игрока</b> — %s
                    
                    ──────────────────────
                    🆔 <b>Никнейм:</b> <code>%s</code>
                    ⏱ <b>Часов в игре:</b> <code>%s</code>
                    🔞 <b>Возраст:</b> <code>%s</code>
                    🌐 <b>Steam:</b> <a href="%s">%s</a>
                    🛠 <b>Роль:</b> <code>%s</code>
                    ──────────────────────
                    """.formatted(
                    user.getNickname(),
                    user.getNickname(),
                    user.getHours(),
                    user.getAge(),
                    user.getSteamLink(),
                    user.getSteamLink() == null ? "Не указан профиль" : "Открыть профиль",
                    user.getRole().getDisplayName()
            ));
        });
        return stringBuilder.toString();
    }

    @SneakyThrows
    public void inLinkSteam(Long chatId, String steamLink) {
        boolean b = userRepository.existsUserByChatId(chatId);
        if (b) {
            Optional<User> userByChatId = userRepository.getUserByChatId(chatId);
            User user = userByChatId.orElseThrow(TelegramApiException::new);
            user.setSteamLink(steamLink);
            userRepository.save(user);
        }
    }

    public boolean isRegisteredUser(Long chatId) {
        return userRepository.existsUserByChatId(chatId);
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void banUser(Long chatId) {
        userRepository.getUserByChatId(chatId).ifPresentOrElse(user -> {
            user.setBanned(true);
        }, () -> {
            throw new UserNotFoundException("Пользователь не найден");
        });
    }

    @Transactional
    public void unBanUser(Long chatId) {
        userRepository.getUserByChatId(chatId).ifPresentOrElse(user -> {
            user.setBanned(false);
        }, () -> {
            throw new UserNotFoundException("Пользователь не найден");
        });
    }

    public boolean isBanned(Long chatId) {
        return userRepository.getUserByChatId(chatId).map(User::isBanned).orElse(false);
    }

    public void setFindNow(Long chatId, boolean findNow) {
        userRepository.getUserByChatId(chatId).ifPresent(user -> {
            user.setFindNow(findNow);
            userRepository.save(user);
        });
    }
}
