package arnav.example.finmate.helper;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.util.Log;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

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
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import arnav.example.finmate.R;
import arnav.example.finmate.model.CategoryBudgetModel;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;
import arnav.example.finmate.model.MonthlyBudgetModel;
import arnav.example.finmate.model.SavingGoalModel;

public class Backend {

    private final FirebaseUser user;
    private final FirebaseFirestore db;
    private final CollectionReference expenseRef;
    private final Context context;
    private final Calendar calendar;
    public static ArrayList<CategoryModel> categories = new ArrayList<>();
    public static ArrayList<CategoryModel> goalsCategories = new ArrayList<>();
    private static final Map<String, Integer> iconMap = new HashMap<>();
    private static final Map<String, Integer> colorMap = new HashMap<>();
    private Timestamp monthStart;
    private Timestamp monthEnd;



    public Backend(Context context) {
        this.context = context;
        this.calendar = Calendar.getInstance();
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            throw new IllegalStateException("User not logged in");
        }
         monthStart = new Timestamp(Backend.getStartOfMonth((Calendar) calendar.clone()));
         monthEnd = new Timestamp(Backend.getEndOfMonth((Calendar) calendar.clone()));

        expenseRef = db.collection("users").document(user.getUid()).collection("expenses");
    }

    public void addExpense(FirebaseFirestore db, String userId, ExpenseModel expense, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        expense.setTimestamp(Timestamp.now());

        String docId = expense.getId();
        db.collection("users").document(userId).collection("expenses").document(docId)
                .set(expense)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);

        db.runTransaction(transaction -> {
                    if (!expense.isIncome()){
                        db.collection("users").document(user.getUid()).collection("category_budgets")
                                .whereGreaterThanOrEqualTo("startDate", monthStart)
                                .whereLessThanOrEqualTo("endDate", monthEnd)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (DocumentSnapshot documentSnapshot :
                                                queryDocumentSnapshots) {
                                            CategoryBudgetModel categoryBudgetModel = documentSnapshot.toObject(CategoryBudgetModel.class);
                                            if (categoryBudgetModel.getCategory().getName().equals(expense.getCategory().getName())) {
                                                double newSpentAmount = categoryBudgetModel.getCategorySpent() + expense.getAmount();
                                                categoryBudgetModel.setCategorySpent(newSpentAmount);
                                                saveOrUpdateCategoryBudget(db, user.getUid(), categoryBudgetModel, aVoid -> {
                                                }, e -> {
                                                });
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                });
                    }

                    return null;
        }).addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});

        db.runTransaction(transaction -> {
                    if (!expense.isIncome()){
                        db.collection("users").document(user.getUid()).collection("monthly_budget")
                                .whereGreaterThanOrEqualTo("startDate", monthStart)
                                .whereLessThanOrEqualTo("endDate", monthEnd)
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        for (DocumentSnapshot documentSnapshot :
                                                queryDocumentSnapshots) {
                                            MonthlyBudgetModel monthlyBudgetModel = documentSnapshot.toObject(MonthlyBudgetModel.class);
                                            double updatedSpent = monthlyBudgetModel.getMonthlySpent() + expense.getAmount();
                                            monthlyBudgetModel.setMonthlySpent(updatedSpent);
                                            DocumentReference docRef = documentSnapshot.getReference();
                                            documentSnapshot.getReference().set(monthlyBudgetModel);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                });
                    }
            return null;
        }).addOnSuccessListener(command -> {})
                .addOnFailureListener(e -> {});

        db.runTransaction(transaction -> {


            return null;
        }).addOnSuccessListener(command -> {})
                .addOnFailureListener(e -> {});
    }

    public static void getAllMonthlyIncome(FirebaseFirestore db, String userId, Timestamp monthStart, Timestamp monthEnd, OnSuccessListener<Double> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection("users").document(userId).collection("expenses")
                .whereGreaterThanOrEqualTo("timestamp", monthStart)
                .whereLessThanOrEqualTo("timestamp", monthEnd)
                .whereEqualTo("income", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalMonthlyIncome =0 ;
                    for (DocumentSnapshot document :
                            queryDocumentSnapshots) {
                        ExpenseModel expense = document.toObject(ExpenseModel.class);
                        totalMonthlyIncome += expense.getAmount();
                    }
                    onSuccessListener.onSuccess(totalMonthlyIncome);
                })
                .addOnFailureListener(onFailureListener);
    }

    public static void setTotalMonthlyBalance(FirebaseFirestore db, String userId, double amount) {
        db.collection("users").document(userId).update("totalMonthlyBalance", amount)
                .addOnSuccessListener(unused -> {})
                .addOnFailureListener(e -> {});
    }


    public static void getAllMonthlyExpenses(FirebaseFirestore db, String userId, Timestamp monthStart, Timestamp monthEnd, OnSuccessListener<Double> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection("users").document(userId).collection("expenses")
                .whereGreaterThanOrEqualTo("timestamp", monthStart)
                .whereLessThanOrEqualTo("timestamp", monthEnd)
                .whereEqualTo("income", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalMonthlySpent =0 ;
                    for (DocumentSnapshot document :
                            queryDocumentSnapshots) {
                        ExpenseModel expense = document.toObject(ExpenseModel.class);
                        totalMonthlySpent += expense.getAmount();
                    }
                    onSuccessListener.onSuccess(totalMonthlySpent);
                })
                .addOnFailureListener(onFailureListener);
    }

    public void updateExpense(String documentId, ExpenseModel updatedExpense, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        expenseRef.document(documentId)
                .set(updatedExpense)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

    public static void deleteExpense(FirebaseFirestore db, String userId, String expenseID, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("expenses")
                .document(expenseID)
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
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
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
        iconMap.put("travel_icon2", R.drawable.travel_icon2);
        iconMap.put("car_icon", R.drawable.car_icon);
        iconMap.put("bike_icon", R.drawable.bike_icon);
        iconMap.put("education_icon2", R.drawable.education_icon2);
        iconMap.put("emergency_fund_icon", R.drawable.emergency_fund_icon);
        iconMap.put("house_icon", R.drawable.house_icon);
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

    public static void setGoalsCategories() {
        goalsCategories.clear();
        goalsCategories.add(new CategoryModel("Travel & Vacation", "travel_icon2", "default"));
        goalsCategories.add(new CategoryModel("Home Purchase", "house_icon", "default"));
        goalsCategories.add(new CategoryModel("Buying a Car", "car_icon", "default"));
        goalsCategories.add(new CategoryModel("Buying a Bike", "bike_icon", "default"));
        goalsCategories.add(new CategoryModel("Education", "education_icon2", "default"));
        goalsCategories.add(new CategoryModel("Emergency Fund", "emergency_fund_icon", "default"));
        goalsCategories.add(new CategoryModel("Other", "other_icon", "default"));
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
        colorMap.put("default", R.color.white);
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


    //Calender Helper methods
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


    public static void addNewMonthlyBudget(FirebaseFirestore db, String userId, MonthlyBudgetModel model, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("monthly_budget")
                .add(model)
                .addOnSuccessListener(documentReference -> {
                    String generatedId = documentReference.getId();
                    model.setMonthlyBudgetId(generatedId);

                    documentReference.update("monthlyBudgetId", generatedId)
                            .addOnSuccessListener(onSuccess)
                            .addOnFailureListener(onFailure);
                })
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

    public static void saveOrUpdateCategoryBudget(FirebaseFirestore db, String userId, CategoryBudgetModel model,
                                                  OnSuccessListener<Void> onSuccess,
                                                  OnFailureListener onFailure) {
        // Use categoryBudgetId as the Firestore document ID
        String docId = model.getCategoryBudgetId();
        DocumentReference docRef = db.collection("users")
                .document(userId)
                .collection("category_budgets")
                .document(docId);

        // Optional: ensure categoryBudgetId in model is always synced with the document ID
        model.setCategoryBudgetId(docId);

        // Check if document exists
        docRef.get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        // Update existing document (merge recommended to avoid overwrite)
                        docRef.set(model, SetOptions.merge())
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    } else {
                        // Create new document
                        docRef.set(model)
                                .addOnSuccessListener(onSuccess)
                                .addOnFailureListener(onFailure);
                    }
                })
                .addOnFailureListener(onFailure);
    }


    public static void getCategorizedSpent(FirebaseFirestore db, String userId, Timestamp startDate, Timestamp endDate, CategoryModel category, OnSuccessListener<Double> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("expenses")
                .whereEqualTo("income", false)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    double totalSpent = 0;
                    for (DocumentSnapshot document :
                            queryDocumentSnapshots) {
                        ExpenseModel expense = document.toObject(ExpenseModel.class);
                        if (expense != null && expense.getCategory().getName().equals(category.getName())) {
                            totalSpent += expense.getAmount();
                        }
                    }
                    onSuccess.onSuccess(totalSpent);
                })
                .addOnFailureListener(onFailure);
    }

    //Saving goals related Methods

//    public static void addSavingGoal(FirebaseFirestore db, String userId, SavingGoalModel model, OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
//
//        db.collection("users").document(userId).collection("saving_goals")
//                .add(model)
//                .addOnSuccessListener(documentReference -> {
//                    String generatedId = documentReference.getId();
//                    model.setSavingGoalId(generatedId);
//
//                    documentReference.update("savingGoalId", generatedId)
//                            .addOnSuccessListener(onSuccess)
//                            .addOnFailureListener(onFailure);
//                })
//                .addOnFailureListener(onFailure);
//
//    }

    public static void addSavingGoal(FirebaseFirestore db, String userId, SavingGoalModel model,
                                     OnSuccessListener<Object> onSuccess, OnFailureListener onFailure) {

        DocumentReference userRef = db.collection("users").document(userId);
        DocumentReference goalRef = db.collection("users").document(userId)
                .collection("saving_goals").document(); // pre-generate ID

        db.runTransaction(transaction -> {
                    // Read current totalIncome
                    DocumentSnapshot userSnapshot = transaction.get(userRef);
                    Double currentIncome = userSnapshot.getDouble("totalIncome");
                    if (currentIncome == null) currentIncome = 0.0;

                    // Deduct saved amount from total income
                    double updatedIncome = currentIncome - model.getSavedAmount();
                    transaction.update(userRef, "totalIncome", updatedIncome);

                    // Set the generated ID in the model
                    model.setSavingGoalId(goalRef.getId());

                    // Save the goal model in 'saving_goals'
                    transaction.set(goalRef, model);

                    return null;
                }).addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    public static void getSavingGoals(FirebaseFirestore db, String userId, OnSuccessListener<QuerySnapshot> onSuccess, OnFailureListener onFailure) {
        db.collection("users").document(userId).collection("saving_goals")
                .get()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }


    public static void deleteSavingGoal(FirebaseFirestore db, String userId, String savingGoalId,
                                        OnSuccessListener<Void> onSuccess, OnFailureListener onFailure) {
        db.collection("users")
                .document(userId)
                .collection("saving_goals")
                .document(savingGoalId)
                .delete()
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onFailure);
    }

}


