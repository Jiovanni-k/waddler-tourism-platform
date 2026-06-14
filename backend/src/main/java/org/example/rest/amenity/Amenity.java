package org.example.rest.amenity;

import jakarta.persistence.*;
import lombok.*;
import org.example.rest.hotel.Hotel;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "amenities")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String iconCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AmenityCategory category;

    @ManyToMany(mappedBy = "amenities")
    @Builder.Default
    private Set<Hotel> hotels = new HashSet<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}