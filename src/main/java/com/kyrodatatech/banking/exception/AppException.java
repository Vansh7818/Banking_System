package com.kyrodatatech.banking.exception;

import org.springframework.http.HttpStatus;

/**
 * ================================================================
 * AppException — Custom Application Exception
 * ================================================================
 *
 * This is our main custom exception class. Instead of throwing generic
 * Java exceptions (RuntimeException, IllegalArgumentException, etc.),
 * we throw AppException with a specific HTTP status code and message.
 *
 * WHY CUSTOM EXCEPTIONS?
 *   - Cleaner code — throw AppException instead of returning error objects
 *   - Consistent API — every error follows the same JSON format
 *   - The GlobalExceptionHandler catches this and returns proper HTTP response
 *
 * USAGE EXAMPLE (in a service class):
 *   if (user == null) {
 *       throw new AppException("User not found with ID: " + id, HttpStatus.NOT_FOUND);
 *   }
 *
 * This will automatically return:
 *   HTTP 404 Not Found
 *   {
 *     "status": 404,
 *     "error": "Not Found",
 *     "message": "User not found with ID: abc-123",
 *     "timestamp": "2024-09-15T09:47:00"
 *   }
 */
public class AppException extends RuntimeException {

    /**
     * The HTTP status code to return when this exception is thrown.
     * Example: HttpStatus.NOT_FOUND (404), HttpStatus.UNAUTHORIZED (401)
     */
    private final HttpStatus status;

    /**
     * Constructor with message and HTTP status.
     *
     * @param message Human-readable error description
     * @param status  HTTP status code to return
     */
    public AppException(String message, HttpStatus status) {
        super(message); // Pass message to parent RuntimeException
        this.status = status;
    }

    /**
     * Constructor with message, cause (for chaining), and HTTP status.
     *
     * @param message Human-readable error description
     * @param cause   The original exception that caused this
     * @param status  HTTP status code to return
     */
    public AppException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

    /**
     * Gets the HTTP status associated with this exception.
     *
     * @return HttpStatus (e.g., HttpStatus.NOT_FOUND)
     */
    public HttpStatus getStatus() {
        return status;
    }
}
