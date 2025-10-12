package com.equipe7.eductrack.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.equipe7.eductrack.Auth.ActivityParentRegister;
import com.equipe7.eductrack.Auth.CreateAdminAccountActivity;
import com.equipe7.eductrack.Auth.LoginActivity;
import com.equipe7.eductrack.R;

public class RoleSelectionActivity extends AppCompatActivity {

    private LinearLayout adminLayout, parentLayout;
    private TextView tvAlreadyHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // Initialisation des vues
        adminLayout = findViewById(R.id.adminLayout);
        parentLayout = findViewById(R.id.parentLayout);
        tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);

        // Click sur Admin → CreateAdminAccountActivity
        adminLayout.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, CreateAdminAccountActivity.class);
            startActivity(intent);
        });

        // Click sur Parent → ActivityParentRegister
        parentLayout.setOnClickListener(v -> {
            Intent intent = new Intent(RoleSelectionActivity.this, ActivityParentRegister.class);
            startActivity(intent);
        });

        // Click sur "Already have an account? Log in" → LoginActivity
        String fullText = "Already have an account? Log in";
        SpannableString spannableString = new SpannableString(fullText);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View widget) {
                Intent intent = new Intent(RoleSelectionActivity.this, LoginActivity.class);
                startActivity(intent);
            }

            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.parseColor("#5E17EB")); // couleur du lien
                ds.setUnderlineText(true); // souligné
            }
        };

        // "Log in" commence à l'index 25
        spannableString.setSpan(clickableSpan, 25, fullText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvAlreadyHaveAccount.setText(spannableString);
        tvAlreadyHaveAccount.setMovementMethod(LinkMovementMethod.getInstance());
        tvAlreadyHaveAccount.setHighlightColor(Color.TRANSPARENT);
    }
}
