package arnav.example.finmate.helper;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arnav.example.finmate.model.ExpenseModel;
import arnav.example.finmate.model.FinancialData;
import arnav.example.finmate.model.MonthlyBudgetModel;
import arnav.example.finmate.model.SavingGoalModel;
    import arnav.example.finmate.model.CategoryBudgetModel;

public class FirebaseHelper {

    private final FirebaseFirestore db;

    public FirebaseHelper() {
        this.db = FirebaseFirestore.getInstance();
    }


    // Fetch the total income and expenses for the user
    public void fetchTotalIncomeAndExpense(String userId, final FirebaseCallback callback) {
        CollectionReference expensesRef = db.collection("users").document(userId).collection("expenses");

        expensesRef.get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        double[] totalIncome = new double[]{0};
                        double[] totalExpense = new double[]{0};
                        Map<String, Double> categorizedExpenses = new HashMap<>();

                        for (DocumentSnapshot document :
                                queryDocumentSnapshots) {
                            ExpenseModel expense = document.toObject(ExpenseModel.class);
                            assert expense != null;
                            String category = expense.getCategory().getName();
                            double amount = expense.getAmount();
                            if (expense.isIncome()) {
                                totalIncome[0] += amount;
                            } else {
                                totalExpense[0] += amount;
                            }
                            categorizedExpenses.put(category, categorizedExpenses.getOrDefault(category, 0.0) + amount);
                        }

                        FinancialData financialData = new FinancialData(totalIncome[0], totalExpense[0]);

                        callback.onSuccess(financialData, categorizedExpenses);

                    }
                });

    }

    // Callback interface to handle Firebase response
    public interface FirebaseCallback {
        default void onSuccess(List<ExpenseModel> expenseList) {}
        default void onSuccess(double totalIncome, double totalExpense) {}
        default void onSuccess(FinancialData financialData, Map<String, Double> categorizedExpenses) {}
        void onError(String error);
    }


    public void fetchMonthlyBudget(String userId, final FirebaseMonthlyBudgetCallback callback) {
        CollectionReference budgetRef = db.collection("users").document(userId).collection("monthly_budget");

        budgetRef.get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                MonthlyBudgetModel budget = doc.toObject(MonthlyBudgetModel.class);
                if (budget != null) {
                    budget.setMonthlyBudgetId(doc.getId());
                    callback.onSuccess(budget);
                } else {
                    callback.onError("Budget data is null");
                }
            } else {
                callback.onError("No budget found");
            }
        }).addOnFailureListener(e -> callback.onError("Failed to fetch budget: " + e.getMessage()));
    }

    public interface FirebaseMonthlyBudgetCallback {
        void onSuccess(MonthlyBudgetModel budget);
        void onError(String error);
    }

    public void fetchSavingGoals(String userId, final FirebaseSavingGoalCallback callback) {
        CollectionReference goalsRef = db.collection("users").document(userId).collection("saving_goals");

        goalsRef.get().addOnSuccessListener(querySnapshot -> {
            List<SavingGoalModel> goalList = new java.util.ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot) {
                SavingGoalModel goal = doc.toObject(SavingGoalModel.class);
                if (goal != null) {
                    goalList.add(goal);
                }
            }
            callback.onSuccess(goalList);
        }).addOnFailureListener(e -> callback.onError("Failed to fetch saving goals: " + e.getMessage()));
    }


    public interface FirebaseSavingGoalCallback {
        void onSuccess(List<SavingGoalModel> goals);
        void onError(String error);
    }


    public void fetchCategoryBudgets(String userId, final FirebaseCategoryBudgetCallback callback) {
        CollectionReference categoryBudgetRef = db.collection("users").document(userId).collection("category_budgets");

        categoryBudgetRef.get().addOnSuccessListener(querySnapshot -> {
            List<CategoryBudgetModel> categoryBudgets = new java.util.ArrayList<>();
            for (DocumentSnapshot doc : querySnapshot) {
                CategoryBudgetModel categoryBudget = doc.toObject(CategoryBudgetModel.class);
                if (categoryBudget != null) {
                    categoryBudgets.add(categoryBudget);
                }
            }
            callback.onSuccess(categoryBudgets);
        }).addOnFailureListener(e -> callback.onError("Failed to fetch category budgets: " + e.getMessage()));
    }

    public interface FirebaseCategoryBudgetCallback {
        void onSuccess(List<CategoryBudgetModel> categoryBudgets);
        void onError(String error);
    }

}
