package arnav.example.finmate.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.icu.util.Calendar;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.adapters.AccountAdapter;
import arnav.example.finmate.adapters.CategoryAdapter;
import arnav.example.finmate.databinding.CategoryDialogBoxBinding;
import arnav.example.finmate.databinding.FragmentAddBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.AccountModel;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;


public class AddFragment extends Fragment {
    private boolean isIncome = false;
    private FragmentAddBinding binding;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    String userId;
    NavController navController;

    Backend helper;


    public AddFragment() {
        // Required empty public constructor
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAddBinding.inflate(inflater);

        helper = new Backend(requireContext());
        navController = NavHostFragment.findNavController(AddFragment.this);

        binding.btnExpense.setSelected(true);

        binding.btnExpense.setOnClickListener(v -> {
            binding.btnExpense.setSelected(true);
            binding.btnIncome.setSelected(false);
            isIncome = false;

        });

        binding.btnIncome.setOnClickListener(v -> {
            binding.btnIncome.setSelected(true);
            binding.btnExpense.setSelected(false);
            isIncome = true;
        });


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
        binding.edtDate.setOnClickListener(v -> {
            helper.showDatePicker(binding.edtDate);
        });


        binding.edtCategory.setOnClickListener(v -> {
            CategoryDialogBoxBinding dialogBoxBinding = CategoryDialogBoxBinding.inflate(inflater);
            AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
            categoryDialog.setView(dialogBoxBinding.getRoot());

            dialogBoxBinding.categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
            CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(), Backend.categories, new CategoryAdapter.CategoryClickListener() {
                @Override
                public void onCategoryClicked(CategoryModel category) {
                    binding.edtCategory.setText(category.getName());
                    categoryDialog.dismiss();
                }
            });
            dialogBoxBinding.categoryRecyclerView.setAdapter(categoryAdapter);

            categoryDialog.show();
        });

        binding.edtAccount.setOnClickListener(v -> {
            CategoryDialogBoxBinding dialogBoxBinding = CategoryDialogBoxBinding.inflate(inflater);
            AlertDialog accountDialog = new AlertDialog.Builder(getContext()).create();
            accountDialog.setView(dialogBoxBinding.getRoot());

            ArrayList<AccountModel> accounts = new ArrayList<>();
            accounts.add(new AccountModel(0, "Cash"));
            accounts.add(new AccountModel(0, "Bank"));
            accounts.add(new AccountModel(0, "UPI"));

            AccountAdapter accountAdapter = new AccountAdapter(requireContext(), accounts, new AccountAdapter.AccountClickListener() {
                @Override
                public void onAccountSelected(AccountModel account) {
                    binding.edtAccount.setText(account.getAccountName());
                    accountDialog.dismiss();
                }
            });
            dialogBoxBinding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            dialogBoxBinding.categoryRecyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
            dialogBoxBinding.categoryRecyclerView.setAdapter(accountAdapter);

            accountDialog.show();
        });

        binding.btnAddTransaction.setOnClickListener(v -> saveExpenseToFirestore());

        return binding.getRoot();
    }

    private void saveExpenseToFirestore() {
        String amountStr = binding.edtAmount.getText().toString().trim();
        String date = binding.edtDate.getText().toString().trim();
        String description = binding.edtDescription.getText().toString().trim();
        String category = binding.edtCategory.getText().toString().trim();
        CategoryModel categoryModel = Backend.getCategoryDetails(category);
        String account = binding.edtAccount.getText().toString().trim();

        if (amountStr.isEmpty() || date.isEmpty() || category.isEmpty() || account.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String expenseId = db.collection("users").document(userId).collection("expenses").document().getId();

        ExpenseModel expense;
        if (description.isEmpty()) {
            expense = new ExpenseModel(expenseId, categoryModel, amount, date, account, isIncome);
        } else {
            expense = new ExpenseModel(expenseId, categoryModel, amount, description, date, account, isIncome);
        }

        helper.addExpense(db, userId, expense, Unused -> {
            Toast.makeText(getContext(), "Expense Added", Toast.LENGTH_SHORT).show();
            navController.navigate(R.id.homeFragment);
        }, e -> {
            Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
        });

    }



}