package arnav.example.finmate.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arnav.example.finmate.R;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;

public class RecyclerViewExpenseAdapter extends RecyclerView.Adapter<RecyclerViewExpenseAdapter.ViewHolder> {

   private Context context;

   private ArrayList<ExpenseModel> expenses;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, CategoryModel> categoryMap = new HashMap<>();
    private FirebaseAuth auth = FirebaseAuth.getInstance();;

    public RecyclerViewExpenseAdapter(Context context, ArrayList<ExpenseModel> expenses) {
        this.context = context;
        this.expenses = expenses;

        db.collection("categories").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        CategoryModel category = doc.toObject(CategoryModel.class);
                        categoryMap.put(doc.getId(), category);
                    }

                    // Now expenseList has all your expenses
                    // You can notify your RecyclerView adapter here
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Failed to fetch expenses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.contact_row, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerViewExpenseAdapter.ViewHolder holder, int position) {


        CategoryModel category = categoryMap.get(expenses.get(position).getCategoryId());
        if (category != null) {
            holder.row_category.setText(category.getName());

            // ✅ Safely handle null iconResName
            String iconName = category.getIcon();
            if (iconName != null) {
                @SuppressLint("DiscouragedApi") int iconResId = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                if (iconResId != 0) {
                    holder.imgCategory.setImageResource(iconResId);
                } else {
                    holder.imgCategory.setImageResource(R.drawable.ic_other); // fallback if resource not found
                }
            } else {
                holder.imgCategory.setImageResource(R.drawable.ic_other); // fallback if icon name is null
            }
        } else {
            // ❗ If category itself is null, set some defaults
            holder.row_category.setText("Unknown");
            holder.imgCategory.setImageResource(R.drawable.ic_other);
        }

        holder.row_expense.setText("₹" + expenses.get(position).getAmount());
        holder.row_note.setText(expenses.get(position).getDescription());
    }


    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView imgCategory;
        TextView row_category, row_note, row_expense;

        LinearLayout lLayout;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory  = itemView.findViewById(R.id.imgCategory);
            row_category = itemView.findViewById(R.id.row_category);
            row_note = itemView.findViewById(R.id.row_note);
            row_expense = itemView.findViewById(R.id.row_expense);
            lLayout = itemView.findViewById(R.id.lLRow);
        }
    }
}
