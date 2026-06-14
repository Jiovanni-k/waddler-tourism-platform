package org.example.rest.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private AuthEntryPointJwt authEntryPointJwt;
    private ByteArrayOutputStream outputStream;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authEntryPointJwt = new AuthEntryPointJwt();
        outputStream = new ByteArrayOutputStream();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCommence_WithBadCredentialsException_ReturnsUnauthorized() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Invalid credentials");
        when(request.getServletPath()).thenReturn("/auth/login");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String responseBody = outputStream.toString();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
        assertTrue(responseBody.contains("\"message\":\"Invalid credentials\""));
    }

    @Test
    void testCommence_WithUsernameNotFoundException_ReturnsUnauthorized() throws IOException {

        AuthenticationException exception = new UsernameNotFoundException("User not found");
        when(request.getServletPath()).thenReturn("/api/hotels");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"status\":401"));
        assertTrue(responseBody.contains("\"message\":\"User not found\""));
    }

    @Test
    void testCommence_WithDisabledException_ReturnsUnauthorized() throws IOException {

        AuthenticationException exception = new DisabledException("Account is disabled");
        when(request.getServletPath()).thenReturn("/api/bookings");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"message\":\"Account is disabled\""));
    }

    @Test
    void testCommence_SetsContentTypeToJSON() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Test error");
        when(request.getServletPath()).thenReturn("/test");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testCommence_ContentTypeIsApplicationJson() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Error");
        when(request.getServletPath()).thenReturn("/path");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
    }

    @Test
    void testCommence_SetsStatusTo401Unauthorized() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Unauthorized");
        when(request.getServletPath()).thenReturn("/auth");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).setStatus(401);
    }

    @Test
    void testCommence_StatusIsAlways401() throws IOException {

        AuthenticationException exception = new UsernameNotFoundException("Not found");
        when(request.getServletPath()).thenReturn("/anything");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response).setStatus(401);
    }

    @Test
    void testCommence_ResponseBodyContainsStatus() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Error");
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"status\""));
        assertTrue(responseBody.contains("401"));
    }

    @Test
    void testCommence_ResponseBodyContainsError() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Credentials invalid");
        when(request.getServletPath()).thenReturn("/api/test");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"error\""));
        assertTrue(responseBody.contains("Unauthorized"));
    }

    @Test
    void testCommence_ResponseBodyContainsMessage() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Invalid token");
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"message\""));
        assertTrue(responseBody.contains("Invalid token"));
    }

    @Test
    void testCommence_ResponseBodyContainsPath() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Error");
        when(request.getServletPath()).thenReturn("/auth/login");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains("\"path\""));
        assertTrue(responseBody.contains("/auth/login"));
    }

    @Test
    void testCommence_ExceptionMessageIsPreserved() throws IOException {

        String exceptionMessage = "Custom error message for testing";
        AuthenticationException exception = new BadCredentialsException(exceptionMessage);
        when(request.getServletPath()).thenReturn("/test");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains(exceptionMessage));
    }

    @Test
    void testCommence_DifferentExceptionMessages() throws IOException {

        String[] messages = {
                "Invalid credentials",
                "Token expired",
                "User not found",
                "Account suspended"
        };

        for (String message : messages) {
            outputStream.reset();
            AuthenticationException exception = new BadCredentialsException(message);
            when(request.getServletPath()).thenReturn("/api");
            when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

            authEntryPointJwt.commence(request, response, exception);

            String responseBody = outputStream.toString();
            assertTrue(responseBody.contains(message),
                    "Response should contain message: " + message);
        }
    }

    @Test
    void testCommence_IncludesCorrectServletPath() throws IOException {
        AuthenticationException exception = new BadCredentialsException("Error");
        String testPath = "/auth/login";
        when(request.getServletPath()).thenReturn(testPath);
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains(testPath));
    }

    @Test
    void testCommence_DifferentPaths() throws IOException {

        String[] paths = {"/auth/login", "/api/hotels", "/api/bookings", "/auth/refresh"};

        for (String path : paths) {
            outputStream.reset();
            AuthenticationException exception = new BadCredentialsException("Error");
            when(request.getServletPath()).thenReturn(path);
            when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

            authEntryPointJwt.commence(request, response, exception);

            String responseBody = outputStream.toString();
            assertTrue(responseBody.contains(path),
                    "Response should contain path: " + path);
        }
    }

    @Test
    void testCommence_ResponseIsValidJSON() throws IOException {

        AuthenticationException exception = new BadCredentialsException("Error");
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        try {
            Map response = objectMapper.readValue(responseBody, Map.class);
            assertNotNull(response);
            assertTrue(response.containsKey("status"));
            assertTrue(response.containsKey("error"));
            assertTrue(response.containsKey("message"));
            assertTrue(response.containsKey("path"));
        } catch (Exception e) {
            fail("Response is not valid JSON: " + e.getMessage());
        }
    }

    @Test
    void testCommence_ResponseBodyCanBeParsed() throws IOException {
        AuthenticationException exception = new BadCredentialsException("Parse test");
        when(request.getServletPath()).thenReturn("/test/path");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        Map<String, Object> parsedResponse = objectMapper.readValue(responseBody, Map.class);

        assertEquals(401, parsedResponse.get("status"));
        assertEquals("Unauthorized", parsedResponse.get("error"));
        assertEquals("Parse test", parsedResponse.get("message"));
        assertEquals("/test/path", parsedResponse.get("path"));
    }

    @Test
    void testCommence_WithDifferentExceptionTypes() throws IOException {
        AuthenticationException[] exceptions = {
                new BadCredentialsException("Bad credentials"),
                new UsernameNotFoundException("User not found"),
                new DisabledException("Account disabled")
        };

        for (AuthenticationException exception : exceptions) {
            outputStream.reset();
            when(request.getServletPath()).thenReturn("/api");
            when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

            authEntryPointJwt.commence(request, response, exception);

            String responseBody = outputStream.toString();
            assertNotNull(responseBody);
            assertTrue(responseBody.contains("\"status\":401"));
            assertTrue(responseBody.contains("\"error\":\"Unauthorized\""));
        }
    }

    @Test
    void testCommence_VerifiesAllResponseMethodsCalled() throws IOException {
        AuthenticationException exception = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response, times(1)).getOutputStream();
    }

    @Test
    void testCommence_DoesNotThrowException() throws IOException {
        AuthenticationException exception = new BadCredentialsException("Test");
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        assertDoesNotThrow(() ->
                authEntryPointJwt.commence(request, response, exception)
        );
    }

    @Test
    void testCommence_WithNullExceptionMessage() throws IOException {
        AuthenticationException exception = new BadCredentialsException(null);
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertNotNull(responseBody);
        assertTrue(responseBody.contains("\"status\":401"));
    }

    @Test
    void testCommence_WithEmptyExceptionMessage() throws IOException {
        AuthenticationException exception = new BadCredentialsException("");
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testCommence_WithSpecialCharactersInMessage() throws IOException {
        String specialMessage = "Error: Invalid credentials! @#$%^&*()";
        AuthenticationException exception = new BadCredentialsException(specialMessage);
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));

        authEntryPointJwt.commence(request, response, exception);

        verify(response, times(1)).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testCommence_WithLongExceptionMessage() throws IOException {
        String longMessage = "This is a very long error message that explains in detail why the authentication failed: " +
                "The provided credentials do not match any known user in the system database and therefore the " +
                "authentication process has been terminated with this detailed error message.";
        AuthenticationException exception = new BadCredentialsException(longMessage);
        when(request.getServletPath()).thenReturn("/api");
        when(response.getOutputStream()).thenReturn(new DummyServletOutputStream(outputStream));
        authEntryPointJwt.commence(request, response, exception);

        String responseBody = outputStream.toString();
        assertTrue(responseBody.contains(longMessage));
    }

    private static class DummyServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final ByteArrayOutputStream baos;

        public DummyServletOutputStream(ByteArrayOutputStream baos) {
            this.baos = baos;
        }

        @Override
        public void write(int b) throws IOException {
            baos.write(b);
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener listener) {
            // Not needed for testing
        }
    }
}