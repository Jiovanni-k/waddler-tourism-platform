package org.example.rest.event;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.rest.eventreservation.EventReservation;
import org.example.rest.hotel.Hotel;
import org.example.rest.security.user.AppUser;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"photos", "tags", "reviews", "eventReservations"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @NotNull(message = "Hotel is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @NotNull(message = "Creator is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private AppUser createdBy;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Category is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventCategory category;

    @ElementCollection
    @CollectionTable(name = "event_tags", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "tag", length = 100)
    private List<String> tags = new ArrayList<>();

    @NotNull(message = "Start date is required")
    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @NotNull(message = "End date is required")
    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @NotNull(message = "Location type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventLocationType locationType;

    @NotBlank(message = "Address is required")
    @Size(max = 500, message = "Address must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String address;

    @NotBlank(message = "City is required")
    @Size(max = 255, message = "City must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String city;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price must be 0 or greater")
    @Column(nullable = false)
    private Long price;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be a 3-letter ISO code e.g. USD")
    @Column(nullable = false, length = 3)
    private String currency = "USD";

    @NotNull(message = "Total capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    @Column(nullable = false)
    private Integer capacityTotal;

    @NotNull
    @Min(value = 1, message = "Max per user must be at least 1")
    @Column(nullable = false)
    private Integer maxPerUser = 1;

    @NotNull
    @Min(value = 0, message = "Booking cutoff must be 0 or greater")
    @Column(nullable = false)
    private Integer bookingCutoffMinutes = 60;

    @ElementCollection
    @CollectionTable(name = "event_photos", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "photo_url", length = 2048)
    private List<String> photos = new ArrayList<>();

    @Column(length = 2048)
    private String bannerImageUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EventStatus status = EventStatus.DRAFT;

    @NotNull
    @Column(nullable = false)
    private Boolean refundEnabled = false;

    @Min(value = 0,   message = "Refund percent must be between 0 and 100")
    @Max(value = 100, message = "Refund percent must be between 0 and 100")
    @Column
    private Integer refundPercent;

    @NotNull
    @Column(nullable = false)
    private Boolean requiresApproval = false;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<EventReservation> eventReservations = new ArrayList<>();
}