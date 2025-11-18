package com.example.appointable;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // UI Fields
    private Button btnRegister;
    private EditText etFirstname, etLastname, etMiddlename, etSuffix;
    private EditText etBirthdate, etAge, etContact;
    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Spinner spinnerRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseApp.initializeApp(this); // IMPORTANT FIX

        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeUI();
        setupLoginClickable();
        setupBirthdatePicker();

        btnRegister.setOnClickListener(v -> registerUser());
    }

    private void initializeUI() {

        // Button
        btnRegister = findViewById(R.id.btnRegister);

        // Name fields
        etFirstname = findViewById(R.id.etFirstname);
        etLastname = findViewById(R.id.etLastname);
        etMiddlename = findViewById(R.id.etMiddlename);
        etSuffix = findViewById(R.id.etSuffix);

        // Demographics
        etBirthdate = findViewById(R.id.etBirthdate);
        etAge = findViewById(R.id.etAge);
        etContact = findViewById(R.id.etContact);

        // Account fields
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etconfirmPassword);

        // Spinner for Role
        spinnerRole = findViewById(R.id.spinnerRole);

        // Populate Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.role_list,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);
    }

    private void setupLoginClickable() {
        TextView tvLogin = findViewById(R.id.tvLogin);
        String fullText = "Already have an account? Login";

        SpannableString span = new SpannableString(fullText);

        int start = fullText.indexOf("Login");
        int end = start + "Login".length();

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(android.view.View widget) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                finish();
            }
        };

        span.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(Color.BLUE), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvLogin.setText(span);
        tvLogin.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void setupBirthdatePicker() {
        etBirthdate.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();

            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String date = (selectedMonth + 1) + "/" + selectedDay + "/" + selectedYear;
                        etBirthdate.setText(date);

                        etAge.setText(String.valueOf(calculateAge(selectedYear, selectedMonth, selectedDay)));
                    },
                    year, month, day
            );

            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });
    }

    private int calculateAge(int y, int m, int d) {
        Calendar dob = Calendar.getInstance();
        dob.set(y, m, d);

        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }

    private void registerUser() {

        // Collect inputs
        String first = etFirstname.getText().toString().trim();
        String last = etLastname.getText().toString().trim();
        String middle = etMiddlename.getText().toString().trim();
        String suffix = etSuffix.getText().toString().trim();
        String birthdate = etBirthdate.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        // Validation
        if (first.isEmpty() || last.isEmpty() || birthdate.isEmpty() || age.isEmpty()
                || contact.isEmpty() || email.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = spinnerRole.getSelectedItem().toString();

        // Create account
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener(auth -> {
                    String uid = auth.getUser().getUid();

                    Map<String, Object> user = new HashMap<>();
                    user.put("uid", uid);
                    user.put("firstName", first);
                    user.put("lastName", last);
                    user.put("middleName", middle);
                    user.put("suffix", suffix);
                    user.put("birthdate", birthdate);
                    user.put("age", age);
                    user.put("contact", contact);
                    user.put("email", email);
                    user.put("username", username);
                    user.put("role", role);

                    db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(a -> {
                                Toast.makeText(this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}
