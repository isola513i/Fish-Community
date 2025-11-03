package sit.meetroom.meetingroomapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity @Table(name = "bookings",
        indexes = {
                @Index(name="idx_room_time", columnList = "room_id,startAt,endAt"),
                @Index(name="idx_user_time", columnList = "user_id,startAt")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="room_id")
    private Room room;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable=false, length=255)
    private String title;

    @Column(nullable=false)
    private Instant startAt;

    @Column(nullable=false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private BookingStatus status = BookingStatus.CONFIRMED;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private Instant cancelledAt;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
