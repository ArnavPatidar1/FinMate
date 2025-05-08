package arnav.example.finmate.model;

public class SavingGoalModel {
    private String savingGoalId;
    private CategoryModel categoryModel;
    private double targetAmount;
    private double savedAmount;
    private double remainingAmount;
    private String description;

    public SavingGoalModel(String savingGoalId, CategoryModel categoryModel, double targetAmount, double savedAmount, String description) {
        this.savingGoalId = savingGoalId;
        this.categoryModel = categoryModel;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.description = description;
        this.remainingAmount = this.targetAmount - this.savedAmount;
    }

    public SavingGoalModel(CategoryModel categoryModel, double targetAmount, double savedAmount, String description) {
        this.categoryModel = categoryModel;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.description = description;
        this.remainingAmount = this.targetAmount - this.savedAmount;
    }

    public SavingGoalModel(String savingGoalId, CategoryModel categoryModel, double targetAmount, double savedAmount) {
        this.savingGoalId = savingGoalId;
        this.categoryModel = categoryModel;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.remainingAmount = this.targetAmount - this.savedAmount;
    }

    public SavingGoalModel(CategoryModel categoryModel, double targetAmount, double savedAmount) {
        this.categoryModel = categoryModel;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.remainingAmount = this.targetAmount - this.savedAmount;
    }

    public SavingGoalModel() {}

    public String getSavingGoalId() {
        return savingGoalId;
    }

    public void setSavingGoalId(String savingGoalId) {
        this.savingGoalId = savingGoalId;
    }

    public CategoryModel getCategoryModel() {
        return categoryModel;
    }

    public void setCategoryModel(CategoryModel categoryModel) {
        this.categoryModel = categoryModel;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        this.targetAmount = targetAmount;
    }

    public double getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(double savedAmount) {
        this.savedAmount = savedAmount;
    }

    public double getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
