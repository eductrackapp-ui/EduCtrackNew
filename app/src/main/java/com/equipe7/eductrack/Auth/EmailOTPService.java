package com.equipe7.eductrack.Auth;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Service combiné OTP (EmailJS + Firebase)
 */
public class EmailOTPService {

    private static final String TAG = "EmailOTPService";

    // === Configuration EmailJS ===
    private static final String EMAILJS_URL = "https://api.emailjs.com/api/v1.0/email/send";
    private static final String SERVICE_ID = "service_yvl11d5";
    private static final String TEMPLATE_ID = "template_zlp263e";
    private static final String PUBLIC_KEY = "Un7snKzeE4AGeorc-";

    private final OkHttpClient client;
    private final Handler mainHandler;
    private final FirebaseAuth firebaseAuth;

    public EmailOTPService(Context context) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    // === Génération OTP aléatoire ===
    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // === Vérifier format OTP ===
    public boolean isValidOTPFormat(String otp) {
        return otp != null && otp.matches("\\d{6}");
    }

    // === Vérifier expiration OTP (5 min) ===
    public boolean isOTPExpired(long otpTimestamp) {
        long currentTime = System.currentTimeMillis();
        long expirationTime = 5 * 60 * 1000; // 5 min
        return (currentTime - otpTimestamp) > expirationTime;
    }

    // === Envoi OTP par EmailJS ===
    public void sendOTPEmail(String toEmail, String userName, String otpCode, OTPCallback callback) {
        try {
            JSONObject emailData = new JSONObject();
            emailData.put("service_id", SERVICE_ID);
            emailData.put("template_id", TEMPLATE_ID);
            emailData.put("user_id", PUBLIC_KEY);

            JSONObject templateParams = new JSONObject();
            templateParams.put("to_email", toEmail);
            templateParams.put("to_name", userName != null ? userName : "User");
            templateParams.put("otp_code", otpCode);

            emailData.put("template_params", templateParams);

            RequestBody body = RequestBody.create(
                    emailData.toString(),
                    MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(EMAILJS_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Erreur réseau OTP", e);
                    mainHandler.post(() -> {
                        if (callback != null) callback.onFailure("Erreur réseau: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.d(TAG, "EmailJS Response: " + responseBody);
                    mainHandler.post(() -> {
                        if (response.isSuccessful()) {
                            if (callback != null) callback.onSuccess("OTP envoyé par email");
                        } else {
                            if (callback != null) callback.onFailure("Erreur EmailJS: " + response.code());
                        }
                    });
                }
            });

        } catch (JSONException e) {
            if (callback != null) callback.onFailure("Erreur JSON: " + e.getMessage());
        }
    }

    // === Envoi OTP par SMS Firebase ===
    public void sendOTPSMS(Activity activity, String phoneNumber, PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks) {
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber(phoneNumber) // numéro +250...
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    // === Vérifier un OTP SMS Firebase ===
    public PhoneAuthCredential verifyOTPSMS(String verificationId, String otpCode) {
        return PhoneAuthProvider.getCredential(verificationId, otpCode);
    }

    public void sendOTP(String email, String fullName, String generatedOTP, OTPCallback otpCallback) {
    }

    /**
     * Interface callback OTP
     */
    public interface OTPCallback {
        void onSuccess(String message);
        void onFailure(String error);
    }
}
