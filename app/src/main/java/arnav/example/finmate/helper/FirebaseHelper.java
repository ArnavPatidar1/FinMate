package arnav.example.finmate.helper;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;
import arnav.example.finmate.model.FinancialData;

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
}
