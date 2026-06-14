package org.example.rest.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    List<Room> findByHotel_Id(Long hotelId);

    List<Room> findByHotel_IdAndActiveTrue(Long hotelId);

    boolean existsByHotel_IdAndName(Long hotelId, String name);

    boolean existsByHotel_IdAndRoomType(Long hotelId, RoomType roomType);

    boolean existsByCancellationPolicyId(Long cancellationPolicyId);
}