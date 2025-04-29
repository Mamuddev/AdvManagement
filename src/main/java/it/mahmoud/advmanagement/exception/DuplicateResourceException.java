package it.mahmoud.advmanagement.exception;

/**
 * Eccezione per risorse duplicate
 */
public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(ApiErrorCode errorCode, String resourceIdentifier) {
        super(errorCode, "Resource identifier: " + resourceIdentifier);
    }

    public static DuplicateResourceException user(String email) {
        return new DuplicateResourceException(ApiErrorCode.USER_ALREADY_EXISTS, email);
    }

    public static DuplicateResourceException category(String name) {
        return new DuplicateResourceException(ApiErrorCode.CATEGORY_ALREADY_EXISTS, name);
    }

    public static DuplicateResourceException tag(String name) {
        return new DuplicateResourceException(ApiErrorCode.TAG_ALREADY_EXISTS, name);
    }
}