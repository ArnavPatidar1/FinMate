package arnav.example.finmate.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import arnav.example.finmate.BuildConfig;
import arnav.example.finmate.adapters.ChatAdapter;
import arnav.example.finmate.databinding.FragmentChatBinding;
import arnav.example.finmate.helper.FirebaseHelper;
import arnav.example.finmate.helper.GeminiHelper;
import arnav.example.finmate.model.CategoryModel;
import arnav.example.finmate.model.ChatMessage;
import arnav.example.finmate.model.FinancialData;

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

                FirebaseHelper firebaseHelper = new FirebaseHelper();
                firebaseHelper.fetchTotalIncomeAndExpense(userId, new FirebaseHelper.FirebaseCallback() {
                    @Override
                    public void onSuccess(FinancialData financialData, Map<String, Double> categorizedExpenses) {
                        GeminiHelper geminiHelper = new GeminiHelper(BuildConfig.GEMINI_API_KEY);
                        geminiHelper.sendPersonalizedMessage(userMessage, financialData, categorizedExpenses, new GeminiHelper.GeminiCallback() {
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
                        requireActivity().runOnUiThread(() -> {
                            messageList.add(new ChatMessage("❌ Error: " + error, false));
                            chatAdapter.notifyItemInserted(messageList.size() - 1);
                            binding.chatRecyclerView.scrollToPosition(messageList.size() - 1);
                        });
                    }
                });
            }
        });
    }
}
