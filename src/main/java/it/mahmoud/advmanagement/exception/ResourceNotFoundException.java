package it.mahmoud.advmanagement.exception;

/**
 * Eccezione per risorse non trovate
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(ApiErrorCode errorCode, String resourceId) {
        super(errorCode, "Resource ID: " + resourceId);
    }

    public static ResourceNotFoundException user(String userId) {
        return new ResourceNotFoundException(ApiErrorCode.USER_NOT_FOUND, userId);
    }

    public static ResourceNotFoundException ad(String adId) {
        return new ResourceNotFoundException(ApiErrorCode.AD_NOT_FOUND, adId);
    }

    public static ResourceNotFoundException category(String categoryId) {
        return new ResourceNotFoundException(ApiErrorCode.CATEGORY_NOT_FOUND, categoryId);
    }

    public static ResourceNotFoundException tag(String tagId) {
        return new ResourceNotFoundException(ApiErrorCode.TAG_NOT_FOUND, tagId);
    }
}
