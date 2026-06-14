package org.example.rest.contact;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rest.PagedResponse;
import org.example.rest.notification.EmailService;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactServiceImpl implements ContactService {

    private final ContactRepository repository;
    private final ContactMapper mapper;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Override

    @Transactional
    public ContactResponseDto.UserView submit(ContactRequestDto dto, Long userId) {
        Contact contact = mapper.toEntity(dto);

        if (userId != null) {
            userRepository.findById(userId).ifPresent(user -> {
                contact.setUserId(userId);
                contact.setSenderName(user.getFirstName() + " " + user.getLastName());
                contact.setSenderEmail(user.getEmail());
            });
        } else {
            if (dto.getSenderName() == null || dto.getSenderName().isBlank())
                throw new IllegalArgumentException("Name is required for guest submissions");
            if (dto.getSenderEmail() == null || dto.getSenderEmail().isBlank())
                throw new IllegalArgumentException("Email is required for guest submissions");
        }

        Contact saved = repository.save(contact);
        log.info("Contact submitted: id={}, category={}, from={}",
                saved.getId(), saved.getCategory(), saved.getSenderEmail());

        try {
            emailService.sendContactConfirmation(saved.getSenderEmail(), saved.getSenderName(), saved);
        } catch (Exception e) {
            log.warn("Contact confirmation email could not be sent — {}", e.getMessage());
        }

        userRepository.findAllByRole(UserRole.ADMIN).forEach(admin -> {
            try {
                emailService.sendContactNotificationToAdmin(admin.getEmail(), admin.getFirstName(), saved);
            } catch (Exception e) {
                log.warn("Contact notification to admin could not be sent — {}", e.getMessage());
            }
        });

        return mapper.toUserView(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ContactResponseDto.AdminView getById(Long id) {
        return mapper.toAdminView(repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ContactResponseDto.AdminView> list(ContactStatus status,
                                                            ContactPriority priority,
                                                            ContactCategory category,
                                                            Pageable pageable) {
        Specification<Contact> spec = Specification
                .where(ContactSpecification.hasStatus(status))
                .and(ContactSpecification.hasPriority(priority))
                .and(ContactSpecification.hasCategory(category));

        Page<Contact> page = repository.findAll(spec, pageable);

        return new PagedResponse<>(
                page.map(mapper::toAdminView).getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContactResponseDto.UserView> getByUserId(Long userId) {
        return repository.findByUserId(userId)
                .stream().map(mapper::toUserView).toList();
    }

    @Override
    @Transactional
    public ContactResponseDto.AdminView update(Long id, ContactUpdateRequest request) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        if (Objects.equals(request.getStatus(), ContactStatus.RESOLVED))
            throw new IllegalArgumentException(
                    "To resolve a contact request, use the PATCH /contact/" + id + "/resolve endpoint. " +
                            "A resolution message is required.");

        if (Objects.nonNull(request.getStatus())) contact.setStatus(request.getStatus());
        if (Objects.nonNull(request.getPriority())) contact.setPriority(request.getPriority());
        if (Objects.nonNull(request.getAdminNotes())) contact.setAdminNotes(request.getAdminNotes());

        log.info("Contact id={} updated", id);
        return mapper.toAdminView(repository.save(contact));
    }

    @Override
    @Transactional
    public ContactResponseDto.AdminView resolve(Long id, ContactResolveRequest request, Long resolvedByUserId) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));

        if (Objects.equals(contact.getStatus(), ContactStatus.RESOLVED)
                || Objects.equals(contact.getStatus(), ContactStatus.CLOSED))
            throw new IllegalArgumentException(
                    "Contact request is already " + contact.getStatus().getDisplayName());

        contact.setStatus(ContactStatus.RESOLVED);
        contact.setResolutionMessage(request.getResolutionMessage());
        if (Objects.nonNull(request.getAdminNotes())) contact.setAdminNotes(request.getAdminNotes());
        if (Objects.nonNull(request.getPriority())) contact.setPriority(request.getPriority());
        contact.setResolvedBy(resolvedByUserId);
        contact.setResolvedAt(LocalDateTime.now());

        Contact saved = repository.save(contact);
        log.info("Contact id={} resolved by adminId={}", id, resolvedByUserId);

        try {
            emailService.sendContactResolution(saved.getSenderEmail(), saved.getSenderName(), saved);
        } catch (Exception e) {
            log.warn("Contact resolution email could not be sent — {}", e.getMessage());
        }

        return mapper.toAdminView(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Contact contact = repository.findById(id)
                .orElseThrow(() -> new ContactNotFoundException(id));
        repository.delete(contact);
        log.info("Contact id={} deleted", id);
    }
}