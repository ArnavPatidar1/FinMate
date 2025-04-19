package arnav.example.finmate.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import arnav.example.finmate.R;
import arnav.example.finmate.databinding.AccountsRowBinding;
import arnav.example.finmate.model.AccountModel;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.AccountViewHolder> {

    private Context context;
    private ArrayList<AccountModel> accounts;

    public interface AccountClickListener {
        void onAccountSelected(AccountModel account);
    }

    AccountClickListener accountClickListener;
    public AccountAdapter(Context context, ArrayList<AccountModel> accounts, AccountClickListener accountClickListener) {
        this.context = context;
        this.accounts = accounts;
        this.accountClickListener = accountClickListener;
    }

    @NonNull
    @Override
    public AccountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(LayoutInflater.from(context).inflate(R.layout.accounts_row, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AccountViewHolder holder, int position) {
        AccountModel account = accounts.get(position);
        holder.binding.txtAccount.setText(account.getAccountName());
        holder.itemView.setOnClickListener(v -> {
            accountClickListener.onAccountSelected(account);
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }

    public class AccountViewHolder extends RecyclerView.ViewHolder {

        AccountsRowBinding binding;
        public AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = AccountsRowBinding.bind(itemView);
        }
    }
}
