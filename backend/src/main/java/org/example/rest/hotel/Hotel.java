package org.example.rest.hotel;

import jakarta.persistence.*;
import lombok.*;
import org.example.rest.amenity.Amenity;
import org.example.rest.cancellationpolicy.CancellationPolicy;
import org.example.rest.room.Room;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"rooms", "amenities", "cancellationPolicies"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String historicalBackground;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String region;

    @Column(nullable = false)
    private Integer starRating;

    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal averageGuestRating = BigDecimal.ZERO;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 150)
    private String email;

    @Column(length = 255)
    private String websiteUrl;

    @Column(length = 500)
    private String coverImageUrl;

    @Column    private Double latitude;

    @Column    private Double longitude;

    @ElementCollection
    @CollectionTable(
            name = "hotel_gallery",
            joinColumns = @JoinColumn(name = "hotel_id")
    )
    @Column(name = "image_url", length = 500)
    @Builder.Default
    private List<String> galleryImageUrls = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HotelStatus status = HotelStatus.ACTIVE;

    @Column(name = "manager_id", nullable = false)
    private Long managerId;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "hotel_amenities",
            joinColumns = @JoinColumn(name = "hotel_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CancellationPolicy> cancellationPolicies = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    public void addRoom(Room room) {
        rooms.add(room);
        room.setHotel(this);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        room.setHotel(null);
    }

    public void addAmenity(Amenity amenity) {
        amenities.add(amenity);
        amenity.getHotels().add(this);
    }

    public void removeAmenity(Amenity amenity) {
        amenities.remove(amenity);
        amenity.getHotels().remove(this);
    }
}