package arnav.example.finmate.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.FragmentAnalysisBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;

public class AnalysisFragment extends Fragment {

    private FragmentAnalysisBinding binding;
    private Calendar calender;
    private FirebaseFirestore db;
    Date startDate, endDate;
    String userId;
    PieChart pieChart;
    List<PieEntry> entries = new ArrayList<>();
    boolean isIncome = false;

    public AnalysisFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAnalysisBinding.inflate(inflater);

        binding.btnExpense.setSelected(true);

        binding.btnExpense.setOnClickListener(v -> {
            binding.btnExpense.setSelected(true);
            binding.btnIncome.setSelected(false);
            isIncome = false;
            getTransactions(startDate, endDate, isIncome);

        });

        binding.btnIncome.setOnClickListener(v -> {
            binding.btnIncome.setSelected(true);
            binding.btnExpense.setSelected(false);
            isIncome = true;
            getTransactions(startDate, endDate, isIncome);

        });

        calender = Calendar.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.previousDate.setOnClickListener(v -> adjustCalendar(-1));
        binding.nextDate.setOnClickListener(v -> adjustCalendar(1));


        pieChart = binding.pieChart;
        startDate = Backend.getStartOfMonth((Calendar) calender.clone());
        endDate = Backend.getEndOfMonth((Calendar) calender.clone());
        monthDateFormat(); // This sets text like "Apr 2025"


        binding.rangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();

                switch (selectedItem) {
                    case "Monthly":
                        startDate = Backend.getStartOfMonth((Calendar) calender.clone());
                        endDate = Backend.getEndOfMonth((Calendar) calender.clone());
                        calender = Calendar.getInstance();
                        adjustCalendar(0);
                        monthDateFormat();
                        break;

                    case "Weekly":
                        startDate = Backend.getStartOfWeek((Calendar) calender.clone());
                        endDate = Backend.getEndOfWeek((Calendar) calender.clone());
                        calender = Calendar.getInstance();
                        adjustCalendar(0);
                        weekDateFormat();
                        break;
                    case "Daily":
                        startDate = Backend.getStartOfDay((Calendar) calender.clone());
                        endDate = Backend.getEndOfDay((Calendar) calender.clone());
                        calender = Calendar.getInstance();
                        adjustCalendar(0);
                        dailyDateFormat();
                        break;
                }

                getTransactions(startDate, endDate, isIncome);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        getTransactions(startDate, endDate, isIncome);


        return binding.getRoot();
    }

    private void dailyDateFormat() {
        String dateFormat = "dd MMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        binding.currentDate.setText(simpleDateFormat.format(calender.getTime()));

    }

    private void weekDateFormat() {
        int weekOfMonth = calender.get(Calendar.WEEK_OF_MONTH);
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        String month = monthFormat.format(calender.getTime());

        String formatted = month + "-W" + weekOfMonth;
        binding.currentDate.setText(formatted);

    }

    private void monthDateFormat() {
        String dateFormat = "MMM yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
        binding.currentDate.setText(simpleDateFormat.format(calender.getTime()));
    }

    private void adjustCalendar(int amount) {
        String selectedRange = binding.rangeSpinner.getSelectedItem().toString();

        switch (selectedRange) {
            case "Daily":
                calender.add(Calendar.DAY_OF_MONTH, amount);
                startDate = Backend.getStartOfDay((Calendar) calender.clone());
                endDate = Backend.getEndOfDay((Calendar) calender.clone());
                dailyDateFormat();
                break;

            case "Weekly":
                calender.add(Calendar.WEEK_OF_MONTH, amount);
                startDate = Backend.getStartOfWeek((Calendar) calender.clone());
                endDate = Backend.getEndOfWeek((Calendar) calender.clone());
                weekDateFormat();
                break;

            case "Monthly":
                calender.add(Calendar.MONTH, amount);
                startDate = Backend.getStartOfMonth((Calendar) calender.clone());
                endDate = Backend.getEndOfMonth((Calendar) calender.clone());
                monthDateFormat();
                break;
        }

        getTransactions(startDate, endDate, isIncome);
    }

    private Date getEndDateForCurrentSelection(Calendar calendar) {
        if (binding.rangeSpinner.getSelectedItem().toString().equals("Daily")) {
            return calendar.getTime();
        } else if (binding.rangeSpinner.getSelectedItem().toString().equals("Weekly")) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
            return calendar.getTime();
        } else if (binding.rangeSpinner.getSelectedItem().toString().equals("Monthly")) {
            calendar.add(Calendar.MONTH, 1);
            return calendar.getTime();
        }
        return calendar.getTime();
    }

    private void getTransactions(Date startDate, Date endDate, boolean isIncome) {
        db.collection("users")
                .document(userId)
                .collection("expenses")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<PieEntry> entrySet = new ArrayList<>();
                    Map<String, Double> categorySums = new HashMap<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ExpenseModel expense = document.toObject(ExpenseModel.class);
                        if (expense == null || expense.getCategory() == null) continue;

                        if (expense.isIncome() != isIncome) continue;
                        Date timestampDate = expense.getTimestamp().toDate();
                        if (timestampDate.before(startDate) || timestampDate.after(endDate)) continue;

                        String categoryName = expense.getCategory().getName();
                        double amount = expense.getAmount();

                        double currentTotal = categorySums.getOrDefault(categoryName, 0.0);
                        categorySums.put(categoryName, currentTotal + amount);
                    }

                    for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
                        entrySet.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
                    }

                    if (entrySet.isEmpty()) {
                        pieChart.clear();
                        pieChart.setNoDataText("No data available for this range.");
                        pieChart.invalidate(); // <- This makes sure "no data" message is shown
                    } else {
                        updatePieChart(entrySet);
                    }
                });

    }

    /*
    .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .whereEqualTo("income", isIncome)
                */


    private void updatePieChart(List<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, isIncome ? "Income Transactions" : "Expense Transactions");
        dataSet.setSliceSpace(4f);
        dataSet.setSelectionShift(5f);
        int[] pieColors = getResources().getIntArray(R.array.pie_chart_colors);

        List<Integer> colors = new ArrayList<>();
        for (int color : pieColors) {
            colors.add(color);
        }

        dataSet.setColors(colors);


        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLinePart1Length(0.6f); // First part of line
        dataSet.setValueLinePart2Length(0.4f); // Second part of line
        dataSet.setValueLineColor(Color.BLACK); // Line color
        dataSet.setValueLineWidth(2f); // Line thickness


        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);
        data.setValueFormatter(new PercentFormatter(pieChart));
        data.setValueFormatter(new ValueFormatter() {
            @SuppressLint("DefaultLocale")
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                return entry.getLabel() + " - " + String.format("%.1f", value) + "%";
            }
        });

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setExtraOffsets(10, 15, 10, 10);
        pieChart.setCenterText(isIncome ? "Income" : "Expenses");
        pieChart.setCenterTextSize(18f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleColor(Color.LTGRAY);
        pieChart.animateY(1000, Easing.EaseInOutCubic);

        Legend legend = pieChart.getLegend();

        legend.setWordWrapEnabled(true);  // Wrap to next line
        legend.setMaxSizePercent(0.6f);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setTextSize(14f);
        legend.setTextColor(Color.DKGRAY);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(12f);
        legend.setDrawInside(false);
        legend.setYOffset(40f);
        legend.setFormToTextSpace(8f);

        pieChart.invalidate();
    }

}