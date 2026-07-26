package com.doodle.challenge.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({
        UserNotFoundException.class,
        TimeSlotNotFoundException.class,
        MeetingNotFoundException.class,
        ParticipantNotFoundException.class
    })
    public ProblemDetail handleNotFound(DomainException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
        SlotOverlapException.class,
        SlotNotFreeException.class,
        SlotHasMeetingException.class,
        DuplicateParticipantException.class
    })
    public ProblemDetail handleConflict(DomainException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(MeetingAccessDeniedException.class)
    public ProblemDetail handleForbidden(MeetingAccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(InvalidTimeRangeException.class)
    public ProblemDetail handleUnprocessable(InvalidTimeRangeException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, detail.isBlank() ? "Validation failed" : detail);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ProblemDetail handleMissingHeader(MissingRequestHeaderException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadable(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Malformed request body");
    }

    // 504: the query/transaction itself timed out, distinct from 503's "couldn't even start" below
    @ExceptionHandler({QueryTimeoutException.class, TransactionTimedOutException.class})
    public ProblemDetail handleOperationTimeout(Exception ex) {
        log.warn("Operation exceeded its configured timeout", ex);
        return problem(HttpStatus.GATEWAY_TIMEOUT, "The request took too long to process and was aborted");
    }

    // most commonly the Hikari connection pool timing out under load - safe to retry
    @ExceptionHandler(TransientDataAccessException.class)
    public ProblemDetail handleTransientDataAccess(TransientDataAccessException ex) {
        log.warn("Transient data access failure (e.g. connection pool exhausted)", ex);
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "The service is temporarily unable to handle the request; please retry");
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error handling request", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        return ProblemDetail.forStatusAndDetail(status, detail);
    }
}
