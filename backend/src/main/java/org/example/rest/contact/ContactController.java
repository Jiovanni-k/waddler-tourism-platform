package org.example.rest.contact;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.rest.PagedResponse;
import org.example.rest.security.SecurityUtil;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService service;

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("createdAt", "updatedAt", "status", "priority", "category");

    @PostMapping
    public ResponseEntity<ContactResponseDto.UserView> submit(
            @Valid @RequestBody ContactRequestDto dto
    ) {
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(dto, userId));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('VIEW_CONTACT')")
    public ResponseEntity<List<ContactResponseDto.UserView>> getMyRequests() {
        Long userId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(userId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.getByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_CONTACT')")
    public ResponseEntity<PagedResponse<ContactResponseDto.AdminView>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        if (status != null && status.isBlank())
            throw new IllegalArgumentException("Parameter 'status' must not be blank");
        if (priority != null && priority.isBlank())
            throw new IllegalArgumentException("Parameter 'priority' must not be blank");
        if (category != null && category.isBlank())
            throw new IllegalArgumentException("Parameter 'category' must not be blank");

        if (sortBy.isBlank())
            throw new IllegalArgumentException("Parameter 'sortBy' must not be blank");
        if (!ALLOWED_SORT_FIELDS.contains(sortBy.trim()))
            throw new IllegalArgumentException("Invalid sortBy value: " + sortBy);
        if (sortDir.isBlank())
            throw new IllegalArgumentException("Parameter 'sortDir' must not be blank");
        if (!sortDir.trim().equalsIgnoreCase("asc") && !sortDir.trim().equalsIgnoreCase("desc"))
            throw new IllegalArgumentException("Invalid sortDir value: " + sortDir);

        ContactStatus contactStatus = null;
        ContactPriority contactPriority = null;
        ContactCategory contactCategory = null;

        try {
            if (status != null) contactStatus = ContactStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for parameter 'status': " + status);
        }
        try {
            if (priority != null) contactPriority = ContactPriority.valueOf(priority.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for parameter 'priority': " + priority);
        }
        try {
            if (category != null) contactCategory = ContactCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid value for parameter 'category': " + category);
        }

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(service.list(contactStatus, contactPriority, contactCategory, pageable));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CONTACT')")
    public ResponseEntity<ContactResponseDto.AdminView> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CONTACT')")
    public ResponseEntity<ContactResponseDto.AdminView> update(
            @PathVariable Long id,
            @RequestBody ContactUpdateRequest request
    ) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAuthority('MANAGE_CONTACT')")
    public ResponseEntity<ContactResponseDto.AdminView> resolve(
            @PathVariable Long id,
            @Valid @RequestBody ContactResolveRequest request
    ) {
        Long adminId = SecurityUtil.getCurrentUserId();
        if (Objects.isNull(adminId)) throw new IllegalArgumentException("Not authenticated");
        return ResponseEntity.ok(service.resolve(id, request, adminId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_CONTACT')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}