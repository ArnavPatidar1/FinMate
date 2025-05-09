package arnav.example.finmate.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.DisplayTransactionRowBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private ArrayList<ExpenseModel> expenses;
    private Context context;
    private FirebaseFirestore db;
    private String userId;


    public ExpenseAdapter(Context context, ArrayList<ExpenseModel> expenses, FirebaseFirestore db, String userId) {
        this.expenses = expenses;
        this.context = context;
        this.db = db;
        this.userId = userId;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ExpenseViewHolder(LayoutInflater.from(context).inflate(R.layout.display_transaction_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        ExpenseModel expense = expenses.get(position);
        holder.binding.rowAccount.setText(expense.getAccountName());
        holder.binding.rowAccount.setBackgroundTintList(context.getColorStateList(Backend.getAccountColor(expense.getAccountName())));
        holder.binding.rowDate.setText(expense.getDate());
        holder.binding.rowAmount.setText(String.valueOf(expense.getAmount()));
        CategoryModel category = expense.getCategory();
        int iconResId = Backend.getIconResId(category.getIconName());
        holder.binding.imgCategory.setImageResource(iconResId);
        holder.binding.imgCategory.setBackgroundTintList(Backend.getColorStateList(context, category.getCategoryColor()));
        holder.binding.rowCategory.setText(category.getName());
        if (expense.isIncome()) {
            holder.binding.rowAmount.setTextColor(context.getColor(R.color.dark_green));
        } else {
            holder.binding.rowAmount.setTextColor(context.getColor(R.color.red));
        }

        holder.binding.transactionRow.setOnLongClickListener(v -> {

            new AlertDialog.Builder(context)
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this saving goal?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Backend.deleteExpense(db, userId, expense.getId(),
                                aVoid -> {
                                    expenses.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Goal deleted", Toast.LENGTH_SHORT).show();
                                },
                                e -> Toast.makeText(context, "Failed to delete goal", Toast.LENGTH_SHORT).show()
                        );
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        });

    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    public class ExpenseViewHolder extends RecyclerView.ViewHolder {
        DisplayTransactionRowBinding binding;
        public ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DisplayTransactionRowBinding.bind(itemView);
        }
    }
}
