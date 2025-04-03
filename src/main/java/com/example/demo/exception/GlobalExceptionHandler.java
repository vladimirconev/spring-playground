package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private final DefaultErrorAttributes errorAttributes;

  public GlobalExceptionHandler(final DefaultErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }

  protected ResponseEntity<ErrorResponse> handleException(
      final Throwable throwable,
      final HttpStatus status,
      final WebRequest request,
      final String message) {
    var httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    var errorResponse = buildErrorResponse(status, request, throwable, message);
    return new ResponseEntity<>(errorResponse, httpHeaders, status);
  }

  protected ErrorResponse buildErrorResponse(
      final HttpStatus httpStatus,
      final WebRequest webRequest,
      final Throwable throwable,
      final String messageDetails) {
    var servletWebRequest = (ServletWebRequest) webRequest;
    var httpServletRequest = servletWebRequest.getNativeRequest(HttpServletRequest.class);

    Objects.requireNonNull(httpServletRequest, "Http servlet request should not be null.");

    var errors = errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
    var status = httpStatus.getReasonPhrase();
    var code = httpStatus.value();
    if (httpStatus == HttpStatus.INTERNAL_SERVER_ERROR) {
      status = HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase();
      code = HttpStatus.SERVICE_UNAVAILABLE.value();
    }

    var message =
        Optional.ofNullable(messageDetails)
            .orElse(Optional.ofNullable(errors.get("message")).map(Object::toString).orElse(""));
    var path =
        Optional.ofNullable(errors.get("path"))
            .map(Object::toString)
            .orElse(httpServletRequest.getRequestURI());
    return new ErrorResponse(
        status,
        code,
        message,
        path,
        httpServletRequest.getMethod(),
        throwable.getClass().getSimpleName(),
        Instant.now());
  }

  @ResponseBody
  @ExceptionHandler(NoSuchElementException.class)
  protected ResponseEntity<ErrorResponse> handleNoSuchElementException(
      final NoSuchElementException ex, final WebRequest webRequest) {
    return handleException(ex, HttpStatus.NOT_FOUND, webRequest, ex.getMessage());
  }

  @ResponseBody
  @ExceptionHandler(MethodArgumentNotValidException.class)
  protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      final MethodArgumentNotValidException ex, final WebRequest webRequest) {
    var fieldErrors = ex.getFieldErrors();
    var message =
        fieldErrors.stream()
            .map(
                fieldError ->
                    "Validation failed for field '%s' with rejected value '%s' due to: '%s'"
                        .formatted(
                            fieldError.getField(),
                            fieldError.getRejectedValue(),
                            fieldError.getDefaultMessage()))
            .collect(Collectors.joining(System.lineSeparator()));
    var detailedMessage = Optional.of(message).orElse(ex.getMessage());
    return handleException(ex, HttpStatus.BAD_REQUEST, webRequest, detailedMessage);
  }
}
