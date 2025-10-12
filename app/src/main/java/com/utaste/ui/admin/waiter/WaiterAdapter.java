package com.utaste.ui.admin.waiter;               // même package que les activities

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;                // bouton Edit
import android.widget.TextView;                   // textes nom/email
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView; // base adapter
import com.utaste.R;                               // R.layout.item_waiter
import com.utaste.domain.user.User;                // modèle
import java.util.List;                             // dataset


public class WaiterAdapter extends RecyclerView.Adapter<WaiterAdapter.VH> {
    public interface OnEdit { void edit(String email); } // callback quand on clique edit
    private final List<User> items;                       // liste de Users
    private final OnEdit onEdit;                         // action à exécuter

    public WaiterAdapter(List<User> items, OnEdit onEdit) {
        this.items = items;                                 // garder dataset
        this.onEdit = onEdit;                               // garder callback
    }

    static class VH extends RecyclerView.ViewHolder {     // ViewHolder = refs des vues par item
        TextView txtName, txtEmail;                         // refs TextView
        ImageButton btnEdit;                                // ref bouton Edit
        VH(@NonNull View v) {
            super(v);
            txtName  = v.findViewById(R.id.txtName);          // bind nom
            txtEmail = v.findViewById(R.id.txtEmail);         // bind email
            btnEdit  = v.findViewById(R.id.btnEdit);          // bind bouton
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())   // inflater du layout
                .inflate(R.layout.item_waiter, parent, false);
        return new VH(v);                                   // retourne un holder
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        User u = items.get(pos);                            // user courant
        String full = ((u.firstName==null?"":u.firstName) + " " +
                (u.lastName==null?"":u.lastName)).trim(); // compose "Prénom Nom"
        h.txtName.setText(full.isEmpty() ? "(Sans nom)" : full); // fallback si vide
        h.txtEmail.setText(u.email);                        // affiche email
        h.btnEdit.setOnClickListener(new View.OnClickListener() { // clic sur Edit
            @Override public void onClick(View v) { onEdit.edit(u.email); } // remonte l'email
        });
    }

    @Override public int getItemCount() { return items.size(); } // nb d'items
}
