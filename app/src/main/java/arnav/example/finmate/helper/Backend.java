package arnav.example.finmate.helper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

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
import java.util.List;
import java.util.Locale;

import arnav.example.finmate.R;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;

public class Backend {

    private final FirebaseUser user;
    private final FirebaseFirestore db;
    private final CollectionReference expenseRef;
    private final Context context;
    private final Calendar calendar;
    public static ArrayList<CategoryModel> categories = new ArrayList<>();

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

    public static void setCategories() {
        categories.add(new CategoryModel("Salary", R.drawable.salaray_icon, R.color.category1));
        categories.add(new CategoryModel("Rent", R.drawable.rent_icon, R.color.category2));
        categories.add(new CategoryModel("Education", R.drawable.education_icon, R.color.category3));
        categories.add(new CategoryModel("Groceries", R.drawable.groceries_icon, R.color.category4));
        categories.add(new CategoryModel("Food", R.drawable.food_icon, R.color.category5));
        categories.add(new CategoryModel("Petrol", R.drawable.petrol_icon, R.color.category6));
        categories.add(new CategoryModel("Investment", R.drawable.investment_icon, R.color.category7));
        categories.add(new CategoryModel("Loan", R.drawable.loan_icon, R.color.category8));
        categories.add(new CategoryModel("Travel", R.drawable.travel_icon, R.color.category9));
        categories.add(new CategoryModel("Others", R.drawable.other_icon, R.color.category10));
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
    public static void getFilteredExpenses(
            String userId,
            Date startDate,
            Date endDate,
            String type, // "Expense" or "Income"
            ExpenseDataCallback callback
    ) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(userId)
                .collection("expenses")
                .whereEqualTo("type", type)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    callback.onSuccess(queryDocumentSnapshots.getDocuments());
                })
                .addOnFailureListener(callback::onFailure);
    }
}


