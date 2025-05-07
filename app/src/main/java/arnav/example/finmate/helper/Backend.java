package arnav.example.finmate.helper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arnav.example.finmate.R;
import arnav.example.finmate.model.CategoryBudgetModel;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;
import arnav.example.finmate.model.MonthlyBudgetModel;

public class Backend {

    private final FirebaseUser user;
    private final FirebaseFirestore db;
    private final CollectionReference expenseRef;
    private final Context context;
    private final Calendar calendar;
    public static ArrayList<CategoryModel> categories = new ArrayList<>();
    private static final Map<String, Integer> iconMap = new HashMap<>();
    private static final Map<String, Integer> colorMap = new HashMap<>();



    public Backend(Context context) {
        this.context = context;
        this.calendar = Calendar.getInstance();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not logged in");
        }

        expenseRef = db.collection("users").document(user.getUid()).collection("expenses");
    }

    public void addExpense(ExpenseModel expense, OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener) {
        expense.setTimestamp(Timestamp.now());

        expense.setId(expenseRef.getId());
        expenseRef.add(expense)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        db.runTransaction(transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(db.collection("users").document(user.getUid()));
            double currentExpense = 0;
            double currentIncome = 0;

            if (expense.isIncome()) {
                if (documentSnapshot.contains("totalIncome")) {
                    currentIncome = documentSnapshot.getDouble("totalIncome");
                    double updatedIncome = currentIncome + expense.getAmount();
                    transaction.update(db.collection("users").document(user.getUid()), "totalIncome", updatedIncome);
                }

            } else {
                if (documentSnapshot.contains("totalExpense")) {
                    currentExpense = documentSnapshot.getDouble("totalExpense");
                    double updatedTotal = currentExpense + expense.getAmount();
                    transaction.update(db.collection("users").document(user.getUid()), "totalExpense", updatedTotal);
                }

            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Total expense updated");
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Failed to update total expense", e);
        });
    }


    public ArrayList<ExpenseModel> getAllExpenses(OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener) {
        ArrayList<ExpenseModel> expenseList = new ArrayList<>();
        expenseRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ExpenseModel expense = document.toObject(ExpenseModel.class);
                        if (expense != null) {
                            expense.setId(document.getId()); // Add the ID manually
                            expenseList.add(expense);
                        }
                    }

                })
                .addOnFailureListener(onFailureListener);
        return expenseList;
    }

    public void updateExpense(String documentId, ExpenseModel updatedExpense, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        expenseRef.document(documentId)
                .set(updatedExpense)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void deleteExpense(String documentId, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        expenseRef.document(documentId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public void showDatePicker(final AppCompatEditText appCompatEditText) {
        DatePickerDialog.OnDateSetListener dateSetListener = ((view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            String dateFormat = "dd/MM/YYYY";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());

            appCompatEditText.setText(simpleDateFormat.format(calendar.getTime()));
        });

        new DatePickerDialog(context,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    public Date parseDate(String dateString) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            return simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            Log.e("DateParsing", "Failed to parse date: " + dateString, e);
             return null;
        }
    }

    public boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)  &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public boolean isSameWeek(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR);
    }

    public boolean isSameMonth(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
    }


    static {
        iconMap.put("salaray_icon", R.drawable.salaray_icon);
        iconMap.put("rent_icon", R.drawable.rent_icon);
        iconMap.put("education_icon", R.drawable.education_icon);
        iconMap.put("groceries_icon", R.drawable.groceries_icon);
        iconMap.put("food_icon", R.drawable.food_icon);
        iconMap.put("petrol_icon", R.drawable.petrol_icon);
        iconMap.put("investment_icon", R.drawable.investment_icon);
        iconMap.put("loan_icon", R.drawable.loan_icon);
        iconMap.put("travel_icon", R.drawable.travel_icon);
        iconMap.put("other_icon", R.drawable.other_icon);
        // Add all other mappings here
    }

    public static int getIconResId(String iconName) {
        Integer resId = iconMap.get(iconName);
        return resId != null ? resId : R.drawable.other_icon;
    }
    public static void setCategories() {
        categories.clear();
        categories.add(new CategoryModel("Salary", "salaray_icon", "category1"));
        categories.add(new CategoryModel("Rent", "rent_icon", "category2"));
        categories.add(new CategoryModel("Education", "education_icon", "category3"));
        categories.add(new CategoryModel("Groceries", "groceries_icon", "category4"));
        categories.add(new CategoryModel("Food", "food_icon", "category5"));
        categories.add(new CategoryModel("Petrol", "petrol_icon", "category6"));
        categories.add(new CategoryModel("Investment", "investment_icon", "category7"));
        categories.add(new CategoryModel("Loan", "loan_icon", "category8"));
        categories.add(new CategoryModel("Travel", "travel_icon", "category9"));
        categories.add(new CategoryModel("Others", "other_icon", "category10"));
    }

    static {
        colorMap.put("category1", R.color.category1);
        colorMap.put("category2", R.color.category2);
        colorMap.put("category3", R.color.category3);
        colorMap.put("category4", R.color.category4);
        colorMap.put("category5", R.color.category5);
        colorMap.put("category6", R.color.category6);
        colorMap.put("category7", R.color.category7);
        colorMap.put("category8", R.color.category8);
        colorMap.put("category9", R.color.category9);
        colorMap.put("category10", R.color.category10);
        // Add all color mappings
    }

    public static ColorStateList getColorStateList(Context context, String colorName) {
        Integer colorResId = colorMap.get(colorName);
        return colorResId != null ? ContextCompat.getColorStateList(context, colorResId) : null;
    }


    public static CategoryModel getCategoryDetails(String categoryName) {
        for (CategoryModel cat :
                categories) {
            if (cat.getName().equals(categoryName)) {
                return cat;
            }
        }
        return null;
    }

    public static int getAccountColor(String accountName) {
        switch (accountName) {
            case "Cash":
                return R.color.category4;
            case "Bank":
                return R.color.category6;
            case "Upi":
                return R.color.purple_500;
            default:
                return R.color.gray;
        }
    }

    public static Date getStartOfDay(Calendar cal) {
        Calendar clone = (Calendar) cal.clone();
        clone.set(Calendar.HOUR_OF_DAY, 0);
        clone.set(Calendar.MINUTE, 0);
        clone.set(Calendar.SECOND, 0);
        clone.set(Calendar.MILLISECOND, 0);
        return clone.getTime();
    }

    public static Date getEndOfDay(Calendar cal) {
        Calendar clone = (Calendar) cal.clone();
        clone.set(Calendar.HOUR_OF_DAY, 23);
        clone.set(Calendar.MINUTE, 59);
        clone.set(Calendar.SECOND, 59);
        clone.set(Calendar.MILLISECOND, 999);
        return clone.getTime();
    }

    public static Date getStartOfWeek(Calendar cal) {
        Calendar clone = (Calendar) cal.clone();
        clone.set(Calendar.DAY_OF_WEEK, clone.getFirstDayOfWeek());
        return getStartOfDay(clone);
    }

    public static Date getEndOfWeek(Calendar cal) {
        Calendar clone = (Calendar) cal.clone();
        clone.set(Calendar.DAY_OF_WEEK, clone.getFirstDayOfWeek());
        clone.add(Calendar.DAY_OF_WEEK, 6);
        return getEndOfDay(clone);
    }

    public static Date getStartOfMonth(Calendar cal) {
        Calendar clone = (Calendar) cal.clone();
        clone.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(clone);
    }

    public static Date getEndOfMonth(Calendar cal) {
        Calendar clone = (Calendar) cal.clone();
        clone.set(Calendar.DAY_OF_MONTH, clone.getActualMaximum(Calendar.DAY_OF_MONTH));
        return getEndOfDay(clone);
    }

    public interface ExpenseDataCallback {
        void onSuccess(List<DocumentSnapshot> documents);
        void onFailure(Exception e);
    }
    public static void loadCategorizedBudget(
            String userId,
            Timestamp startDate,
            Timestamp endDate,
            ExpenseDataCallback callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("category_budgets")
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("endDate", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.getDocuments());
                })
                .addOnFailureListener(callback::onFailure);
    }


    //BudgetFragment Related Methods

    public static void getMonthlyBudgetForRange(FirebaseFirestore db, String userId, Timestamp startDate, Timestamp endDate, OnSuccessListener<QuerySnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("monthly_budget")
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("endDate", endDate)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    public static void updateMonthlyBudget(FirebaseFirestore db, String userId, MonthlyBudgetModel model, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("monthly_budget")
                .document(model.getMonthlyBudgetId())
                .set(model)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    public static void addNewMonthlyBudget(FirebaseFirestore db, String userId, MonthlyBudgetModel model, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("monthly_budget")
                .add(model)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static void getCategorizedBudget(FirebaseFirestore db, String userId, Timestamp startDate, Timestamp endDate, OnSuccessListener<QuerySnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("category_budgets")
                .whereGreaterThanOrEqualTo("startDate", startDate)
                .whereLessThanOrEqualTo("endDate", endDate)
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static void addNewCategoryBudget(FirebaseFirestore db, String userId, CategoryBudgetModel model, OnSuccessListener<DocumentReference> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("category_budgets")
                .add(model)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static void getCategorizedSpent(FirebaseFirestore db, String userId, Timestamp startDate, Timestamp endDate, CategoryModel category, OnSuccessListener<Double> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("expenses")
                .whereEqualTo("income", false)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSpent = 0;
                    for (DocumentSnapshot document :
                            queryDocumentSnapshots) {
                        ExpenseModel expense = document.toObject(ExpenseModel.class);
                        if (expense != null) {
                            totalSpent += expense.getAmount();
                        }
                    }
                    onSuccess.onSuccess(totalSpent);
                })
                .addOnFailureListener(onFailure);
    }



}


