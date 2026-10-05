package edu.cibertec.products.controller;

import edu.cibertec.products.dto.ProductRequest;
import edu.cibertec.products.exception.DuplicateResourceException;
import edu.cibertec.products.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldHandleKnownErrors() throws Exception {
        assertThat(handler.handleNotFound(new ResourceNotFoundException("missing"), request()).getStatus()).isEqualTo(404);
        assertThat(handler.handleDuplicate(new DuplicateResourceException("duplicate"), request()).getStatus()).isEqualTo(409);
        assertThat(handler.handleBadRequest(new IllegalArgumentException("bad"), request()).getStatus()).isEqualTo(400);
        assertThat(handler.handleDataIntegrity(request()).getStatus()).isEqualTo(409);
        assertThat(handler.handleTypeMismatch(new MethodArgumentTypeMismatchException(
                "abc", Integer.class, "page", null, null), request()).getStatus()).isEqualTo(400);
        assertThat(handler.handleMalformedJson(new HttpMessageNotReadableException("invalid", (Throwable) null), request())
                .getStatus()).isEqualTo(400);
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("request", "name", "must not be blank")));
        Method method = ProductController.class.getMethod("create", ProductRequest.class);
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                new MethodParameter(method, 0), bindingResult);

        ProblemDetail result = handler.handleValidation(exception, request());

        assertThat(result.getStatus()).isEqualTo(400);
        assertThat(result.getProperties()).containsKey("fieldErrors");
    }

    private MockHttpServletRequest request() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/products");
        return request;
    }
}
