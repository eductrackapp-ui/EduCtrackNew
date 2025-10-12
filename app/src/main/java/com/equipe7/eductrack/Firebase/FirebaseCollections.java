package com.equipe7.eductrack.Firebase;

/**
 * Centralized Firestore collection and field names to avoid typos.
 */
public final class FirebaseCollections {

    private FirebaseCollections() {}

    // Collections
    public static final String USERS = "users";
    public static final String ADMINS = "admins";
    public static final String OTPS = "otps";
    public static final String RATE_LIMITS = "rate_limits";
    public static final String STUDENTS = "students";

    // Common fields
    public static final String FIELD_EMAIL = "email";
    public static final String FIELD_ROLE = "role";
    public static final String FIELD_CREATED_AT = "createdAt";

    // OTP fields
    public static final String FIELD_CODE = "code";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_USED = "used";
    public static final String FIELD_EXPIRES_AT = "expiresAt";

    // Rate limit fields
    public static final String FIELD_LAST_REQUEST = "lastRequest";
    public static final String FIELD_ATTEMPTS = "attempts";
}


