package sit.meetroom.meetingroomapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Column(nullable=false, length=255)
    private String passwordHash;

    @Column(nullable=false, length=255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private UserRole role = UserRole.MEMBER;

    @Column(nullable=false, length=64)
    private String timezone = "Asia/Bangkok";

    @Column(nullable=false)
    private Boolean isActive = true;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}
