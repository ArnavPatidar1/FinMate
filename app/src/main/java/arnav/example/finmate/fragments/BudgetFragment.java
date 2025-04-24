package arnav.example.finmate.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.FragmentBudgetBinding;


public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;

    public BudgetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBudgetBinding.inflate(inflater);
        return binding.getRoot();
    }
}