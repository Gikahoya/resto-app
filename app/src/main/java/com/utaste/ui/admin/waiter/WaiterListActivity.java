package com.utaste.ui.admin.waiter;

import android.content.Intent;                    // navigation vers le form
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;  // Activity de base
import androidx.recyclerview.widget.LinearLayoutManager; // liste verticale
import androidx.recyclerview.widget.RecyclerView;
import com.utaste.R;                               // R.layout / R.id
import com.utaste.ServiceLocator;                  // accès au service
import com.utaste.domain.user.User;                // modèle
import java.util.ArrayList;                        // dataset local
import java.util.List;


public class WaiterListActivity extends AppCompatActivity {
    private RecyclerView rv;                        // RecyclerView
    private TextView txtEmpty;                      // message "aucun serveur"
    private WaiterAdapter adapter;                  // adapter custom
    private final List<User> data = new ArrayList<>(); // dataset affiché

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);                            // cycle de vie
        setContentView(R.layout.activity_waiter_list);// layout de la liste

        rv = findViewById(R.id.rvWaiters);            // bind liste
        txtEmpty = findViewById(R.id.txtEmpty);       // bind message vide

        findViewById(R.id.btnBack).setOnClickListener(v -> finish()); // bouton retour

        findViewById(R.id.btnAdd).setOnClickListener(new View.OnClickListener() { // bouton "Ajouter"
            @Override public void onClick(View v) { openForm(null); }               // null = création
        });

        rv.setLayoutManager(new LinearLayoutManager(this)); // layout vertical
        adapter = new WaiterAdapter(                        // créer adapter
                data,
                new WaiterAdapter.OnEdit() {                      // callback edit
                    @Override public void edit(String email) { openForm(email); } // ouvrir en édition
                }
        );
        rv.setAdapter(adapter);                             // brancher l’adapter
    }

    @Override protected void onResume() {
        super.onResume();                                   // re-affichage de l’écran
        data.clear();                                       // reset dataset
        data.addAll(ServiceLocator.waiters().list());       // charger depuis service
        adapter.notifyDataSetChanged();                     // rafraîchir UI
        txtEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE); // message vide
        if (data.isEmpty()) {
            Toast.makeText(this, "No waiter for the moment", Toast.LENGTH_SHORT).show(); // feedback léger
        }
    }

    private void openForm(String email) {                 // utilitaire navigation
        Intent i = new Intent(this, UserFormActivity.class); // cible le form
        if (email != null) i.putExtra("email", email);      // passer l'email si édition
        startActivity(i);                                   // lancer Activity
    }
}
