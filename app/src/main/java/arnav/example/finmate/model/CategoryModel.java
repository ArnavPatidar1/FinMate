package arnav.example.finmate.model;

public class CategoryModel {
    private String name;
    private String iconName; // Resource name (e.g., "ic_food")

    private String categoryColor;

    public CategoryModel() {
    } // Needed for Firestore

    public CategoryModel(String name, String iconResName, String categoryColor) {
        this.name = name;
        this.iconName = iconResName;
        this.categoryColor = categoryColor;
    }



    public String getName() {
        return name;
    }

    public String getIconName() {
        return iconName;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setIconName(String iconResName) {
        this.iconName = iconResName;
    }

    public String getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(String categoryColor) {
        this.categoryColor = categoryColor;
    }
}
