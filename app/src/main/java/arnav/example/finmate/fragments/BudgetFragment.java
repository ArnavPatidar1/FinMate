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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import arnav.example.finmate.R;
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
                            }, e -> {
                                Toast.makeText(getContext(), "Failed to update budget", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                } else {
                    String budgetId = db.collection("users").document(userId).collection("monthly_budget").document().getId();
                    MonthlyBudgetModel model = new MonthlyBudgetModel(budgetId, monthlyBudgetAmount, monthStart, monthEnd);
                    Backend.addNewMonthlyBudget(db, userId, model, documentReference -> {
                        Toast.makeText(getContext(), "Monthly Budget Setup Completed", Toast.LENGTH_SHORT).show();
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

                double[] categorySpent = new double[]{0};
                Backend.getCategorizedSpent(db, userId, monthStart, monthEnd, category, aDouble -> {
                    categorySpent[0] = aDouble;
                }, e -> {
                    Toast.makeText(getContext(), "Failed in loading categorized spent", Toast.LENGTH_SHORT).show();
                });
                CategoryBudgetModel model = new CategoryBudgetModel(category, 0, categorySpent[0], monthStart, monthEnd);
                Backend.addNewCategoryBudget(db, userId, model, documentReference -> {}, e -> {});
                categoryList.add(model);
                categoryBudgetAdapter.notifyItemInserted(categoryList.size() - 1);

                categoryDialog.dismiss();

            }
        });
        dialogBoxBinding.categoryRecyclerView.setAdapter(categoryAdapter);
        categoryDialog.show();
        });

        categoryBudgetAdapter = new CategoryBudgetAdapter(requireContext(), categoryList);
        binding.budgetList.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.budgetList.setAdapter(categoryBudgetAdapter);

//        Backend.loadCategorizedBudget(userId, startDate, endDate, new Backend.ExpenseDataCallback() {
//            @Override
//            public void onSuccess(List<DocumentSnapshot> documents) {
//                for (DocumentSnapshot documentSnapshot :
//                        documents) {
//                    CategoryBudgetModel model = documentSnapshot.toObject(CategoryBudgetModel.class);
//                    categoryList.add(model);
//                }
//                categoryBudgetAdapter.notifyDataSetChanged();
//            }
//
//            @Override
//            public void onFailure(Exception e) {
//
//            }
//        });
//
//
//        loadCategorizedBudget(startDate, endDate);
//
//        categoryBudgetAdapter = new CategoryBudgetAdapter(requireContext(), categoryList, binding.budgetList);
//        binding.budgetList.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.budgetList.setAdapter(categoryBudgetAdapter);
//
//
//        binding.btnAddCategoryBudget.setOnClickListener(v -> showCategoryDialog());
//
//        binding.btnSetBudget.setOnClickListener(v -> {
//            saveBudgetsToFirestore();
//            categoryBudgetAdapter.notifyDataSetChanged();
//        });

    }

//    private void saveBudgetsToFirestore() {
//        String monthlyBudgetStr = binding.monthlyBudgetAmount.getText().toString().trim();
//        if (monthlyBudgetStr.isEmpty()) {
//
//        }
//
//        monthlyBudgetAmount = Double.parseDouble(monthlyBudgetStr);
//
//        Calendar cal = Calendar.getInstance();
//        cal.set(Calendar.DAY_OF_MONTH, 1);
//        Timestamp startDate = new Timestamp(cal.getTime());
//
//        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
//        Timestamp endDate = new Timestamp(cal.getTime());
//
//        MonthlyBudgetModel monthlyBudgetModel = new MonthlyBudgetModel(monthlyBudgetAmount, startDate, endDate);
//        db.collection("users")
//                .document(userId)
//                .collection("monthly_budget")
//                .add(monthlyBudgetModel)
//                .addOnSuccessListener(documentReference -> {
//                    saveCategoryBudgets(startDate, endDate);
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(), "Failed to save monthly budget", Toast.LENGTH_SHORT).show()
//                );
//    }
//
//    private void saveCategoryBudgets(Timestamp startDate, Timestamp endDate) {
//        CollectionReference categoryRef = db.collection("users")
//                .document(userId)
//                .collection("category_budgets");
//
//        for (int i = 0; i < categoryList.size(); i++) {
//            CategoryBudgetModel model = categoryList.get(i);
//
//            double amount = categoryBudgetAdapter.getBudgetAmount(i);
//            model.setCategoryBudget(amount);
//            model.setStartDate(startDate);
//            model.setEndDate(endDate);
//
//            categoryRef.add(model).addOnSuccessListener(documentReference -> {
//                categoryBudgetAdapter.notifyDataSetChanged();
//            })
//                    .addOnFailureListener(e ->
//                            Toast.makeText(getContext(), "Error Saving Category: ", Toast.LENGTH_SHORT).show()
//                    );
//        }
//        Toast.makeText(getContext(), "Budget saved successfully", Toast.LENGTH_SHORT).show();
//    }
//
//    private void showCategoryDialog() {
//        CategoryDialogBoxBinding dialogBoxBinding = CategoryDialogBoxBinding.inflate(getLayoutInflater());
//        AlertDialog categoryDialog = new AlertDialog.Builder(getContext()).create();
//        categoryDialog.setView(dialogBoxBinding.getRoot());
//
//        dialogBoxBinding.categoryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
//        CategoryAdapter categoryAdapter = new CategoryAdapter(requireContext(), Backend.categories, new CategoryAdapter.CategoryClickListener() {
//            @Override
//            public void onCategoryClicked(CategoryModel category) {
//                for (CategoryBudgetModel model :
//                        categoryList) {
//                    if (model.getCategory().getName().equals(category.getName())) {
//                        Toast.makeText(getContext(), "Category already added", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//                }
//
//                CategoryBudgetModel model = new CategoryBudgetModel();
//                model.setCategory(category);
//                model.setCategoryBudget(0.0);
//                categoryList.add(model);
//                categoryBudgetAdapter.notifyItemInserted(categoryList.size() - 1);
//
//                categoryDialog.dismiss();
//            }
//        });
//        dialogBoxBinding.categoryRecyclerView.setAdapter(categoryAdapter);
//        categoryDialog.show();
//    }
//
//
//    private void loadCategorizedBudget(Date startDate, Date endDate){
//
//        db.collection("users")
//                .document(userId)
//                .collection("category_budgets")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot documentSnapshot :
//                            queryDocumentSnapshots) {
//                        CategoryBudgetModel model = documentSnapshot.toObject(CategoryBudgetModel.class);
//                        assert model != null;
//                        Date start = model.getStartDate().toDate();
//                        Date end = model.getEndDate().toDate();
//                        if (start.before(startDate) || end.after(endDate) ) continue;
//
//                        Log.d("Loading Categorized budget", "Categorized "+ model.getCategory().getName() + " budget: " + model.getCategoryBudget() + ", spent: " + model.getCategorySpent() + ", startDate: " + model.getStartDate() + ", endDate: " + model.getEndDate());
//                        categoryList.add(model);
//
//                    }
//                    Toast.makeText(getContext(), "Loading of categorized budget successful", Toast.LENGTH_SHORT).show();
//                    categoryBudgetAdapter.notifyDataSetChanged();
//                })
//                .addOnFailureListener(e ->
//                        Toast.makeText(getContext(), "Failed to load categorized budget", Toast.LENGTH_SHORT).show());
//    }
//

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


}

