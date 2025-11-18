package com.example.appointable;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private Button btnSignIn;
    private TextView tvSignUp, tvForgotPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI elements
        btnSignIn = findViewById(R.id.btnSignIn);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvSignUp = findViewById(R.id.tvSignUp);

        // -------------------------------------
        // SIGN IN BUTTON → Firebase login
        // -------------------------------------
        btnSignIn.setOnClickListener(v -> loginUser());

        // -------------------------------------
        // FORGOT PASSWORD → ForgotPasswordActivity
        // -------------------------------------
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // -------------------------------------
        // CLICKABLE "SIGN UP" text only
        // -------------------------------------
        String text = "Still don’t have an account? Sign Up";
        SpannableString spannableString = new SpannableString(text);

        int startIndex = text.indexOf("Sign Up");
        int endIndex = startIndex + "Sign Up".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(Color.BLUE), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvSignUp.setText(spannableString);
        tvSignUp.setMovementMethod(LinkMovementMethod.getInstance());
        tvSignUp.setHighlightColor(Color.TRANSPARENT);
    }

    // -----------------------------------------------------------
    // LOGIN USER WITH FIREBASE
    // -----------------------------------------------------------
    private void loginUser() {
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        checkUserRoleAndRedirect(user.getUid());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    // -----------------------------------------------------------
    // CHECK USER ROLE IN FIRESTORE AND REDIRECT
    // -----------------------------------------------------------
    private void checkUserRoleAndRedirect(String uid) {

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "User data not found.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String role = doc.getString("role");

                    if (role == null) {
                        Toast.makeText(this, "Role not found for this account.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    if (role.equals("Parent")) {
                        startActivity(new Intent(this, ParentMainActivity.class));
                    }
                    else if (role.equals("Teacher")) {
                        startActivity(new Intent(this, TeacherMainActivity.class));
                    }
                    else {
                        Toast.makeText(this, "Unknown role: " + role, Toast.LENGTH_LONG).show();
                        return;
                    }

                    finish(); // Prevent returning to login
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error loading user role: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
