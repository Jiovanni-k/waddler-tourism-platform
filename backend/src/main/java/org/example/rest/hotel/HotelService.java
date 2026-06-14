package org.example.rest.hotel;

import org.example.rest.amenity.AmenityDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface HotelService {

    HotelResponseDto.DetailResponse createHotel(HotelRequestDto.CreateRequest request, Long managerId);

    HotelResponseDto.DetailResponse updateHotel(Long hotelId, HotelRequestDto.UpdateRequest request, Long managerId);

    void deleteHotel(Long hotelId, Long managerId);

    HotelResponseDto.DetailResponse getHotelById(Long hotelId);

    Page<HotelResponseDto.SummaryResponse> listHotels(String city, Integer minStars,
                                                      BigDecimal minRating, Pageable pageable);

    Page<HotelResponseDto.SummaryResponse> searchHotels(String query, Pageable pageable);

    List<HotelResponseDto.SummaryResponse> getHotelsByManager(Long managerId);

    void refreshAverageRating(Long hotelId);

    HotelResponseDto.DetailResponse approveHotel(Long hotelId);

    HotelResponseDto.DetailResponse suspendHotel(Long hotelId);

    HotelResponseDto.DetailResponse reactivateHotel(Long hotelId);

    List<HotelResponseDto.SummaryResponse> getPendingHotels();

    List<AmenityDto> getHotelAmenities(Long hotelId);

    void addPhotoToHotel(Long hotelId, String photoUrl, Long managerId);
    void validateSortBy(String sortBy);
    void validateSortDir(String sortDir);

    DistanceResponse getDistanceToHotel(Long hotelId, Double userLat, Double userLng);

    List<HotelResponseDto.SummaryResponse> getNearbyHotels(Double userLat, Double userLng, Double radiusKm);
}