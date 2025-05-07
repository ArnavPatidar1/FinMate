package arnav.example.finmate.model;

import com.google.firebase.Timestamp;

public class MonthlyBudgetModel {
    private String monthlyBudgetId;

    public MonthlyBudgetModel(String monthlyBudgetId, double budgetAmount, Timestamp startDate, Timestamp endDate) {
        this.monthlyBudgetId = monthlyBudgetId;
        this.budgetAmount = budgetAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getMonthlyBudgetId() {
        return monthlyBudgetId;
    }

    public void setMonthlyBudgetId(String monthlyBudgetId) {
        this.monthlyBudgetId = monthlyBudgetId;
    }

    private double budgetAmount;
    private Timestamp startDate;
    private Timestamp endDate;

    public MonthlyBudgetModel() {}

    public MonthlyBudgetModel(double budgetAmount, Timestamp startDate, Timestamp endDate) {
        this.budgetAmount = budgetAmount;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public double getBudgetAmount() {
        return budgetAmount;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setBudgetAmount(double budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }
}
