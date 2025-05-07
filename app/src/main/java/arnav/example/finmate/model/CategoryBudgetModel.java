package arnav.example.finmate.model;

import com.google.firebase.Timestamp;

public class CategoryBudgetModel {
    private CategoryModel category;
    private double categoryBudget;
    private double categorySpent;
    private Timestamp startDate;
    private Timestamp endDate;
    private String categoryBudgetId;

    public CategoryBudgetModel() {}

    public CategoryBudgetModel(String categoryBudgetId, CategoryModel category, double categoryBudget, double categorySpent, Timestamp startDate, Timestamp endDate) {
        this.categoryBudgetId = categoryBudgetId;
        this.category = category;
        this.categoryBudget = categoryBudget;
        this.categorySpent = categorySpent;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CategoryBudgetModel(CategoryModel category, double categoryBudget, double categorySpent, Timestamp startDate, Timestamp endDate) {
        this.category = category;
        this.categoryBudget = categoryBudget;
        this.categorySpent = categorySpent;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public CategoryModel getCategory() {
        return category;
    }

    public double getCategoryBudget() {
        return categoryBudget;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setCategory(CategoryModel category) {
        this.category = category;
    }

    public void setCategoryBudget(double categoryBudget) {
        this.categoryBudget = categoryBudget;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }

    public double getCategorySpent() {
        return categorySpent;
    }

    public void setCategorySpent(double categorySpent) {
        this.categorySpent = categorySpent;
    }

    public String getCategoryBudgetId() {
        return categoryBudgetId;
    }

    public void setCategoryBudgetId(String categoryBudgetId) {
        this.categoryBudgetId = categoryBudgetId;
    }
}

