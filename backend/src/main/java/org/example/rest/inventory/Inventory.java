package org.example.rest.inventory;

import jakarta.persistence.*;
import lombok.*;
import org.example.rest.room.Room;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "inventory",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_inventory_room_date",
                columnNames = {"room_id", "date"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "room")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private Integer totalRooms;

    @Column(nullable = false)
    private Integer availableRooms;

    @Version
    private Long version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean hasAvailability(int requested) {
        return availableRooms >= requested;
    }

    public void reserve(int count) {
        if (!hasAvailability(count))
            throw new IllegalStateException(
                    "Not enough rooms available on " + date +
                            " (requested=" + count + ", available=" + availableRooms + ")");
        this.availableRooms -= count;
    }

    public void release(int count) {
        this.availableRooms = Math.min(availableRooms + count, totalRooms);
    }
}