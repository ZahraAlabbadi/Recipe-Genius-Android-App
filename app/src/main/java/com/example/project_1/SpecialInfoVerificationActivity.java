package com.example.project_1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.project_1.Data.DatabaseHelper;

public class SpecialInfoVerificationActivity extends AppCompatActivity {

    private EditText etSpecialInfo;
    private Button btnVerifySpecialInfo;
    private DatabaseHelper db;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_info_verification);

        etSpecialInfo = findViewById(R.id.etSpecialInfo);
        btnVerifySpecialInfo = findViewById(R.id.btnVerifySpecialInfo);
        db = new DatabaseHelper(this);
        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the login page
                Intent intent = new Intent(SpecialInfoVerificationActivity.this, ForgetPasswordActivity.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });

        // Retrieve the email passed from ForgetPasswordActivity
        email = getIntent().getStringExtra("email");

        btnVerifySpecialInfo.setOnClickListener(v -> {
            String specialInfo = etSpecialInfo.getText().toString().trim();

            if (TextUtils.isEmpty(specialInfo)) {
                Toast.makeText(SpecialInfoVerificationActivity.this, "Enter your special info!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify special info
            boolean isSpecialInfoCorrect = db.checkSpecialInfo(email, specialInfo);
            if (isSpecialInfoCorrect) {
                // Redirect to ResetPasswordActivity
                Intent intent = new Intent(SpecialInfoVerificationActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SpecialInfoVerificationActivity.this, "Incorrect special info!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

