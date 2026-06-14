package org.example.rest.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    Optional<Inventory> findByRoomIdAndDate(Long roomId, LocalDate date);

    List<Inventory> findByRoomId(Long roomId);

    List<Inventory> findByRoomIdAndDateBetween(Long roomId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByRoomIdAndDate(Long roomId, LocalDate date);

    List<Inventory> findByRoomIdAndAvailableRoomsGreaterThan(Long roomId, int minAvailable);
}