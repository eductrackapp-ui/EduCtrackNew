package com.equipe7.eductrack.Firebase;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EnhancedEmailOTPService {

    private static final String TAG = "EnhancedEmailOTPService";

    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes
    private static final int MAX_ATTEMPTS_PER_EMAIL = 3;
    private static final long RATE_LIMIT_WINDOW = 60 * 1000; // 1 minute

    private final Context context;
    private final FirebaseFirestore firestore;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final SecureRandom secureRandom;

    public EnhancedEmailOTPService(Context context) {
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
        this.executorService = Executors.newCachedThreadPool();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.secureRandom = new SecureRandom();
    }

    /** Génère un OTP à 6 chiffres */
    public String generateOTP() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /** Envoi d’un OTP avec gestion rate limit */
    public void sendOTP(String email, String userName, OTPCallback callback) {
        checkRateLimit(email, new RateLimitCallback() {
            @Override
            public void onAllowed() {
                String otpCode = generateOTP();
                storeOTPInFirestore(email, otpCode, new FirestoreCallback() {
                    @Override
                    public void onSuccess() {
                        sendEmailAsync(email, userName, otpCode, callback);
                    }
                    @Override
                    public void onFailure(String error) {
                        callback.onFailure("Échec stockage OTP : " + error);
                    }
                });
            }
            @Override
            public void onRateLimited(long waitTime) {
                callback.onFailure("Trop de tentatives. Attendez " + (waitTime / 1000) + " sec.");
            }
        });
    }

    /** Vérifie l’OTP fourni */
    public void verifyOTP(String email, String inputCode, OTPCallback callback) {
        firestore.collection("otps").document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        callback.onFailure("Code non trouvé ou expiré");
                        return;
                    }
                    String storedCode = doc.getString("code");
                    Long timestamp = doc.getLong("timestamp");
                    Boolean used = doc.getBoolean("used");

                    if (storedCode == null || !storedCode.equals(inputCode)) {
                        callback.onFailure("Code incorrect");
                        return;
                    }
                    if (used != null && used) {
                        callback.onFailure("Ce code a déjà été utilisé");
                        return;
                    }
                    if (timestamp == null || isOTPExpired(timestamp)) {
                        callback.onFailure("Le code a expiré");
                        return;
                    }

                    doc.getReference().update("used", true)
                            .addOnSuccessListener(aVoid -> callback.onSuccess("Code vérifié avec succès"))
                            .addOnFailureListener(e -> callback.onFailure("Erreur lors de la validation"));
                })
                .addOnFailureListener(e -> callback.onFailure("Erreur vérification OTP"));
    }

    /** Vérifie la limite d’envoi par email */
    private void checkRateLimit(String email, RateLimitCallback callback) {
        firestore.collection("rate_limits").document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    long currentTime = System.currentTimeMillis();
                    if (!doc.exists()) {
                        createRateLimitRecord(email, currentTime);
                        callback.onAllowed();
                        return;
                    }

                    Long lastRequest = doc.getLong("lastRequest");
                    Long attempts = doc.getLong("attempts");
                    if (lastRequest == null) lastRequest = 0L;
                    if (attempts == null) attempts = 0L;

                    if (currentTime - lastRequest > RATE_LIMIT_WINDOW) attempts = 0L;

                    if (attempts >= MAX_ATTEMPTS_PER_EMAIL) {
                        long waitTime = RATE_LIMIT_WINDOW - (currentTime - lastRequest);
                        if (waitTime > 0) {
                            callback.onRateLimited(waitTime);
                            return;
                        }
                        attempts = 0L;
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastRequest", currentTime);
                    updates.put("attempts", attempts + 1);
                    doc.getReference().update(updates);
                    callback.onAllowed();
                })
                .addOnFailureListener(e -> callback.onAllowed());
    }

    private void createRateLimitRecord(String email, long currentTime) {
        Map<String, Object> rateLimitData = new HashMap<>();
        rateLimitData.put("email", email);
        rateLimitData.put("lastRequest", currentTime);
        rateLimitData.put("attempts", 1);
        firestore.collection("rate_limits").document(email).set(rateLimitData);
    }

    /** Stocke l’OTP dans Firestore */
    private void storeOTPInFirestore(String email, String otpCode, FirestoreCallback callback) {
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("code", otpCode);
        otpData.put("timestamp", System.currentTimeMillis());
        otpData.put("used", false);
        otpData.put("email", email);
        otpData.put("expiresAt", System.currentTimeMillis() + OTP_EXPIRY_TIME);

        firestore.collection("otps").document(email)
                .set(otpData)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    /** Envoi d’email via EmailJS API (asynchrone) */
    private void sendEmailAsync(String toEmail, String userName, String otpCode, OTPCallback callback) {
        executorService.execute(() -> {
            try {
                JSONObject emailData = new JSONObject();
                emailData.put("service_id", "service_yvl11d5");
                emailData.put("template_id", "template_zlp263e");
                emailData.put("user_id", "Un7snKzeE4AGeorc-");

                JSONObject templateParams = new JSONObject();
                templateParams.put("to_email", toEmail);
                templateParams.put("to_name", userName != null ? userName : "User");
                templateParams.put("verification_code", otpCode);
                templateParams.put("subject", "Votre code de vérification EduTrack");
                templateParams.put("message", "Bonjour " + userName + ",\n\nVotre code de vérification est : " + otpCode);

                emailData.put("template_params", templateParams);

                RequestBody body = RequestBody.create(emailData.toString(), MediaType.get("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url("https://api.emailjs.com/api/v1.0/email/send")
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                OkHttpClient client = new OkHttpClient();
                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        mainHandler.post(() -> callback.onFailure("Erreur réseau: " + e.getMessage()));
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        final boolean success = response.isSuccessful();
                        mainHandler.post(() -> {
                            if (success) callback.onSuccess("Code envoyé avec succès");
                            else callback.onFailure("Erreur envoi OTP: " + response.code());
                        });
                        response.close();
                    }
                });
            } catch (JSONException e) {
                mainHandler.post(() -> callback.onFailure("Erreur configuration email: " + e.getMessage()));
            }
        });
    }

    /** Vérifie expiration OTP */
    public boolean isOTPExpired(long timestamp) {
        return (System.currentTimeMillis() - timestamp) > OTP_EXPIRY_TIME;
    }

    /** Nettoyage des OTP expirés */
    public void cleanupExpiredOTPs() {
        long now = System.currentTimeMillis();
        firestore.collection("otps").whereLessThan("expiresAt", now)
                .get()
                .addOnSuccessListener(query -> {
                    for (var doc : query.getDocuments()) doc.getReference().delete();
                });
    }

    /** Arrêt du service */
    public void shutdown() {
        if (executorService != null && !executorService.isShutdown()) executorService.shutdown();
    }

    /** Interfaces callback */
    public interface OTPCallback { void onSuccess(String message); void onFailure(String error); }
    private interface FirestoreCallback { void onSuccess(); void onFailure(String error); }
    private interface RateLimitCallback { void onAllowed(); void onRateLimited(long waitTime); }
}
