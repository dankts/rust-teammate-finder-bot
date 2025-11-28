package dan.kts.rustfinderplayer.entity;

import dan.kts.rustfinderplayer.entity.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Entity
@Builder
@RequiredArgsConstructor
@Table(name = "users")
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String username;
    @Column(name = "chat_id", unique = true)
    private Long chatId;
    private String nickname;
    private Integer hours;
    private Integer age = 10;
    @Enumerated(EnumType.STRING)
    private Role role = Role.PvP;
    @Column(name = "steam_link")
    private String steamLink;
    @Column(name = "find_now")
    private boolean isFindNow;
    @Column(name = "is_banned", nullable = false)
    private boolean isBanned = false;
}
