package arnav.example.finmate;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import arnav.example.finmate.helper.Backend;

public class MainActivity extends AppCompatActivity {

    EditText editTextPhone, editTextOtp, editTextName, editTextEmail;
    Button buttonSendOtp, buttonVerifyOtp;
    TextView txtLoginTitle;
    String verificationId;
    FirebaseAuth mAuth;
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editTextPhone = findViewById(R.id.editTextPhone);
        editTextOtp = findViewById(R.id.editTextOtp);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonSendOtp = findViewById(R.id.buttonSendOtp);
        buttonVerifyOtp = findViewById(R.id.buttonVerifyOtp);
        txtLoginTitle = findViewById(R.id.txtLoginTitle);
        Backend.setCategories();

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        buttonSendOtp.setOnClickListener(v -> sendOtp());

        buttonVerifyOtp.setOnClickListener(v -> {

            String code = editTextOtp.getText().toString();
            if (verificationId != null) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
                signInWithPhoneAuthCredential(credential);
            }
        });
    }

    private void sendOtp() {
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();
        if (phone.isEmpty() || name.isEmpty() || email.isEmpty()) {
            editTextPhone.setError("Please, Enter All Fields");
            return;
        }

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth).setPhoneNumber("+91" + phone).setTimeout(60L, TimeUnit.SECONDS).setActivity(this).setCallbacks(mCallbacks).build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential); // Auto verify
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Log.e("OTP", "Invalid request: " + e.getMessage());
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Log.e("OTP", "SMS quota exceeded.");
            } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                Log.e("OTP", "reCAPTCHA verification failed.");
            } else {
                Log.e("OTP", "Verification failed: " + e.getMessage());
            }
            Toast.makeText(MainActivity.this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void onCodeSent(@NonNull String id, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            verificationId = id;

            editTextName.setVisibility(View.GONE);
            editTextEmail.setVisibility(View.GONE);
            editTextPhone.setVisibility(View.GONE);
            buttonSendOtp.setVisibility(View.GONE);
            txtLoginTitle.setText("Verify OTP...");
            editTextOtp.setVisibility(View.VISIBLE);
            buttonVerifyOtp.setVisibility(View.VISIBLE);

            Toast.makeText(MainActivity.this, "OTP Sent!", Toast.LENGTH_SHORT).show();
        }
    };



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = task.getResult().getUser();
                String uId = user.getUid();
                String name = editTextName.getText().toString();
                String email = editTextEmail.getText().toString();
                String phone = user.getPhoneNumber();

                db.collection("users").document(uId).get().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful() && task1.getResult().exists()) {
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                        finish();
                    } else {
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("name", name);
                        userData.put("email", email);
                        userData.put("phone", phone);
                        userData.put("totalIncome", 0);
                        userData.put("totalMonthlyBalance", 0);

                        db.collection("users").document(uId).set(userData).addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Profile Created", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(MainActivity.this, HomeActivity.class));
                            finish();

                        });
                    }
                });
            } else {
                Toast.makeText(this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            finish();
        }
    }

}