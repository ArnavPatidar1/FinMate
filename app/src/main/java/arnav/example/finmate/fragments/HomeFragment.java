package arnav.example.finmate.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import arnav.example.finmate.R;
import arnav.example.finmate.adapters.ExpenseAdapter;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.ExpenseModel;

public class HomeFragment extends Fragment {

    ArrayList<ExpenseModel> expenses = new ArrayList<>();
    ArrayList<ExpenseModel> temporary = new ArrayList<>();
    TextView currentDate;
    ImageView previousDate, nextDate;
    TabLayout tabLayout;
    ExpenseAdapter expenseAdapter;
    Backend helper;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Calendar calendar;

    public HomeFragment() {
        // Required empty public constructor
    }
    
    @SuppressLint("NotifyDataSetChanged")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        helper = new Backend(requireContext());
        RecyclerView recyclerExpense = view.findViewById(R.id.recyclerExpense);
        currentDate = view.findViewById(R.id.currentDate);
        previousDate = view.findViewById(R.id.previousDate);
        nextDate = view.findViewById(R.id.nextDate);
        tabLayout = view.findViewById(R.id.tabLayout);

        /*Adding Current Date*/
        calendar = Calendar.getInstance();
        updateDate();

        /*Updating to Previous Date*/
        previousDate.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, -1);
            updateDate();
            loadExpense();
        });

        /*Updating to Next Date*/
        nextDate.setOnClickListener(v -> {
            calendar.add(Calendar.DATE, 1);
            updateDate();
            loadExpense();
        });

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                loadExpense();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.post(() -> tabLayout.getTabAt(0).select());


        expenseAdapter = new ExpenseAdapter(requireContext(), expenses);
        recyclerExpense.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerExpense.setAdapter(expenseAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Your method to load expenses from DB
        loadExpense();

    }

    public void updateDate() {
        String dateFormat = "dd MMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        currentDate.setText(simpleDateFormat.format(calendar.getTime()));
    }

    public void loadExpense() {
        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).collection("expenses")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    expenses.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        ExpenseModel expense = doc.toObject(ExpenseModel.class);
                        expenses.add(expense);
                    }

                    TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                    if (selectedTab != null) {
                        String tabText = selectedTab.getText().toString().toLowerCase();
                        temporary = filterExpense(tabText);
                        expenses.clear();
                        expenses.addAll(temporary);
                        expenseAdapter.notifyDataSetChanged();
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to fetch expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    public ArrayList<ExpenseModel> filterExpense(String mode) {
        ArrayList<ExpenseModel> filteredList = new ArrayList<>();

        for (ExpenseModel expense :
                expenses) {
            Date expenseDate = helper.parseDate(expense.getDate());
            if (expenseDate == null) continue;

            Calendar expenseCalender = Calendar.getInstance();
            expenseCalender.setTime(expenseDate);

            switch (mode) {
                case "daily":
                    if (helper.isSameDay(calendar, expenseCalender)) {
                        filteredList.add(expense);
                    }
                    break;
                case "weekly":
                    if (helper.isSameWeek(calendar, expenseCalender)) {
                        filteredList.add(expense);
                    }
                    break;
                case "monthly":
                    if (helper.isSameMonth(calendar, expenseCalender)) {
                        filteredList.add(expense);
                    }
                    break;
            }
        }
        return filteredList;
    }
}