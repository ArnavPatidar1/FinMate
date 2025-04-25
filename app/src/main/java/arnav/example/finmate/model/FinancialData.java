package arnav.example.finmate.model;

public class FinancialData {
    private double totalIncome;
    private double totalExpense;

    public FinancialData(double totalIncome, double totalExpense) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
    }

    public double getTotalIncome() {
        return totalIncome;
    }

    public double getTotalExpense() {
        return totalExpense;
    }
}

