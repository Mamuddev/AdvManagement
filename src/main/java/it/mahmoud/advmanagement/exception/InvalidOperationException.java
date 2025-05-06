package it.mahmoud.advmanagement.exception;

/**
 *  invalid operations exceptions
 */
public class InvalidOperationException extends ApiException {

    public InvalidOperationException(ApiErrorCode errorCode, String details) {
        super(errorCode, details);
    }

    public static InvalidOperationException categoryCircularReference(String categoryId, String parentId) {
        return new InvalidOperationException(
                ApiErrorCode.CATEGORY_CIRCULAR_REFERENCE,
                "Cannot set category " + parentId + " as parent of " + categoryId);
    }

    public static InvalidOperationException categoryHasSubcategories(String categoryId) {
        return new InvalidOperationException(
                ApiErrorCode.CATEGORY_HAS_SUBCATEGORIES,
                "Category " + categoryId + " has subcategories");
    }

    public static InvalidOperationException categoryHasAds(String categoryId) {
        return new InvalidOperationException(
                ApiErrorCode.CATEGORY_HAS_ADS,
                "Category " + categoryId + " has ads");
    }

    public static InvalidOperationException tagHasAds(String tagId) {
        return new InvalidOperationException(
                ApiErrorCode.TAG_HAS_ADS,
                "Tag " + tagId + " has ads");
    }

    public static InvalidOperationException adInvalidStatus(String adId, String currentStatus, String targetStatus) {
        return new InvalidOperationException(
                ApiErrorCode.AD_INVALID_STATUS,
                "Cannot transition ad " + adId + " from " + currentStatus + " to " + targetStatus);
    }

    public static InvalidOperationException passwordMismatch() {
        return new InvalidOperationException(
                ApiErrorCode.USER_PASSWORD_MISMATCH,
                "Password and confirm password do not match");
    }
}

