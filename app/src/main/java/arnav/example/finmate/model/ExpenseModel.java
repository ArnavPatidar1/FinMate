package arnav.example.finmate.model;

public class ExpenseModel {
    private String id;
    private String categoryId;
    private double amount;
    private String description;
    private String date;
    private boolean isIncome;

    public ExpenseModel() {
        // Required for Firestore
    }

    public ExpenseModel(String id, String categoryId, double amount, String description, String date, boolean isIncome) {
        this.id = id;
        this.categoryId = categoryId;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.isIncome = isIncome;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }
}

