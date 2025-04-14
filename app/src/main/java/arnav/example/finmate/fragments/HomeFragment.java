package arnav.example.finmate.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import arnav.example.finmate.HomeActivity;
import arnav.example.finmate.R;
import arnav.example.finmate.adapters.RecyclerViewExpenseAdapter;
import arnav.example.finmate.model.ExpenseModel;

public class HomeFragment extends Fragment {

ArrayList<ExpenseModel>  expenses = new ArrayList<>();

    RecyclerViewExpenseAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;


    public HomeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerExpense = view.findViewById(R.id.recyclerExpense);

        recyclerExpense.setLayoutManager(new LinearLayoutManager(getContext()));
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ExpenseModel expense = doc.toObject(ExpenseModel.class);
                        expenses.add(expense);
                    }

                    adapter.notifyDataSetChanged();

                    // Now expenseList has all your expenses
                    // You can notify your RecyclerView adapter here
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


adapter =new RecyclerViewExpenseAdapter(getContext(), expenses);
recyclerExpense.setAdapter(adapter);

        return view;
    }
}