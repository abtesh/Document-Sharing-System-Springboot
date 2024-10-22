package com.LIB.MeesagingSystem.exceptions;

import com.LIB.MeesagingSystem.Dto.SecurityDtos.GenericResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AccountBlockedException.class)
    public ResponseEntity<GenericResponseDto<Void>> handleAccountBlockedException(AccountBlockedException ex) {
        return new ResponseEntity<>(new GenericResponseDto<>(false,403,ex.getMessage(),null), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponseDto<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return new ResponseEntity<>(new GenericResponseDto<>(false,403,ex.getMessage(),null), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GenericResponseDto<Void>> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        return new ResponseEntity<>(new GenericResponseDto<>(false,403,ex.getMessage(),null), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<GenericResponseDto<Void>> handleIllegalStateException(IllegalStateException ex) {
        return new ResponseEntity<>(new GenericResponseDto<>(false,429,ex.getMessage(),null), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<GenericResponseDto<Void>> handleAuthenticationException(AuthenticationException authenticationException){
        return new ResponseEntity<>(new GenericResponseDto<>(false,401,authenticationException.getMessage(),null), HttpStatus.UNAUTHORIZED);
    }
}
