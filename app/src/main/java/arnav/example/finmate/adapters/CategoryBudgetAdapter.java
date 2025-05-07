package arnav.example.finmate.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.CategoryWiseBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryBudgetModel;
import arnav.example.finmate.model.CategoryModel;

public class CategoryBudgetAdapter extends RecyclerView.Adapter<CategoryBudgetAdapter.CategoryBudgetViewHolder> {

    private Context context;
    private ArrayList<CategoryBudgetModel> categoryList;

    public CategoryBudgetAdapter(Context context, ArrayList<CategoryBudgetModel> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    public ArrayList<CategoryBudgetModel> getCategoryList() {
        return categoryList;
    }

    public double getBudgetAmount(int position) {
        if (position >= 0 && position < categoryList.size()) {
            return categoryList.get(position).getCategoryBudget();
        }
        return 0.0;
    }



    @NonNull
    @Override
    public CategoryBudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CategoryBudgetViewHolder(LayoutInflater.from(context).inflate(R.layout.category_wise, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryBudgetViewHolder holder, int position) {
        CategoryBudgetModel model = categoryList.get(position);
        CategoryModel categoryModel = model.getCategory();
        holder.binding.rowCategory.setText(categoryModel.getName());
        int iconResId = Backend.getIconResId(categoryModel.getIconName());
        holder.binding.imgCategory.setImageResource(iconResId);
        holder.binding.imgCategory.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.budget_bg));
        DecimalFormat format = new DecimalFormat("0.00");
        holder.binding.spentAmount.setText(format.format(model.getCategorySpent()) + "/");
        holder.binding.budgetAmount.setText(format.format(model.getCategoryBudget()));

        if (holder.binding.budgetAmount.getTag() instanceof TextWatcher) {
            holder.binding.budgetAmount.removeTextChangedListener((TextWatcher) holder.binding.budgetAmount.getTag());
        }
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                double amt = 0;
                try {
                    amt = Double.parseDouble(s.toString());
                } catch (NumberFormatException ignored) {}
                model.setCategoryBudget(amt);
            }
        };
        holder.binding.budgetAmount.addTextChangedListener(watcher);
        holder.binding.budgetAmount.setTag(watcher);


        int percentUsed = model.getCategoryBudget() > 0
                ? (int) ((model.getCategorySpent() / model.getCategoryBudget()) * 100)
                : 0;
        if (percentUsed > 100) percentUsed = 100;
        holder.binding.categoryProgress.setProgress(percentUsed);

    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    @Override
    public void onViewRecycled(@NonNull CategoryBudgetViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.binding.budgetAmount.getTag() instanceof TextWatcher) {
            holder.binding.budgetAmount.removeTextChangedListener((TextWatcher) holder.binding.budgetAmount.getTag());
        }
    }


    public class CategoryBudgetViewHolder extends RecyclerView.ViewHolder {

        CategoryWiseBinding binding;
        public CategoryBudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = CategoryWiseBinding.bind(itemView);
        }
    }
}
