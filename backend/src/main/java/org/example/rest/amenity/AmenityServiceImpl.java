package org.example.rest.amenity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    @Override
    public AmenityResponseDto.DetailResponse createAmenity(AmenityRequestDto dto) {
        if (amenityRepository.existsByName(dto.getName())) {
            throw new DuplicateAmenityException();
        }

        Amenity amenity = AmenityMapper.toEntity(dto);
        Amenity saved = amenityRepository.save(amenity);
        log.info("Amenity created: id={}, name={}", saved.getId(), saved.getName());
        return AmenityMapper.toDetailResponse(saved);
    }

    @Override
    public AmenityResponseDto.DetailResponse updateAmenity(Long id, AmenityRequestDto dto) {
        Amenity amenity = findById(id);

        if (!amenity.getName().equals(dto.getName()) && amenityRepository.existsByName(dto.getName())) {
            throw new DuplicateAmenityException();
        }

        AmenityMapper.applyUpdate(dto, amenity);
        Amenity updated = amenityRepository.save(amenity);
        log.info("Amenity updated: id={}, name={}", updated.getId(), updated.getName());
        return AmenityMapper.toDetailResponse(updated);
    }

    @Override
    public void deleteAmenity(Long id) {
        Amenity amenity = findById(id);
        amenityRepository.delete(amenity);
        log.info("Amenity deleted: id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public AmenityResponseDto.DetailResponse getAmenityById(Long id) {
        return AmenityMapper.toDetailResponse(findById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AmenityResponseDto.Response> listAmenities(Pageable pageable) {
        return amenityRepository.findAll(pageable)
                .map(AmenityMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AmenityResponseDto.Response> getAllAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(AmenityMapper::toResponse)
                .toList();
    }

    private Amenity findById(Long id) {
        return amenityRepository.findById(id)
                .orElseThrow(() -> new AmenityNotFoundException(id));
    }
}