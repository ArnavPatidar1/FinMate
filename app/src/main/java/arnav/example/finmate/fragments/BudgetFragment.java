package arnav.example.finmate.fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import arnav.example.finmate.adapters.CategoryAdapter;
import arnav.example.finmate.adapters.CategoryBudgetAdapter;
import arnav.example.finmate.databinding.CategoryDialogBoxBinding;
import arnav.example.finmate.databinding.FragmentBudgetBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryBudgetModel;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.MonthlyBudgetModel;


public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private ArrayList<CategoryBudgetModel> categoryList = new ArrayList<>();
    private CategoryBudgetAdapter categoryBudgetAdapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private double monthlyBudgetAmount = 0.0;
    private Date startDate, endDate;
    private Calendar calendar;

    private Backend helper;

    public BudgetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBudgetBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        helper = new Backend(getContext());
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();
        calendar = Calendar.getInstance();

        startDate = Backend.getStartOfMonth((Calendar) calendar.clone());
        endDate = Backend.getEndOfMonth((Calendar) calendar.clone());
        Timestamp monthStart = new Timestamp(startDate);
        Timestamp monthEnd = new Timestamp(endDate);

        Backend.getMonthlyBudgetForRange(db, userId, monthStart, monthEnd, queryDocumentSnapshots -> {
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    MonthlyBudgetModel model = document.toObject(MonthlyBudgetModel.class);
                    if (model != null) {
                        monthlyBudgetAmount = model.getBudgetAmount();
                        binding.monthlyBudgetAmount.setText(Double.toString(monthlyBudgetAmount));
                    }
                }
            }
        }, e -> {
            Toast.makeText(getContext(), "Failed to load Monthly Budget", Toast.LENGTH_SHORT).show();
        });

        loadCategorizedBudget(monthStart, monthEnd);


        binding.btnSetBudget.setOnClickListener(v -> {
            monthlyBudgetAmount = Double.parseDouble(binding.monthlyBudgetAmount.getText().toString());
            if (monthlyBudgetAmount <= 0) return;

            Backend.getMonthlyBudgetForRange(db, userId, monthStart, monthEnd, queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        MonthlyBudgetModel model = document.toObject(MonthlyBudgetModel.class);
                        if (model != null && model.getBudgetAmount() != monthlyBudgetAmount) {
                            model.setBudgetAmount(monthlyBudgetAmount);
                            Backend.updateMonthlyBudget(db, userId, model, unused -> {
                                Toast.makeText(getContext(), "Monthly Budget Updated", Toast.LENGTH_SHORT).show();
                                saveCategoryBudgets(monthStart, monthEnd);
                            }, e -> {
                                Toast.makeText(getContext(), "Failed to update budget", Toast.LENGTH_SHORT).show();
                            });
                        } else {
                            saveCategoryBudgets(monthStart, monthEnd);
                        }
                    }
                } else {
                    String budgetId = db.collection("users").document(userId).collection("monthly_budget").document().getId();
                    MonthlyBudgetModel model = new MonthlyBudgetModel( monthlyBudgetAmount, monthStart, monthEnd);
                    Backend.addNewMonthlyBudget(db, userId, model, v2 -> {
                        Toast.makeText(getContext(), "Monthly Budget Setup Completed", Toast.LENGTH_SHORT).show();
                        saveCategoryBudgets(monthStart, monthEnd);
                    }, e -> {
                        Toast.makeText(getContext(), "Monthly Budget Setup failed", Toast.LENGTH_SHORT).show();
                    });
                }
            }, e -> {
                Toast.makeText(getContext(), "Failed to check existing monthly budget", Toast.LENGTH_SHORT).show();
            });



        });

        binding.btnAddCategoryBudget.setOnClickListener(v -> {
            CategoryDialogBoxBinding dialogBoxBinding = CategoryDialogBoxBinding.inflate(getLayoutInflater());
        AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
        categoryDialog.setView(dialogBoxBinding.getRoot());

        dialogBoxBinding.categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        CategoryAdapter categoryAdapter = new CategoryAdapter(getContext(), Backend.categories, new CategoryAdapter.CategoryClickListener() {
            @Override
            public void onCategoryClicked(CategoryModel category) {
                for (CategoryBudgetModel model :
                        categoryList) {
                    if (model.getCategory().getName().equals(category.getName())) {
                        Toast.makeText(getContext(), "Category already added", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Backend.getCategorizedSpent(db, userId, monthStart, monthEnd, category, aDouble -> {
                    String categoryBudgetId = db.collection("users").document(userId).collection("category_budgets").document().getId();
                    CategoryBudgetModel model = new CategoryBudgetModel(categoryBudgetId, category, 0, aDouble, monthStart, monthEnd);
//                    Backend.addNewCategoryBudget(db, userId, model, documentReference -> {}, e -> {});
                    categoryList.add(model);
                    categoryBudgetAdapter.notifyItemInserted(categoryList.size() - 1);

                    categoryDialog.dismiss();

                }, e -> {
                    Toast.makeText(getContext(), "Failed in loading categorized spent", Toast.LENGTH_SHORT).show();
                });


            }
        });
        dialogBoxBinding.categoryRecyclerView.setAdapter(categoryAdapter);
        categoryDialog.show();
        });

        categoryBudgetAdapter = new CategoryBudgetAdapter(requireContext(), categoryList);
        binding.budgetList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.budgetList.setAdapter(categoryBudgetAdapter);

    }

    @Override
    public void onResume() {

        super.onResume();
        startDate = Backend.getStartOfMonth((Calendar) calendar.clone());
        endDate = Backend.getEndOfMonth((Calendar) calendar.clone());
        Timestamp monthStart = new Timestamp(startDate);
        Timestamp monthEnd = new Timestamp(endDate);
        loadCategorizedBudget(monthStart, monthEnd);
    }

    private void loadCategorizedBudget(Timestamp monthStart, Timestamp monthEnd) {
        Backend.getCategorizedBudget(db, userId, monthStart, monthEnd, queryDocumentSnapshots -> {
            categoryList.clear();
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot documentSnapshot :
                        queryDocumentSnapshots) {
                    int i = 1;
                    CategoryBudgetModel model = documentSnapshot.toObject(CategoryBudgetModel.class);
                    categoryList.add(model);
                    String TAG = "Category wise Budget" + i;
                    String message = "Category: " + model.getCategory().getName() + ", BudgetAmount: " + model.getCategoryBudget() + ", SpentAmount: " + model.getCategorySpent() + ", startDate: " + model.getStartDate().toString() + ", endDate: " + model.getEndDate().toString();
                    Log.d(TAG, message);
                    i++;
                }
                categoryBudgetAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Loading of categorized budgets successful", Toast.LENGTH_SHORT).show();
            }
        }, e -> {
            Toast.makeText(getContext(), "Failed to load categorized budgets", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveCategoryBudgets(Timestamp start, Timestamp end) {
        for (CategoryBudgetModel model : categoryList) {
            model.setStartDate(start);
            model.setEndDate(end);
            Backend.saveOrUpdateCategoryBudget(db, userId, model,v -> {}, e -> {
                Toast.makeText(getContext(), "Failed to save category: " + model.getCategory().getName(), Toast.LENGTH_SHORT).show();
            });
        }
        Toast.makeText(getContext(), "Category budgets saved", Toast.LENGTH_SHORT).show();
    }



}

