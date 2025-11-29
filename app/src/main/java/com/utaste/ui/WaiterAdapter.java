package com.utaste.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.utaste.R;
import com.utaste.domain.user.User;
import java.util.List;

public class WaiterAdapter extends RecyclerView.Adapter<WaiterAdapter.WaiterViewHolder> {

    public interface OnDeleteClickListener {
        void onDeleteClick(User waiter);
    }

    private final List<User> items;
    private final OnDeleteClickListener onDeleteClickListener;

    public WaiterAdapter(List<User> items, OnDeleteClickListener onDeleteClickListener) {
        this.items = items;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public WaiterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_waiter, parent, false);
        return new WaiterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull WaiterViewHolder holder, int pos) {
        User u = items.get(pos);
        String fullName = ((u.firstName == null ? "" : u.firstName) + " " +
                (u.lastName == null ? "" : u.lastName)).trim();
        holder.waiterName.setText(fullName.isEmpty() ? "(No name)" : fullName);
        holder.waiterEmail.setText(u.email);
        holder.deleteButton.setOnClickListener(v -> onDeleteClickListener.onDeleteClick(u));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class WaiterViewHolder extends RecyclerView.ViewHolder {
        TextView waiterName, waiterEmail;
        ImageButton deleteButton;

        WaiterViewHolder(@NonNull View v) {
            super(v);
            waiterName = v.findViewById(R.id.waiter_name);
            waiterEmail = v.findViewById(R.id.waiter_email);
            deleteButton = v.findViewById(R.id.delete_button);
        }
    }
}
