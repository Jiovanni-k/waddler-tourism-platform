package org.example.rest.contact;

import jakarta.mail.MessagingException;
import org.example.rest.PagedResponse;
import org.example.rest.notification.EmailService;
import org.example.rest.security.user.AppUser;
import org.example.rest.security.user.UserRepository;
import org.example.rest.security.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContactServiceImplTest {

    @Mock
    private ContactRepository repository;

    @Mock
    private ContactMapper mapper;

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContactServiceImpl service;

    private Contact contact;
    private ContactRequestDto requestDto;
    private ContactResponseDto.UserView userViewDto;
    private ContactResponseDto.AdminView adminViewDto;
    private AppUser user;
    private AppUser adminUser;

    @BeforeEach
    void setUp() {
        contact = Contact.builder()
                .id(1L)
                .userId(100L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .subject("Test Subject")
                .message("This is a test message with enough content")
                .category(ContactCategory.GENERAL_INQUIRY)
                .status(ContactStatus.NEW)
                .priority(ContactPriority.MEDIUM)
                .build();

        requestDto = new ContactRequestDto();
        requestDto.setSenderName("John Doe");
        requestDto.setSenderEmail("john@example.com");
        requestDto.setSubject("Test Subject");
        requestDto.setMessage("This is a test message with enough content");
        requestDto.setCategory(ContactCategory.GENERAL_INQUIRY);

        userViewDto = ContactResponseDto.UserView.builder()
                .subject("Test Subject")
                .message("This is a test message with enough content")
                .category(ContactCategory.GENERAL_INQUIRY)
                .status(ContactStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        adminViewDto = ContactResponseDto.AdminView.builder()
                .id(1L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .subject("Test Subject")
                .message("This is a test message with enough content")
                .category(ContactCategory.GENERAL_INQUIRY)
                .status(ContactStatus.NEW)
                .priority(ContactPriority.MEDIUM)
                .createdAt(LocalDateTime.now())
                .build();

        user = new AppUser();
        user.setId(100L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john@example.com");

        adminUser = new AppUser();
        adminUser.setId(200L);
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setEmail("admin@example.com");
        adminUser.setRole(UserRole.ADMIN);
    }

    @Test
    void testSubmit_AuthenticatedUser_Success() throws MessagingException {
        Long userId = 100L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toEntity(requestDto)).thenReturn(contact);
        when(repository.save(any(Contact.class))).thenReturn(contact);
        when(mapper.toUserView(contact)).thenReturn(userViewDto);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(List.of(adminUser));

        ContactResponseDto.UserView result = service.submit(requestDto, userId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(Contact.class));
        verify(emailService, times(1)).sendContactConfirmation(anyString(), anyString(), any(Contact.class));
        verify(emailService, times(1)).sendContactNotificationToAdmin(anyString(), anyString(), any(Contact.class));
    }

    @Test
    void testSubmit_GuestUser_Success() throws MessagingException {
        when(mapper.toEntity(requestDto)).thenReturn(contact);
        when(repository.save(any(Contact.class))).thenReturn(contact);
        when(mapper.toUserView(contact)).thenReturn(userViewDto);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(List.of(adminUser));

        ContactResponseDto.UserView result = service.submit(requestDto, null);

        assertNotNull(result);
        verify(repository, times(1)).save(any(Contact.class));
        verify(emailService, times(1)).sendContactConfirmation(anyString(), anyString(), any(Contact.class));
    }

    @Test
    void testSubmit_GuestUser_MissingName() {
        ContactRequestDto invalidDto = new ContactRequestDto();
        invalidDto.setSenderEmail("guest@example.com");
        invalidDto.setSubject("Subject");
        invalidDto.setMessage("This is a test message with enough content");

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(invalidDto, null));

        verify(repository, never()).save(any());
    }

    @Test
    void testSubmit_GuestUser_MissingEmail() {
        ContactRequestDto invalidDto = new ContactRequestDto();
        invalidDto.setSenderName("Guest User");
        invalidDto.setSubject("Subject");
        invalidDto.setMessage("This is a test message with enough content");

        assertThrows(IllegalArgumentException.class,
                () -> service.submit(invalidDto, null));

        verify(repository, never()).save(any());
    }

    @Test
    void testSubmit_UserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(mapper.toEntity(requestDto)).thenReturn(contact);
        when(repository.save(any(Contact.class))).thenReturn(contact);
        when(mapper.toUserView(contact)).thenReturn(userViewDto);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(List.of(adminUser));

        ContactResponseDto.UserView result = service.submit(requestDto, userId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(Contact.class));
    }

    @Test
    void testSubmit_EmailServiceFailure_DoesNotThrow() throws MessagingException {
        Long userId = 100L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.toEntity(requestDto)).thenReturn(contact);
        when(repository.save(any(Contact.class))).thenReturn(contact);
        when(mapper.toUserView(contact)).thenReturn(userViewDto);
        when(userRepository.findAllByRole(UserRole.ADMIN)).thenReturn(List.of(adminUser));
        doThrow(new MessagingException("Email service down")).when(emailService)
                .sendContactConfirmation(anyString(), anyString(), any(Contact.class));

        assertDoesNotThrow(() -> service.submit(requestDto, userId));
        verify(repository, times(1)).save(any(Contact.class));
    }


    @Test
    void testGetById_Success() {
        Long contactId = 1L;

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        ContactResponseDto.AdminView result = service.getById(contactId);

        assertNotNull(result);
        assertEquals(adminViewDto.getId(), result.getId());
        verify(repository, times(1)).findById(contactId);
    }

    @Test
    void testGetById_NotFound() {
        Long contactId = 999L;

        when(repository.findById(contactId)).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class,
                () -> service.getById(contactId));
    }


    @Test
    void testList_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_WithStatusFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(
                ContactStatus.NEW, null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testList_WithPriorityFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(
                null, ContactPriority.MEDIUM, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testList_WithCategoryFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(
                null, null, ContactCategory.GENERAL_INQUIRY, pageable);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void testList_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testList_WithMultipleFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Contact> page = new PageImpl<>(List.of(contact), pageable, 1);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(
                ContactStatus.NEW,
                ContactPriority.MEDIUM,
                ContactCategory.GENERAL_INQUIRY,
                pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testGetByUserId_Success() {
        Long userId = 100L;

        when(repository.findByUserId(userId)).thenReturn(List.of(contact));
        when(mapper.toUserView(contact)).thenReturn(userViewDto);

        List<ContactResponseDto.UserView> result = service.getByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(repository, times(1)).findByUserId(userId);
    }

    @Test
    void testGetByUserId_NoContacts() {
        Long userId = 999L;

        when(repository.findByUserId(userId)).thenReturn(List.of());

        List<ContactResponseDto.UserView> result = service.getByUserId(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetByUserId_MultipleContacts() {
        Long userId = 100L;
        Contact contact2 = Contact.builder()
                .id(2L)
                .userId(userId)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .subject("Another Subject")
                .message("This is another test message with enough content")
                .category(ContactCategory.FEEDBACK)
                .status(ContactStatus.IN_PROGRESS)
                .priority(ContactPriority.HIGH)
                .build();

        when(repository.findByUserId(userId)).thenReturn(List.of(contact, contact2));
        when(mapper.toUserView(contact)).thenReturn(userViewDto);
        when(mapper.toUserView(contact2)).thenReturn(userViewDto);

        List<ContactResponseDto.UserView> result = service.getByUserId(userId);

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetByUserId_Paginated() {
        Long userId = 100L;
        Pageable pageable = PageRequest.of(0, 5);
        Contact contact2 = Contact.builder()
                .id(2L)
                .userId(userId)
                .senderName("Jane Doe")
                .senderEmail("jane@example.com")
                .subject("Another Subject")
                .message("This is another test message with enough content")
                .category(ContactCategory.FEEDBACK)
                .status(ContactStatus.IN_PROGRESS)
                .priority(ContactPriority.HIGH)
                .build();

        Page<Contact> page = new PageImpl<>(List.of(contact, contact2), pageable, 2);

        when(repository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);
        when(mapper.toAdminView(contact2)).thenReturn(adminViewDto);

        PagedResponse<ContactResponseDto.AdminView> result = service.list(null, null, null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(5, result.getSize());
        verify(repository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testUpdate_Success() {
        Long contactId = 1L;
        ContactUpdateRequest updateRequest = new ContactUpdateRequest();
        updateRequest.setStatus(ContactStatus.IN_PROGRESS);
        updateRequest.setPriority(ContactPriority.HIGH);
        updateRequest.setAdminNotes("Looking into this issue");

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));
        when(repository.save(any(Contact.class))).thenReturn(contact);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        ContactResponseDto.AdminView result = service.update(contactId, updateRequest);

        assertNotNull(result);
        verify(repository, times(1)).save(any(Contact.class));
    }

    @Test
    void testUpdate_NotFound() {
        Long contactId = 999L;
        ContactUpdateRequest updateRequest = new ContactUpdateRequest();

        when(repository.findById(contactId)).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class,
                () -> service.update(contactId, updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdate_CannotSetResolvedStatus() {
        Long contactId = 1L;
        ContactUpdateRequest updateRequest = new ContactUpdateRequest();
        updateRequest.setStatus(ContactStatus.RESOLVED);

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(contactId, updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    void testUpdate_OnlyPriority() {
        Long contactId = 1L;
        ContactUpdateRequest updateRequest = new ContactUpdateRequest();
        updateRequest.setPriority(ContactPriority.LOW);

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));
        when(repository.save(any(Contact.class))).thenReturn(contact);
        when(mapper.toAdminView(contact)).thenReturn(adminViewDto);

        ContactResponseDto.AdminView result = service.update(contactId, updateRequest);

        assertNotNull(result);
        verify(repository, times(1)).save(any(Contact.class));
    }

    @Test
    void testUpdate_InvalidStatusTransition() {
        Long contactId = 1L;
        ContactUpdateRequest updateRequest = new ContactUpdateRequest();
        updateRequest.setStatus(ContactStatus.RESOLVED); // Cannot directly set to RESOLVED

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));

        assertThrows(IllegalArgumentException.class,
                () -> service.update(contactId, updateRequest));

        verify(repository, never()).save(any());
    }

    @Test
    void testResolve_Success() throws MessagingException {
        Long contactId = 1L;
        Long adminId = 200L;
        ContactResolveRequest resolveRequest = new ContactResolveRequest();
        resolveRequest.setResolutionMessage("Issue has been resolved");
        resolveRequest.setAdminNotes("Completed all necessary steps");
        resolveRequest.setPriority(ContactPriority.LOW);

        Contact resolvedContact = Contact.builder()
                .id(1L)
                .userId(100L)
                .senderName("John Doe")
                .senderEmail("john@example.com")
                .subject("Test Subject")
                .message("This is a test message with enough content")
                .category(ContactCategory.GENERAL_INQUIRY)
                .status(ContactStatus.RESOLVED)
                .priority(ContactPriority.LOW)
                .resolutionMessage("Issue has been resolved")
                .resolvedBy(adminId)
                .resolvedAt(LocalDateTime.now())
                .build();

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));
        when(repository.save(any(Contact.class))).thenReturn(resolvedContact);
        when(mapper.toAdminView(resolvedContact)).thenReturn(adminViewDto);

        ContactResponseDto.AdminView result = service.resolve(contactId, resolveRequest, adminId);

        assertNotNull(result);
        verify(repository, times(1)).save(any(Contact.class));
        verify(emailService, times(1)).sendContactResolution(anyString(), anyString(), any(Contact.class));
    }

    @Test
    void testResolve_ContactNotFound() {
        Long contactId = 999L;
        Long adminId = 200L;
        ContactResolveRequest resolveRequest = new ContactResolveRequest();
        resolveRequest.setResolutionMessage("Issue has been resolved");

        when(repository.findById(contactId)).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class,
                () -> service.resolve(contactId, resolveRequest, adminId));

        verify(repository, never()).save(any());
    }

    @Test
    void testResolve_AlreadyResolved() {
        Long contactId = 1L;
        Long adminId = 200L;

        Contact resolvedContact = Contact.builder()
                .id(1L)
                .status(ContactStatus.RESOLVED)
                .build();

        ContactResolveRequest resolveRequest = new ContactResolveRequest();
        resolveRequest.setResolutionMessage("Another resolution");

        when(repository.findById(contactId)).thenReturn(Optional.of(resolvedContact));

        assertThrows(IllegalArgumentException.class,
                () -> service.resolve(contactId, resolveRequest, adminId));

        verify(repository, never()).save(any());
    }

    @Test
    void testResolve_AlreadyClosed() {
        Long contactId = 1L;
        Long adminId = 200L;

        Contact closedContact = Contact.builder()
                .id(1L)
                .status(ContactStatus.CLOSED)
                .build();

        ContactResolveRequest resolveRequest = new ContactResolveRequest();
        resolveRequest.setResolutionMessage("Resolution message");

        when(repository.findById(contactId)).thenReturn(Optional.of(closedContact));

        assertThrows(IllegalArgumentException.class,
                () -> service.resolve(contactId, resolveRequest, adminId));

        verify(repository, never()).save(any());
    }

    @Test
    void testResolve_EmailFailure_DoesNotThrow() throws MessagingException {
        Long contactId = 1L;
        Long adminId = 200L;
        ContactResolveRequest resolveRequest = new ContactResolveRequest();
        resolveRequest.setResolutionMessage("Issue has been resolved");

        Contact resolvedContact = Contact.builder()
                .id(1L)
                .status(ContactStatus.RESOLVED)
                .resolvedBy(adminId)
                .resolvedAt(LocalDateTime.now())
                .senderEmail("john@example.com")
                .senderName("John Doe")
                .build();

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));
        when(repository.save(any(Contact.class))).thenReturn(resolvedContact);
        when(mapper.toAdminView(resolvedContact)).thenReturn(adminViewDto);
        doThrow(new MessagingException("Email service down")).when(emailService)
                .sendContactResolution(anyString(), anyString(), any(Contact.class));

        assertDoesNotThrow(() -> service.resolve(contactId, resolveRequest, adminId));
        verify(repository, times(1)).save(any(Contact.class));
    }

    @Test
    void testDelete_Success() {
        Long contactId = 1L;

        when(repository.findById(contactId)).thenReturn(Optional.of(contact));

        service.delete(contactId);

        verify(repository, times(1)).delete(any(Contact.class));
    }

    @Test
    void testDelete_NotFound() {
        Long contactId = 999L;

        when(repository.findById(contactId)).thenReturn(Optional.empty());

        assertThrows(ContactNotFoundException.class,
                () -> service.delete(contactId));

        verify(repository, never()).delete(any(Contact.class));
    }
}
