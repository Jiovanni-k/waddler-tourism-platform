package org.example.rest.amenity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AmenityService {
    AmenityResponseDto.DetailResponse createAmenity(AmenityRequestDto dto);
    AmenityResponseDto.DetailResponse updateAmenity(Long id, AmenityRequestDto dto);
    void deleteAmenity(Long id);
    AmenityResponseDto.DetailResponse getAmenityById(Long id);
    Page<AmenityResponseDto.Response> listAmenities(Pageable pageable);
    List<AmenityResponseDto.Response> getAllAmenities();
}