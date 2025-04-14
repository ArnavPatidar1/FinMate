package arnav.example.finmate.model;

public class CategoryModel {
    private String id;
    private String name;
    private String icon; // Resource name (e.g., "ic_food")

    public CategoryModel() {} // Needed for Firestore

    public CategoryModel(String id, String name, String iconResName) {
        this.id = id;
        this.name = name;
        this.icon = iconResName;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getIcon() { return icon; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setIcon(String iconResName) { this.icon = iconResName; }
}
