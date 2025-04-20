package arnav.example.finmate.adapters;

import android.content.Context;
import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.DisplayTransactionRowBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ExpenseModel;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    private ArrayList<ExpenseModel> expenses;
    private Context context;

    public ExpenseAdapter(Context context, ArrayList<ExpenseModel> expenses) {
        this.expenses = expenses;
        this.context = context;
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
        holder.binding.imgCategory.setImageResource(category.getIcon());
        holder.binding.imgCategory.setBackgroundTintList(context.getColorStateList(category.getCategoryColor()));
        holder.binding.rowCategory.setText(category.getName());
        if (expense.isIncome()) {
            holder.binding.rowAmount.setTextColor(context.getColor(R.color.dark_green));
        } else {
            holder.binding.rowAmount.setTextColor(context.getColor(R.color.red));
        }

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
