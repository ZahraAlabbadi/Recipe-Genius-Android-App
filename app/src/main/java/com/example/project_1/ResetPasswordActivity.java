package com.example.project_1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.project_1.Data.DatabaseHelper;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText etEmail, etNewPassword, etConfirmPassword;
    private Button btnResetPassword;
    private DatabaseHelper db;
    private String emailFromForgetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        etEmail = findViewById(R.id.etEmail);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetPasswordActivity.this, SpecialInfoVerificationActivity.class);
                startActivity(intent);
                finish();
            }
        });

        db = new DatabaseHelper(this);

        // Retrieve the email passed from ForgetPasswordActivity
        emailFromForgetPassword = getIntent().getStringExtra("email");

        if (emailFromForgetPassword != null) {
            etEmail.setText(emailFromForgetPassword);
            etEmail.setEnabled(false);
        }

        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!email.equals(emailFromForgetPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Email does not match the one from the previous step!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(ResetPasswordActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isStrongPassword(newPassword)) {
                showWeakPasswordDialog();
                return;
            }

            boolean isUpdated = db.updatePassword(email, newPassword);
            if (isUpdated) {
                Toast.makeText(ResetPasswordActivity.this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Failed to reset password. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isStrongPassword(String password) {
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[@#$%^&+=!].*");
    }

    private void showWeakPasswordDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Weak Password")
                .setMessage("Your password must have:\n- At least 8 characters\n- Uppercase and lowercase letters\n- Numbers\n- Special characters")
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
