package dan.kts.rustfinderplayer.entity;

import dan.kts.rustfinderplayer.entity.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requests")
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class Request {

    @Id
    @GeneratedValue
    private Long id;
    @ManyToOne
    @JoinColumn(name = "to_user_id")
    private User toUser;
    @ManyToOne
    @JoinColumn(name = "from_user_id")
    private User fromUser;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @PrePersist
    public void ensureStatus() {
        if (this.status == null) {
            this.status = RequestStatus.PENDING;
        }
    }
}
