package org.example.rest.cancellationpolicy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.hotel.Hotel;
import org.example.rest.hotel.HotelNotFoundException;
import org.example.rest.hotel.HotelRepository;
import org.example.rest.room.RoomRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancellationPolicyServiceImpl implements CancellationPolicyService {

    private final CancellationPolicyRepository repository;
    private final CancellationPolicyMapper mapper;
    private final HotelRepository hotelRepository;
    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public CancellationPolicyResponseDto create(Long hotelId, Long managerId, CancellationPolicyRequestDto dto) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));

        verifyOwnership(hotel, managerId);

        if (repository.findByHotel_IdAndName(hotelId, dto.getName()).isPresent())
            throw new DuplicateCancellationPolicyException(dto.getName().getDisplayName(), hotelId);

        CancellationPolicy policy = mapper.toEntity(dto, hotel);
        CancellationPolicy saved = repository.save(policy);
        log.info("Created cancellation policy id={} name={} for hotelId={} by managerId={}",
                saved.getId(), saved.getName(), hotelId, managerId);
        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CancellationPolicyResponseDto getById(Long hotelId, Long id) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        CancellationPolicy policy = repository.findById(id)
                .orElseThrow(() -> new CancellationPolicyNotFoundException(id));

        if (!policy.getHotel().getId().equals(hotelId))
            throw new CancellationPolicyNotBelongToHotelException(id, hotelId);

        return mapper.toResponseDto(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CancellationPolicyResponseDto> list(
            Long hotelId,
            CancellationPolicyName name,
            Integer minDays,
            Integer maxDays,
            BigDecimal minRefund,
            BigDecimal maxRefund,
            Pageable pageable) {

        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        if (minDays != null && maxDays != null && minDays > maxDays)
            throw new IllegalArgumentException("minDays must not be greater than maxDays");
        if (minRefund != null && maxRefund != null && minRefund.compareTo(maxRefund) > 0)
            throw new IllegalArgumentException("minRefund must not be greater than maxRefund");

        Specification<CancellationPolicy> spec = Specification
                .where(CancellationPolicySpecification.hasHotelId(hotelId))
                .and(CancellationPolicySpecification.hasName(name))
                .and(CancellationPolicySpecification.minDaysBeforeCheckin(minDays))
                .and(CancellationPolicySpecification.maxDaysBeforeCheckin(maxDays))
                .and(CancellationPolicySpecification.minRefundPercentage(minRefund))
                .and(CancellationPolicySpecification.maxRefundPercentage(maxRefund));

        Page<CancellationPolicy> page = repository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(mapper::toResponseDto).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public CancellationPolicyResponseDto update(Long hotelId, Long id, Long managerId,
                                                CancellationPolicyRequestDto dto) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        CancellationPolicy policy = repository.findById(id)
                .orElseThrow(() -> new CancellationPolicyNotFoundException(id));

        if (!policy.getHotel().getId().equals(hotelId))
            throw new CancellationPolicyNotBelongToHotelException(id, hotelId);

        verifyOwnership(policy.getHotel(), managerId);

        if (!policy.getName().equals(dto.getName()) &&
                repository.findByHotel_IdAndName(hotelId, dto.getName()).isPresent())
            throw new DuplicateCancellationPolicyException(dto.getName().getDisplayName(), hotelId);

        policy.setName(dto.getName());
        policy.setDescription(dto.getDescription());
        policy.setDaysBeforeCheckin(dto.getDaysBeforeCheckin());
        policy.setRefundPercentage(dto.getRefundPercentage());

        log.info("Updated cancellation policy id={} for hotelId={} by managerId={}", id, hotelId, managerId);
        return mapper.toResponseDto(repository.save(policy));
    }

    @Override
    @Transactional
    public void delete(Long hotelId, Long id, Long managerId) {
        if (!hotelRepository.existsById(hotelId))
            throw new HotelNotFoundException(hotelId);

        CancellationPolicy policy = repository.findById(id)
                .orElseThrow(() -> new CancellationPolicyNotFoundException(id));

        if (!policy.getHotel().getId().equals(hotelId))
            throw new CancellationPolicyNotBelongToHotelException(id, hotelId);

        verifyOwnership(policy.getHotel(), managerId);

        if (roomRepository.existsByCancellationPolicyId(id))
            throw new IllegalStateException(
                    "Cannot delete cancellation policy id=" + id +
                            " — it is still assigned to one or more rooms. " +
                            "Please reassign or remove it from all rooms first.");

        repository.delete(policy);
        log.info("Deleted cancellation policy id={} for hotelId={} by managerId={}", id, hotelId, managerId);
    }

    private void verifyOwnership(Hotel hotel, Long managerId) {
        String role = SecurityUtil.getCurrentUserRole();
        if ("ADMIN".equals(role)) return;

        if (!hotel.getManagerId().equals(managerId)) {
            throw new AccessDeniedException(
                    "You do not have permission to manage policies for hotel id=" + hotel.getId());
        }
    }
}