package arnav.example.finmate.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.adapters.CategoryAdapter;
import arnav.example.finmate.adapters.CategoryAdapter2;
import arnav.example.finmate.adapters.SavingGoalAdapter;
import arnav.example.finmate.databinding.AddGoalsDialogBoxBinding;
import arnav.example.finmate.databinding.CategoryDialogBoxBinding;
import arnav.example.finmate.databinding.FragmentSavingGoalBinding;
import arnav.example.finmate.helper.Backend;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.SavingGoalModel;


public class SavingGoalFragment extends Fragment {

    private FragmentSavingGoalBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;
    private ArrayList<SavingGoalModel> savingGoals = new ArrayList<>();
    private CategoryModel categoryModel;
    SavingGoalAdapter savingGoalAdapter;

    public SavingGoalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentSavingGoalBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = auth.getCurrentUser().getUid();


        Backend.setGoalsCategories();
        loadSavingGoals();
        binding.btnAddGoals.setOnClickListener(v -> {
            // Inflate dialog layout using ViewBinding
            AddGoalsDialogBoxBinding dialogBinding = AddGoalsDialogBoxBinding.inflate(getLayoutInflater());

            Dialog dialog = new Dialog(requireContext());
            dialog.setContentView(dialogBinding.getRoot());

            dialogBinding.edtCategory.setFocusable(false);
            dialogBinding.edtCategory.setOnClickListener(view1 -> {
                // Inflate category dialog
                CategoryDialogBoxBinding categoryBinding = CategoryDialogBoxBinding.inflate(getLayoutInflater());

                AlertDialog categoryDialog = new AlertDialog.Builder(requireContext())
                        .setView(categoryBinding.getRoot())
                        .create();

                // Setup RecyclerView
                categoryBinding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // Or GridLayoutManager if preferred
                CategoryAdapter2 adapter = new CategoryAdapter2(requireContext(), Backend.goalsCategories, category -> {
                    categoryModel = category;
                    if (category.getName().equals("Other")) {
                        dialogBinding.edtDescription.setVisibility(View.VISIBLE);
                        if (dialogBinding.edtDescription.getText() != null) {
                            dialogBinding.edtCategory.setText(dialogBinding.edtDescription.getText());
                        } else {
                            dialogBinding.edtCategory.setText(category.getName());
                        }
                    } else {
                        dialogBinding.edtCategory.setText(category.getName());
                    }

                    categoryDialog.dismiss();
                });
                categoryBinding.categoryRecyclerView.setAdapter(adapter);

                categoryDialog.show();
            });

            dialogBinding.btnAddGoal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    double targetAmount = Double.parseDouble(String.valueOf(dialogBinding.edtAmount.getText()));
                    double savedAmount = Double.parseDouble(String.valueOf(dialogBinding.edtSaving.getText()));
                    SavingGoalModel savingGoalModel;
                    if (dialogBinding.edtDescription.getText() == null){
                        savingGoalModel = new SavingGoalModel(categoryModel, targetAmount, savedAmount);
                    } else {
                        String description = String.valueOf(dialogBinding.edtDescription.getText());
                        savingGoalModel = new SavingGoalModel(categoryModel, targetAmount, savedAmount, description);
                    }
                    Backend.addSavingGoal(db,userId,savingGoalModel,unused -> {
                        Toast.makeText(getContext(), "Saving Goal Sucessfully added", Toast.LENGTH_SHORT).show();
                    }, e -> {
                        Toast.makeText(getContext(), "Failed to add Saving Goal", Toast.LENGTH_SHORT).show();
                    });
                    savingGoals.add(savingGoalModel);
                    savingGoalAdapter.notifyItemInserted(savingGoals.size() - 1);

                    dialog.dismiss();
                }
            });

            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT, // Or fixed value like (int) getResources().getDimension(R.dimen._270sdp)
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
            }
            dialog.show();
        });

        savingGoalAdapter = new SavingGoalAdapter(getContext(), savingGoals, db, userId);
        binding.goalRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.goalRecyclerView.setAdapter(savingGoalAdapter);

    }

    private void loadSavingGoals() {
        Backend.getSavingGoals(db,userId,queryDocumentSnapshots -> {
            savingGoals.clear();
            if (!queryDocumentSnapshots.isEmpty()) {
                for (DocumentSnapshot documentSnapshot :
                        queryDocumentSnapshots) {
                    SavingGoalModel model = documentSnapshot.toObject(SavingGoalModel.class);
                    savingGoals.add(model);
                }
                savingGoalAdapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "loading sucessful", Toast.LENGTH_SHORT).show();
            }
        }, e -> Toast.makeText(getContext(), "Failed to load saving goals", Toast.LENGTH_SHORT).show());
    }
}