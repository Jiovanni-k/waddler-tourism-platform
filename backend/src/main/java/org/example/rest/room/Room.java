package org.example.rest.room;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.rest.amenity.Amenity;
import org.example.rest.booking.Booking;
import org.example.rest.hotel.Hotel;
import org.example.rest.inventory.Inventory;
import org.example.rest.pricingrule.PricingRule;
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
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"hotel", "amenities", "inventories", "pricingRules", "bookings"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(name = "cancellation_policy_id")
    private Long cancellationPolicyId;

    @Column(length = 100)
    private String name;

    @NotNull(message = "Room type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 30)
    private RoomType roomType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Column(nullable = false)
    private Integer totalRooms;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 50)
    private String bedType;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToMany
    @JoinTable(
            name = "room_amenities",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    @Builder.Default
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PricingRule> pricingRules = new ArrayList<>();

    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public void addAmenity(Amenity amenity) {
        amenities.add(amenity);
    }

    public void removeAmenity(Amenity amenity) {
        amenities.remove(amenity);
    }

    public void addInventory(Inventory inventory) {
        inventories.add(inventory);
        inventory.setRoom(this);
    }

    public void removeInventory(Inventory inventory) {
        inventories.remove(inventory);
        inventory.setRoom(null);
    }

    public void addPricingRule(PricingRule rule) {
        pricingRules.add(rule);
        rule.setRoom(this);
    }

    public void removePricingRule(PricingRule rule) {
        pricingRules.remove(rule);
        rule.setRoom(null);
    }
}