package com.utaste.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.utaste.R;
import com.utaste.domain.user.Role;
import com.utaste.domain.user.User;
import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> displayItems = new ArrayList<>();
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    public UserAdapter(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    public void setData(List<User> users) {
        displayItems.clear();

        User admin = null;
        User chef = null;
        List<User> waiters = new ArrayList<>();

        for (User user : users) {
            if (user.role == Role.ADMIN) {
                admin = user;
            } else if (user.role == Role.CHEF) {
                chef = user;
            } else if (user.role == Role.WAITER) {
                waiters.add(user);
            }
        }

        if (admin != null) {
            displayItems.add("Admin");
            displayItems.add(admin);
        }

        if (chef != null) {
            displayItems.add("Chef");
            displayItems.add(chef);
        }

        if (!waiters.isEmpty()) {
            displayItems.add("Waiters");
            displayItems.addAll(waiters);
        }

        notifyDataSetChanged();
    }

    // ... (Le reste de la classe est correct et ne change pas) ...

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.header.setText((String) displayItems.get(position));
        } else {
            UserViewHolder userHolder = (UserViewHolder) holder;
            User user = (User) displayItems.get(position);
            String fullName = (user.firstName + " " + user.lastName).trim();
            userHolder.name.setText(fullName.isEmpty() ? "(No name)" : fullName);
            userHolder.email.setText(user.email != null ? user.email : user.id);
            userHolder.itemView.setOnClickListener(v -> onUserClickListener.onUserClick(user));
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        HeaderViewHolder(View view) {
            super(view);
            header = view.findViewById(R.id.tvHeader);
        }
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView name, email;
        UserViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.txtName);
            email = view.findViewById(R.id.txtEmail);
        }
    }
}
