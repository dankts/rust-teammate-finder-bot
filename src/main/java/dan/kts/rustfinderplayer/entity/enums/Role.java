package dan.kts.rustfinderplayer.entity.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Role {
    PvP("Комбатер/Стрелок"),
    Builder("Билдер"),
    Farmer("Фермер"),
    Electrician("Электрик");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public static Role getFromdisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(role -> role.getDisplayName().equals(displayName))
                .findFirst().orElse(null);
    }
}
