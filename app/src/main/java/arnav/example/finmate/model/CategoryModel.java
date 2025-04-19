package arnav.example.finmate.model;

public class CategoryModel {
    private String name;
    private int icon; // Resource name (e.g., "ic_food")

    private int categoryColor;

    public CategoryModel() {
    } // Needed for Firestore

    public CategoryModel(String name, int iconResName, int categoryColor) {
        this.name = name;
        this.icon = iconResName;
        this.categoryColor = categoryColor;
    }



    public String getName() {
        return name;
    }

    public int getIcon() {
        return icon;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setIcon(int iconResName) {
        this.icon = iconResName;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }
}
