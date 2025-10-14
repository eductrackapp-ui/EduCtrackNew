package com.equipe7.eductrack.Auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Activity.AdminHomeActivityNew;
import com.equipe7.eductrack.Firebase.FirebaseManager;
import com.equipe7.eductrack.R;

import java.util.HashMap;

public class VerificationCodeActivity extends AppCompatActivity {

    private EditText[] otpEditTexts;
    private Button btnVerify, btnResendCode;
    private TextView tvTimer, tvEmail;
    private ProgressBar progressBar;

    private String email;
    private String username;
    private boolean fromLogin;
    private CountDownTimer countDownTimer;
    private boolean isTimerRunning = false;
    private boolean isLoading = false;

    private FirebaseManager firebaseManager;
    private EmailOTPService otpService;

    // Gestion limite d'envoi
    private int resendCount = 0;
    private long nextAvailableTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification_code);

        firebaseManager = FirebaseManager.getInstance();
        otpService = new EmailOTPService(this);

        email = getIntent().getStringExtra("email");
        username = getIntent().getStringExtra("username");
        fromLogin = getIntent().getBooleanExtra("fromLogin", false);

        initializeViews();
        setupOTPInputs();
        setupClickListeners();
        startTimer();
    }

    private void initializeViews() {
        otpEditTexts = new EditText[6];
        otpEditTexts[0] = findViewById(R.id.etOtp1);
        otpEditTexts[1] = findViewById(R.id.etOtp2);
        otpEditTexts[2] = findViewById(R.id.etOtp3);
        otpEditTexts[3] = findViewById(R.id.etOtp4);
        otpEditTexts[4] = findViewById(R.id.etOtp5);
        otpEditTexts[5] = findViewById(R.id.etOtp6);

        btnVerify = findViewById(R.id.btnVerify);
        btnResendCode = findViewById(R.id.btnResendCode);
        tvTimer = findViewById(R.id.tvTimer);
        tvEmail = findViewById(R.id.tvEmail);
        progressBar = findViewById(R.id.progressBar);

        tvEmail.setText("Code envoyé à " + email);
    }

    private void setupOTPInputs() {
        for (int i = 0; i < otpEditTexts.length; i++) {
            final int index = i;
            otpEditTexts[i].addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (otpEditTexts[index].getText().length() == 1 && index < otpEditTexts.length - 1) {
                        otpEditTexts[index + 1].requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void setupClickListeners() {
        btnVerify.setOnClickListener(v -> { if (!isLoading) verifyOTP(); });
        btnResendCode.setOnClickListener(v -> { if (!isTimerRunning) handleResend(); });
    }

    private void verifyOTP() {
        String code = getOTPCode();
        if (code.length() != 6) {
            showError("Veuillez entrer le code complet");
            return;
        }

        setLoadingState(true);

        firebaseManager.getFirestore()
                .collection("otps")
                .document(email)
                .get()
                .addOnSuccessListener(doc -> {
                    setLoadingState(false);
                    if (!doc.exists()) {
                        showError("Code invalide ou expiré");
                        return;
                    }

                    String storedCode = doc.getString("code");
                    Long timestamp = doc.getLong("timestamp");
                    Boolean used = doc.getBoolean("used");

                    if (storedCode != null && storedCode.equals(code)) {
                        if (used != null && used) { showError("Ce code a déjà été utilisé"); return; }
                        if (timestamp != null && otpService.isOTPExpired(timestamp)) { showError("Le code a expiré"); return; }

                        doc.getReference().update("used", true);
                        Toast.makeText(this, "Code vérifié avec succès !", Toast.LENGTH_SHORT).show();
                        if (fromLogin) goToHomePage(); else finish();

                    } else { showError("Code incorrect"); }
                })
                .addOnFailureListener(e -> { setLoadingState(false); showError("Erreur : " + e.getMessage()); });
    }

    private void handleResend() {
        long now = System.currentTimeMillis();
        if (now < nextAvailableTime) {
            long remaining = (nextAvailableTime - now) / 1000;
            showError("Veuillez attendre " + remaining + "s avant de renvoyer un code");
            return;
        }

        resendOTP();
        resendCount++;

        if (resendCount == 5) nextAvailableTime = System.currentTimeMillis() + 3 * 60 * 1000; // 3 min
        if (resendCount == 10) nextAvailableTime = System.currentTimeMillis() + 10 * 60 * 1000; // 10 min
    }

    private void resendOTP() {
        setLoadingState(true);
        String newOtp = otpService.generateOTP(); // 6 chiffres

        firebaseManager.getFirestore()
                .collection("otps")
                .document(email)
                .set(new HashMap<String, Object>() {{
                    put("code", newOtp);
                    put("timestamp", System.currentTimeMillis());
                    put("used", false);
                }})
                .addOnSuccessListener(aVoid -> otpService.sendOTP(email, username, newOtp, new EmailOTPService.OTPCallback() {
                    @Override
                    public void onSuccess(String message) {
                        setLoadingState(false);
                        Toast.makeText(VerificationCodeActivity.this, "Nouveau code envoyé", Toast.LENGTH_SHORT).show();
                        clearOTPFields();
                        startTimer();
                    }
                    @Override
                    public void onFailure(String error) { setLoadingState(false); showError("Erreur envoi OTP : " + error); }
                }))
                .addOnFailureListener(e -> { setLoadingState(false); showError("Erreur stockage OTP : " + e.getMessage()); });
    }

    private void startTimer() {
        isTimerRunning = true;
        btnResendCode.setEnabled(false);
        btnResendCode.setAlpha(0.5f);

        countDownTimer = new CountDownTimer(60000, 1000) { // 60s entre chaque envoi
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimer.setText("Renvoyer le code dans " + millisUntilFinished / 1000 + "s");
            }
            @Override
            public void onFinish() {
                isTimerRunning = false;
                btnResendCode.setEnabled(true);
                btnResendCode.setAlpha(1f);
                tvTimer.setText("Vous n'avez pas reçu le code ?");
            }
        }.start();
    }

    private String getOTPCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText et : otpEditTexts) sb.append(et.getText().toString().trim());
        return sb.toString();
    }

    private void clearOTPFields() {
        for (EditText et : otpEditTexts) et.setText("");
        otpEditTexts[0].requestFocus();
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!loading);
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        for (EditText et : otpEditTexts) et.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake));
    }

    private void goToHomePage() {
        Intent intent = new Intent(this, AdminHomeActivityNew.class);
        startActivity(intent);
        finish();
    }
}
