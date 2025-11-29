package com.utaste.ui;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.utaste.R;

// Cette activité permettra à l'administrateur de modifier ses informations.
public class ChangeProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // On va lier cette activité à son fichier de layout
        setContentView(R.layout.activity_change_profile);
    }
}
