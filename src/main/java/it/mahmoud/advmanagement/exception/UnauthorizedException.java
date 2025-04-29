package it.mahmoud.advmanagement.exception;

/**
 * Eccezione per accessi non autorizzati
 */
public class UnauthorizedException extends ApiException {

    public UnauthorizedException(String details) {
        super(ApiErrorCode.UNAUTHORIZED, details);
    }

    public static UnauthorizedException adAccess(String adId, String userId) {
        return new UnauthorizedException("User " + userId + " cannot modify ad " + adId);
    }

    public static UnauthorizedException invalidCredentials() {
        return new UnauthorizedException("Invalid username or password");
    }
}