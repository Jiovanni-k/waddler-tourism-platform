package org.example.rest.contact;

import org.example.rest.PagedResponse;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ContactService {

    ContactResponseDto.UserView submit(ContactRequestDto dto, Long userId);

    ContactResponseDto.AdminView getById(Long id);

    PagedResponse<ContactResponseDto.AdminView> list(ContactStatus status,
                                                     ContactPriority priority,
                                                     ContactCategory category,
                                                     Pageable pageable);

    List<ContactResponseDto.UserView> getByUserId(Long userId);

    ContactResponseDto.AdminView update(Long id, ContactUpdateRequest request);

    ContactResponseDto.AdminView resolve(Long id, ContactResolveRequest request, Long resolvedByUserId);

    void delete(Long id);
}