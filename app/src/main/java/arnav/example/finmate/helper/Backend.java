package arnav.example.finmate.helper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public  void addExpense(ExpenseModel expense, OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener) {
        expenseRef.add(expense)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        db.runTransaction(transaction -> {
            DocumentSnapshot documentSnapshot = transaction.get(db.collection("users").document(user.getUid()));
            double currentTotal =0;
            if (documentSnapshot.contains("totalExpense")) {
                currentTotal = documentSnapshot.getDouble("totalExpense");
            }
            double updatedTotal = currentTotal + expense.getAmount();
            transaction.update(db.collection("users").document(user.getUid()), "totalExpense", updatedTotal);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("Firestore", "Total expense updated");
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Failed to update total expense", e);
        });
    }


    public ArrayList<ExpenseModel> getAllExpenses(  OnSuccessListener<DocumentReference> onSuccessListener, OnFailureListener onFailureListener) {
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

}
