package arnav.example.finmate.model;

public class ExpenseModel {
    private String id;
    private CategoryModel category;
    private double amount;
    private String description;
    private String date;
    private String accountName;
    private boolean isIncome;

    public ExpenseModel() {
        // Required for Firestore
    }

    public ExpenseModel(CategoryModel category, double amount, String description, String date, String accountName, boolean isIncome) {
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.accountName = accountName;
        this.isIncome = isIncome;
    }

    public ExpenseModel(CategoryModel category, double amount, String date, String accountName, boolean isIncome) {
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.accountName = accountName;
        this.isIncome = isIncome;
    }

    public ExpenseModel(String id, CategoryModel category, double amount, String description, String date, String accountName, boolean isIncome) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.accountName = accountName;
        this.isIncome = isIncome;
    }

    public ExpenseModel(String id, CategoryModel category, double amount, String date, String accountName, boolean isIncome) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.accountName = accountName;
        this.isIncome = isIncome;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public CategoryModel getCategory() { return category; }
    public void setCategory(CategoryModel category) { this.category = category; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isIncome() { return isIncome; }
    public void setIncome(boolean income) { isIncome = income; }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }
}

