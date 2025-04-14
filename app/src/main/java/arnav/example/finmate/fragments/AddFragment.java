package arnav.example.finmate.fragments;

import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arnav.example.finmate.R;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;


public class AddFragment extends Fragment {

    private EditText etAmount, etDate, etDescription;
    private Spinner categorySpinner;
    private List<CategoryModel> categoryList = new ArrayList<>();
    private List<String> categoryNames = new ArrayList<>();
    private Map<String, String> categoryIdMap = new HashMap<>();
    private AppCompatButton btnAddExpense;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public AddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add, container, false);

        etAmount = view.findViewById(R.id.etAmount);
        etDate = view.findViewById(R.id.etDate);
        etDescription = view.findViewById(R.id.etDescription);
        categorySpinner = view.findViewById(R.id.category);
        btnAddExpense = view.findViewById(R.id.btnAddExpense);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        loadCategories();
        setupDatePicker();

        btnAddExpense.setOnClickListener(v -> saveExpenseToFirestore());

        return view;
    }

    private void loadCategories() {
        db.collection("categories")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categoryList.clear();
                    categoryNames.clear();
                    categoryIdMap.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        CategoryModel category = doc.toObject(CategoryModel.class);
                        categoryList.add(category);
                        categoryNames.add(category.getName());
                        categoryIdMap.put(category.getName(), category.getId());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, categoryNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    categorySpinner.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to load categories", Toast.LENGTH_SHORT).show();
                });
    }


    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
                String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                etDate.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void saveExpenseToFirestore() {
        String amountStr = etAmount.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String categoryName = categorySpinner.getSelectedItem().toString();
        String selectedCategoryName = categorySpinner.getSelectedItem().toString();
        String categoryId = categoryIdMap.get(selectedCategoryName);

        if (amountStr.isEmpty() || date.isEmpty() || description.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);

        String userId = auth.getCurrentUser().getUid();
        String expenseId = db.collection("users").document(userId)
                .collection("expenses").document().getId();

        ExpenseModel expense = new ExpenseModel(expenseId,  categoryId, amount,date, description, false);

        db.collection("users").document(userId)
                .collection("expenses").document(expenseId)
                .set(expense)
                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());


    }



}