package arnav.example.finmate;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class HomeActivity extends AppCompatActivity {

    TextView textViewWelcome, textViewMessage;
    Button buttonLogout;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewWelcome = findViewById(R.id.textViewWelcome);
        textViewMessage = findViewById(R.id.textViewMessage);
        buttonLogout = findViewById(R.id.buttonLogout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String uid = user.getUid();

            // Fetch user info from Firestore
            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String phone = documentSnapshot.getString("phone");

                            if (name != null && !name.isEmpty()) {
                                textViewWelcome.setText("Welcome, " + name + "!");
                            } else {
                                textViewWelcome.setText("Welcome, " + phone + "!");
                            }

                            // Optionally set a custom message or task
                            textViewMessage.setText("You're already registered. Ready to track expenses?");
                        }
                    });
        }

        buttonLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, arnav.example.finmate.MainActivity.class));
            finish();
        });
    }
}