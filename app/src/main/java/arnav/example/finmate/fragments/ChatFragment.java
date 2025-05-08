package arnav.example.finmate.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import arnav.example.finmate.R;
import arnav.example.finmate.adapters.ChatAdapter;
import arnav.example.finmate.databinding.FragmentChatBinding;
import arnav.example.finmate.helper.FirebaseHelper;
import arnav.example.finmate.helper.GeminiHelper;
import arnav.example.finmate.model.CategoryBudgetModel;
import arnav.example.finmate.model.ChatMessage;
import arnav.example.finmate.model.FinancialData;
import arnav.example.finmate.model.MonthlyBudgetModel;
import arnav.example.finmate.model.SavingGoalModel;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;
    private GeminiHelper geminiHelper;
    private FirebaseHelper firebaseHelper;

    public ChatFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChatBinding.inflate(inflater);
        addSuggestionChips();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize message list and adapter
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        binding.chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.chatRecyclerView.setAdapter(chatAdapter);

        binding.messageEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.chatRecyclerView.postDelayed(() -> {
                    if (!messageList.isEmpty()) {
                        binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                    }
                }, 200);
            }
        });

        // Initialize helpers
        geminiHelper = new GeminiHelper("AIzaSyA_OWMvVy2hMgZW2wU_jBgNAH0YSY0zzRs");
        firebaseHelper = new FirebaseHelper();

        // Handle send button click
        binding.sendButton.setOnClickListener(v -> {
            String userMessage = binding.messageEditText.getText().toString().trim();

            if (!TextUtils.isEmpty(userMessage)) {
                messageList.add(new ChatMessage(userMessage, true));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                binding.messageEditText.setText("");

                fetchAllFinancialDataAndSendToGemini(userId, userMessage);
            }
        });
    }

    private void fetchAllFinancialDataAndSendToGemini(String userId, String userMessage) {
        firebaseHelper.fetchTotalIncomeAndExpense(userId, new FirebaseHelper.FirebaseCallback() {
            @Override
            public void onSuccess(FinancialData financialData, Map<String, Double> categorizedExpenses) {

                firebaseHelper.fetchMonthlyBudget(userId, new FirebaseHelper.FirebaseMonthlyBudgetCallback() {
                    @Override
                    public void onSuccess(MonthlyBudgetModel monthlyBudget) {

                        firebaseHelper.fetchCategoryBudgets(userId, new FirebaseHelper.FirebaseCategoryBudgetCallback() {
                            @Override
                            public void onSuccess(List<CategoryBudgetModel> categoryBudgets) {

                                firebaseHelper.fetchSavingGoals(userId, new FirebaseHelper.FirebaseSavingGoalCallback() {
                                    @Override
                                    public void onSuccess(List<SavingGoalModel> savingGoals) {

                                        // ✅ Construct the full prompt
                                        StringBuilder prompt = new StringBuilder();
                                        prompt.append("User's financial summary:\n");
                                        prompt.append("- Total Income: ₹").append(financialData.getTotalIncome()).append("\n");
                                        prompt.append("- Total Expenses: ₹").append(financialData.getTotalExpense()).append("\n\n");

                                        prompt.append("Category-wise Expenses:\n");
                                        for (Map.Entry<String, Double> entry : categorizedExpenses.entrySet()) {
                                            prompt.append("- ").append(entry.getKey()).append(": ₹").append(entry.getValue()).append("\n");
                                        }

                                        prompt.append("\nMonthly Budget: ₹").append(monthlyBudget.getBudgetAmount()).append("\n");

                                        prompt.append("\nCategory Budgets:\n");
                                        for (CategoryBudgetModel cb : categoryBudgets) {
                                            prompt.append("- ").append(cb.getCategory().getName())
                                                    .append(": ₹").append(cb.getCategoryBudget()).append("\n");
                                        }

                                        prompt.append("\nSaving Goals:\n");
                                        for (SavingGoalModel sg : savingGoals) {
                                            prompt.append("- ").append(sg.getCategoryModel().getName())
                                                    .append(": Target ₹").append(sg.getTargetAmount())
                                                    .append(", Saved ₹").append(sg.getSavedAmount()).append("\n");
                                        }

                                        prompt.append("\n\nUser's query: ").append(userMessage).append("\n");
                                        prompt.append("Based on all this data, provide helpful suggestions or tips related to finances.");

                                        // ✅ Send to Gemini
                                        geminiHelper.sendMessage(prompt.toString(), new GeminiHelper.GeminiCallback() {
                                            @Override
                                            public void onResponse(String reply) {
                                                requireActivity().runOnUiThread(() -> {
                                                    messageList.add(new ChatMessage(reply, false));
                                                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                                                    binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                                                });
                                            }

                                            @Override
                                            public void onError(String error) {
                                                requireActivity().runOnUiThread(() -> {
                                                    messageList.add(new ChatMessage("❌ Error: " + error, false));
                                                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                                                    binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                                                });
                                            }
                                        });

                                    }

                                    @Override
                                    public void onError(String error) {
                                        showError(error);
                                    }
                                });

                            }

                            @Override
                            public void onError(String error) {
                                showError(error);
                            }
                        });

                    }

                    @Override
                    public void onError(String error) {
                        showError(error);
                    }
                });
            }

            @Override
            public void onError(String error) {
                showError(error);
            }
        });
    }

    private void showError(String error) {
        requireActivity().runOnUiThread(() -> {
            messageList.add(new ChatMessage("❌ Error: " + error, false));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
        });
    }

    private void addSuggestionChips() {
        String[] suggestions = {
                "Where am I overspending?",
                "How can I save more?",
                "Summarize my spending by category",
                "What’s my financial overview this month?"
        };

        for (String suggestion : suggestions) {
            AppCompatButton chip = new AppCompatButton(requireContext());
            chip.setText(suggestion);
            chip.setTextSize(14);
            chip.setAllCaps(false);
            chip.setBackgroundResource(R.drawable.suggestion_chip_background); // optional styling
            chip.setPadding(32, 8, 32, 8);
            // Set margin between chips
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 16, 0); // 16dp space to the right of each chip
            chip.setLayoutParams(params);

            chip.setOnClickListener(v -> {
                binding.messageEditText.setText(suggestion);
                binding.messageEditText.requestFocus();
                binding.messageEditText.setSelection(suggestion.length());
            });

            binding.suggestionLayout.addView(chip);
        }
    }


}
