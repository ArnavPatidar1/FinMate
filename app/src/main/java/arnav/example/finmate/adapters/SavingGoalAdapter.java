package arnav.example.finmate.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.DisplaySavingGoalsBinding;
import arnav.example.finmate.databinding.UpdateSavingDialogBoxBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.SavingGoalModel;

public class SavingGoalAdapter extends RecyclerView.Adapter<SavingGoalAdapter.SavingGoalViewHolder> {
    private Context context;
    private ArrayList<SavingGoalModel> goals;
    private String userId;
    private FirebaseFirestore db;

    public SavingGoalAdapter(Context context, ArrayList<SavingGoalModel> goals, FirebaseFirestore db, String userId) {
        this.context = context;
        this.goals = goals;
        this.db = db;
        this.userId = userId;
    }

    @NonNull
    @Override
    public SavingGoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SavingGoalViewHolder(LayoutInflater.from(context).inflate(R.layout.display_saving_goals, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SavingGoalViewHolder holder, int position) {
        SavingGoalModel goalModel = goals.get(position);
        CategoryModel category = goalModel.getCategoryModel();
        int iconResId = Backend.getIconResId(category.getIconName());
        holder.binding.imgCategory.setImageResource(iconResId);
        holder.binding.imgCategory.setBackgroundTintList(Backend.getColorStateList(context, category.getCategoryColor()));
        holder.binding.txtGoalAmount.setText("₹" + goalModel.getTargetAmount());
        holder.binding.txtSavedAmount.setText("₹" + goalModel.getSavedAmount());
        holder.binding.txtRemainingAmount.setText("₹" + goalModel.getRemainingAmount());
        if (goalModel.getDescription() == null || goalModel.getDescription().isEmpty()) {
            holder.binding.txtCategoryName.setText(category.getName());
        } else {
            holder.binding.txtCategoryName.setText(goalModel.getDescription());
        }

        int percentUsed = goalModel.getTargetAmount() > 0
                ? (int) ((goalModel.getSavedAmount() / goalModel.getTargetAmount()) * 100)
                : 0;
        if (percentUsed > 100) percentUsed = 100;
        holder.binding.progressBar.setProgress(percentUsed);

        holder.binding.goalRow.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            UpdateSavingDialogBoxBinding dialogBoxBinding = UpdateSavingDialogBoxBinding.inflate(LayoutInflater.from(context));
            builder.setView(dialogBoxBinding.getRoot());

            AlertDialog dialog = builder.create();

            // Set data in dialog
            dialogBoxBinding.imgCategoryIcon.setImageResource(iconResId);
            dialogBoxBinding.imgCategoryIcon.setBackgroundTintList(Backend.getColorStateList(context, category.getCategoryColor()));
            dialogBoxBinding.txtTotalTarget.setText("₹" + goalModel.getTargetAmount());
            dialogBoxBinding.txtSavedAmount.setText("₹" + goalModel.getSavedAmount());
            dialogBoxBinding.txtRemainingAmount.setText("₹" + goalModel.getRemainingAmount());

            if (goalModel.getDescription() == null || goalModel.getDescription().isEmpty()) {
                dialogBoxBinding.txtGoalName.setText(category.getName());
            } else {
                dialogBoxBinding.txtGoalName.setText(goalModel.getDescription());
            }

            dialogBoxBinding.progressCircle.setIndeterminate(false);
            int progressPercent = (int) ((goalModel.getSavedAmount() / (float) goalModel.getTargetAmount()) * 100);
            dialogBoxBinding.progressCircle.setProgressCompat(progressPercent, true);

            // Handle Add Saving button click
            dialogBoxBinding.btnAddSaving.setOnClickListener(btnView -> {
                String addAmountStr = dialogBoxBinding.edtNewSaving.getText().toString().trim();
                if (addAmountStr.isEmpty()) {
                    dialogBoxBinding.edtNewSaving.setError("Enter amount");
                    return;
                }

                double addAmount = Double.parseDouble(addAmountStr);
                double newSaved = goalModel.getSavedAmount() + addAmount;
                double remaining = 0;

                int updatedProgress = (int) (newSaved / (float) goalModel.getTargetAmount()) * 100;
                dialogBoxBinding.progressCircle.setProgressCompat(progressPercent, true);
                remaining = goalModel.getTargetAmount() - newSaved;

                // Firestore update
                Map<String, Object> updates = new HashMap<>();
                updates.put("savedAmount", newSaved);
                updates.put("remainingAmount", remaining);

                double finalRemaining = remaining;
                db.collection("users").document(userId)
                        .collection("saving_goals").document(goalModel.getSavingGoalId())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            goalModel.setSavedAmount(newSaved);
                            goalModel.setRemainingAmount(finalRemaining);
                            notifyItemChanged(position);

                            DocumentReference userRef = db.collection("users").document(userId);
                            DocumentReference goalRef = db.collection("users").document(userId)
                                    .collection("saving_goals").document();
                            db.runTransaction(transaction -> {
                                        // Read current totalIncome
                                        DocumentSnapshot userSnapshot = transaction.get(userRef);
                                        Double currentIncome = userSnapshot.getDouble("totalIncome");
                                        if (currentIncome == null) currentIncome = 0.0;

                                        // Deduct saved amount from total income
                                        double updatedIncome = currentIncome - addAmount;
                                        transaction.update(userRef, "totalIncome", updatedIncome);


                                        return null;
                                    }).addOnSuccessListener(command -> {
                                    })
                                    .addOnFailureListener(e -> {
                                    });

                            Toast.makeText(context, "Saving updated", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(context, "Failed to update saving", Toast.LENGTH_SHORT).show();
                        });
            });

            // Fix dialog width after showing
            dialog.setOnShowListener(d -> {
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            });

            dialog.show();
        });


        holder.itemView.setOnLongClickListener(v ->

        {
            new AlertDialog.Builder(context)
                    .setTitle("Delete Goal")
                    .setMessage("Are you sure you want to delete this saving goal?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Backend.deleteSavingGoal(db, userId, goalModel.getSavingGoalId(),
                                aVoid -> {
                                    goals.remove(position);
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
        return goals.size();
    }

    public class SavingGoalViewHolder extends RecyclerView.ViewHolder {
        DisplaySavingGoalsBinding binding;

        public SavingGoalViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DisplaySavingGoalsBinding.bind(itemView);
        }
    }
}
