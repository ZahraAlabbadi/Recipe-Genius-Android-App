package com.example.project_1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.example.project_1.Data.DatabaseHelper;
import com.example.project_1.Model.User;

public class register extends AppCompatActivity {

    DatabaseHelper db;
    EditText editTextUsername, editTextEmail, editTextPassword, editTextCnfPassword;
    Button buttonRegister;
    EditText editTextSpecialInfo;

    TextView textViewLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editTextSpecialInfo = findViewById(R.id.editTextSpecialInfo);

        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate back to the login page
                Intent intent = new Intent(register.this, LogIn.class);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });

        db = new DatabaseHelper(this);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextCnfPassword = findViewById(R.id.editTextCnfPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        textViewLogin = findViewById(R.id.textViewLogin);
        textViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(register.this, LogIn.class));
            }
        });

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName = editTextUsername.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String passwordConf = editTextCnfPassword.getText().toString().trim();
                String specialInfo = editTextSpecialInfo.getText().toString().trim(); // Get special info

                // Validate input fields
                if (userName.isEmpty() || email.isEmpty() || password.isEmpty() || specialInfo.isEmpty()) {
                    Toast.makeText(register.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution
                }

                // Validate email format
                if (!isValidEmail(email)) {
                    new android.app.AlertDialog.Builder(register.this)
                            .setTitle("Invalid Email")
                            .setMessage("Please enter a valid email address (e.g., user@example.com).")
                            .setPositiveButton("OK", null)
                            .show();
                    return; // Stop further execution
                }

                // Check for strong password
                if (!isStrongPassword(password)) {
                    new android.app.AlertDialog.Builder(register.this)
                            .setTitle("Weak Password")
                            .setMessage("Your password must contain at least 8 characters, including a mix of letters, numbers, and special characters.")
                            .setPositiveButton("OK", null)
                            .show();
                    return; // Stop further execution
                }

                if (password.equals(passwordConf)) {
                    User user = new User();
                    user.setUserName(userName);
                    user.setEmail(email);
                    user.setPassword(password);
                    user.setSpecialInfo(specialInfo); // Set special info

                    long val = db.addUser(user);
                    if (val > 0) {
                        Toast.makeText(register.this, "You have registered", Toast.LENGTH_SHORT).show();
                        Intent moveToLogin = new Intent(register.this, LogIn.class);
                        startActivity(moveToLogin);
                    } else {
                        Toast.makeText(register.this, "Registration Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Method to validate password strength
    private boolean isStrongPassword(String password) {
        String passwordPattern = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return password.matches(passwordPattern);
    }

    // Method to validate email format
    private boolean isValidEmail(String email) {
        String emailPattern = "^[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+$";
        return email.matches(emailPattern);
    }
}
