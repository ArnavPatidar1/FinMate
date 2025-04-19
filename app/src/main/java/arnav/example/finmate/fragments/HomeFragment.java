package arnav.example.finmate.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.adapters.ExpenseAdapter;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.ExpenseModel;

public class HomeFragment extends Fragment {

    ArrayList<ExpenseModel> expenses = new ArrayList<>();

    ExpenseAdapter expenseAdapter;
    Backend helper;
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

        helper  = new Backend(requireContext());
        RecyclerView recyclerExpense = view.findViewById(R.id.recyclerExpense);

//        auth = FirebaseAuth.getInstance();
//        db = FirebaseFirestore.getInstance();
//
//        String uid = auth.getCurrentUser().getUid();
//        helper.getAllExpenses(new Backend.ExpenseFetchCallback() {
//            @Override
//            public void onSuccess(ArrayList<ExpenseModel> expenseList) {
//                expenses.addAll(expenseList);
//                Log.d("Expense", "Title: " + expenses.get(0).getAccountName());
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//                Toast.makeText(getContext(), "Fetching Failed", Toast.LENGTH_SHORT).show();
//            }
//        });

//        expenses.add(new ExpenseModel(Backend.categories.get(2), 5000, "19/04/2025", "Cash", false ));

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

                    expenseAdapter.notifyDataSetChanged();

                    // Now expenseList has all your expenses
                    // You can notify your RecyclerView adapter here
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });


        expenseAdapter = new ExpenseAdapter(requireContext(), expenses);
        recyclerExpense.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerExpense.setAdapter(expenseAdapter);


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
         // Your method to load expenses from DB
        expenseAdapter.notifyDataSetChanged();
    }

}