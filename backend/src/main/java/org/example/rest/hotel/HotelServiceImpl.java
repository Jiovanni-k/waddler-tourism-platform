package org.example.rest.hotel;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.amenity.AmenityDto;
import org.example.rest.amenity.AmenityMapper;
import org.example.rest.amenity.AmenityRepository;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HotelServiceImpl implements HotelService {

    public void validateSortBy(String sortBy) {
        if (!StringUtils.hasText(sortBy))
            throw new IllegalArgumentException("Parameter 'sortBy' must not be empty or blank");
        if (!HotelConfigService.getInstance().getAllowedSortFields().contains(sortBy.trim()))
            throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
    }

    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;

    @Override
    public HotelResponseDto.DetailResponse createHotel(HotelRequestDto.CreateRequest request, Long managerId) {
        Hotel hotel = HotelMapper.toEntity(request);
        hotel.setManagerId(managerId);
        hotel.setStatus(HotelStatus.PENDING_APPROVAL);

        if (Objects.nonNull(request.getAmenityIds())) {
            amenityRepository.findAllById(request.getAmenityIds())
                    .forEach(hotel::addAmenity);
        }

        Hotel saved = hotelRepository.save(hotel);
        log.info("Hotel created: id={}, name={}, managerId={}, status=PENDING_APPROVAL",
                saved.getId(), saved.getName(), managerId);
        return HotelMapper.toDetailResponse(saved);
    }

    @Override
    public HotelResponseDto.DetailResponse updateHotel(Long hotelId,
                                                       HotelRequestDto.UpdateRequest request,
                                                       Long managerId) {
        Hotel hotel = findAndVerifyOwnerOrAdmin(hotelId, managerId);
        HotelMapper.applyUpdate(request, hotel);

        if (Objects.nonNull(request.getAmenityIds())) {
            hotel.getAmenities().clear();
            amenityRepository.findAllById(request.getAmenityIds())
                    .forEach(hotel::addAmenity);
        }

        log.info("Hotel updated: id={}, updatedBy={}", hotelId, managerId);
        return HotelMapper.toDetailResponse(hotelRepository.save(hotel));
    }

    @Override
    public void deleteHotel(Long hotelId, Long managerId) {
        Hotel hotel = findAndVerifyOwnerOrAdmin(hotelId, managerId);
        hotel.setStatus(HotelStatus.INACTIVE);
        hotelRepository.save(hotel);
        log.info("Hotel soft-deleted: id={}, deletedBy={}", hotelId, managerId);
    }

    @Override
    @Transactional
    public HotelResponseDto.DetailResponse getHotelById(Long hotelId) {
        return HotelMapper.toDetailResponse(findById(hotelId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponseDto.SummaryResponse> listHotels(String city,
                                                             Integer minStars,
                                                             BigDecimal minRating,
                                                             Pageable pageable) {
        validateCity(city);
        validateMinStars(minStars);
        validateMinRating(minRating);

        Specification<Hotel> spec = Specification
                .where(HotelSpecifications.hasStatus(HotelStatus.ACTIVE))
                .and(HotelSpecifications.hasCity(normalizeNullable(city)))
                .and(HotelSpecifications.hasMinStars(minStars))
                .and(HotelSpecifications.hasMinRating(minRating));

        return hotelRepository.findAll(spec, pageable)
                .map(HotelMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponseDto.SummaryResponse> searchHotels(String query, Pageable pageable) {
        validateRequiredText(query, "q");

        Specification<Hotel> spec = Specification
                .where(HotelSpecifications.hasStatus(HotelStatus.ACTIVE))
                .and(HotelSpecifications.searchByNameOrCity(query.trim()));

        return hotelRepository.findAll(spec, pageable)
                .map(HotelMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public List<HotelResponseDto.SummaryResponse> getHotelsByManager(Long managerId) {
        return hotelRepository.findByManagerId(managerId)
                .stream().map(HotelMapper::toSummaryResponse).toList();
    }

    @Override
    public HotelResponseDto.DetailResponse approveHotel(Long hotelId) {
        Hotel hotel = findById(hotelId);
        if (!Objects.equals(hotel.getStatus(), HotelStatus.PENDING_APPROVAL)) {
            throw new IllegalArgumentException(
                    "Cannot approve hotel " + hotelId + " because current status is " + hotel.getStatus());
        }
        hotel.setStatus(HotelStatus.ACTIVE);
        log.info("Hotel approved by admin: id={}, managerId={}", hotelId, hotel.getManagerId());
        return HotelMapper.toDetailResponse(hotelRepository.save(hotel));
    }

    @Override
    public HotelResponseDto.DetailResponse reactivateHotel(Long hotelId) {
        Hotel hotel = findById(hotelId);
        if (!Objects.equals(hotel.getStatus(), HotelStatus.SUSPENDED)) {
            throw new IllegalArgumentException("Only SUSPENDED hotels can be reactivated");
        }
        hotel.setStatus(HotelStatus.ACTIVE);
        log.info("Reactivated hotel id={}", hotelId);
        return HotelMapper.toDetailResponse(hotelRepository.save(hotel));
    }

    @Override
    public HotelResponseDto.DetailResponse suspendHotel(Long hotelId) {
        Hotel hotel = findById(hotelId);
        hotel.setStatus(HotelStatus.SUSPENDED);
        log.info("Hotel suspended by admin: id={}, managerId={}", hotelId, hotel.getManagerId());
        return HotelMapper.toDetailResponse(hotelRepository.save(hotel));
    }

    @Override
    @Transactional
    public List<HotelResponseDto.SummaryResponse> getPendingHotels() {
        return hotelRepository.findAllByStatus(HotelStatus.PENDING_APPROVAL)
                .stream().map(HotelMapper::toSummaryResponse).toList();
    }

    @Override
    public void refreshAverageRating(Long hotelId) {
        log.debug("refreshAverageRating called for hotelId={}", hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityDto> getHotelAmenities(Long hotelId) {
        Hotel hotel = findById(hotelId);
        return hotel.getAmenities().stream()
                .map(AmenityMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public void addPhotoToHotel(Long hotelId, String photoUrl, Long managerId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
        findAndVerifyOwnerOrAdmin(hotelId, managerId);
        hotel.getGalleryImageUrls().add(photoUrl);
        hotelRepository.save(hotel);
    }

    private Hotel findById(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
    }

    private Hotel findAndVerifyOwnerOrAdmin(Long hotelId, Long callerId) {
        Hotel hotel = findById(hotelId);
        String role = SecurityUtil.getCurrentUserRole();
        boolean isAdmin = "ADMIN".equals(role);
        if (!isAdmin && !Objects.equals(hotel.getManagerId(), callerId)) {
            log.warn("Access denied: userId={} tried to modify hotel={} owned by managerId={}",
                    callerId, hotelId, hotel.getManagerId());
            throw new AccessDeniedException("You do not manage hotel " + hotelId);
        }
        return hotel;
    }

    private String normalizeNullable(String value) {
        return value == null ? null : value.trim();
    }


    public void validateSortDir(String sortDir) {
        if (!StringUtils.hasText(sortDir)) {
            throw new IllegalArgumentException("Parameter 'sortDir' must not be empty or blank");
        }
        String normalized = sortDir.trim().toLowerCase();
        if (!normalized.equals("asc") && !normalized.equals("desc")) {
            throw new IllegalArgumentException("Invalid sortDir value: " + sortDir);
        }
    }

    private void validateCity(String city) {
        if (city != null && city.isBlank()) {
            throw new IllegalArgumentException("Parameter 'city' must not be blank");
        }
    }

    private void validateMinStars(Integer minStars) {
        if (minStars != null && (minStars < 1 || minStars > 5)) {
            throw new IllegalArgumentException("Parameter 'minStars' must be between 1 and 5");
        }
    }

    private void validateMinRating(BigDecimal minRating) {
        if (minRating != null &&
                (minRating.compareTo(BigDecimal.ZERO) < 0 || minRating.compareTo(BigDecimal.valueOf(5)) > 0)) {
            throw new IllegalArgumentException("Parameter 'minRating' must be between 0 and 5");
        }
    }

    private void validateRequiredText(String value, String paramName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Parameter '" + paramName + "' must not be empty or blank");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DistanceResponse getDistanceToHotel(Long hotelId, Double userLat, Double userLng) {
        Hotel hotel = findById(hotelId);

        if (hotel.getLatitude() == null || hotel.getLongitude() == null)
            throw new IllegalArgumentException("Hotel location is not set yet.");

        double distance = haversineKm(userLat, userLng, hotel.getLatitude(), hotel.getLongitude());

        return DistanceResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .city(hotel.getCity())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .distanceKm(Math.round(distance * 100.0) / 100.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelResponseDto.SummaryResponse> getNearbyHotels(Double userLat, Double userLng, Double radiusKm) {
        return hotelRepository.findAll().stream()
                .filter(h -> h.getStatus() == HotelStatus.ACTIVE)
                .filter(h -> h.getLatitude() != null && h.getLongitude() != null)
                .filter(h -> haversineKm(userLat, userLng, h.getLatitude(), h.getLongitude()) <= radiusKm)
                .sorted((a, b) -> Double.compare(
                        haversineKm(userLat, userLng, a.getLatitude(), a.getLongitude()),
                        haversineKm(userLat, userLng, b.getLatitude(), b.getLongitude())))
                .map(HotelMapper::toSummaryResponse)
                .toList();
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}