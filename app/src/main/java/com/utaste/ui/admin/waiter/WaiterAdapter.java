package com.utaste.ui.admin.waiter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.utaste.R;
import com.utaste.domain.user.User;
import java.util.List;

public class WaiterAdapter extends RecyclerView.Adapter<WaiterAdapter.WaiterViewHolder> {

    private final List<User> waiters;
    private final OnEdit onEditCallback;

    public interface OnEdit {
        void edit(String email);
    }

    public WaiterAdapter(List<User> waiters, OnEdit onEditCallback) {
        this.waiters = waiters;
        this.onEditCallback = onEditCallback;
    }

    @NonNull
    @Override
    public WaiterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Crée la vue pour chaque ligne à partir du layout XML.
        // Assurez-vous d'avoir un layout nommé "activity_item_waiter.xml" dans res/layout.
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_waiter, parent, false);
        return new WaiterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WaiterViewHolder holder, int position) {
        // Récupère le serveur à la position actuelle
        User currentWaiter = waiters.get(position);

        // Met à jour les vues avec les données.
        // En se basant sur votre InMemoryUserRepository, les champs sont firstName, lastName et email.
        String fullName = currentWaiter.firstName + " " + currentWaiter.lastName;
        holder.txtName.setText(fullName);
        holder.txtEmail.setText(currentWaiter.email);

        // Ajoute un "listener" sur le bouton pour gérer le clic
        holder.btnEdit.setOnClickListener(v -> {
            if (onEditCallback != null) {
                onEditCallback.edit(currentWaiter.email);
            }
        });
    }

    @Override
    public int getItemCount() {
        return waiters != null ? waiters.size() : 0;
    }

    // Le "ViewHolder" qui contient les composants graphiques de chaque ligne
    public static class WaiterViewHolder extends RecyclerView.ViewHolder {
        public TextView txtName;
        public TextView txtEmail;
        public Button btnEdit;

        public WaiterViewHolder(View itemView) {
            super(itemView);
            // Lie les variables aux widgets du layout XML
            txtName = itemView.findViewById(R.id.txtName);
            txtEmail = itemView.findViewById(R.id.txtEmail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
