package com.utaste.ui.admin;                 // paquet UI admin

import android.os.Bundle;                    // cycle de vie Activity
import android.text.TextUtils;               // helpers pour strings vides
import android.util.Patterns;                // regex email Android
import android.view.View;                    // callback onClick
import android.widget.EditText;              // champs texte
import android.widget.Toast;                 // petits messages
import com.utaste.ServiceLocator;            // callback classe ServiceLocator


import androidx.appcompat.app.AppCompatActivity; // Activity base

import com.utaste.R;                         // ressources (layout, id…)
import com.utaste.data.sqlite.UserDao;       // notre DAO SQLite

/**
 * AdminActivity — 3 actions:
 * Reset Password / Update Profile / Reset Database (users).
 */
public class AdminActivity extends AppCompatActivity {

    private UserDao dao;                      // accès à la DB users
    private EditText edtEmail;                // champ Email ciblé
    private EditText edtFirst;                // champ First name
    private EditText edtLast;                 // champ Last name
    private EditText edtNewPwd;               // champ New password

    @Override
    protected void onCreate(Bundle savedInstanceState) { // création de l’écran
        super.onCreate(savedInstanceState);              // base
        setContentView(R.layout.activity_admin);         // charge le layout XML

        // bind des vues par id
        edtEmail = findViewById(R.id.edtEmail);
        edtFirst = findViewById(R.id.edtFirst);
        edtLast  = findViewById(R.id.edtLast);
        edtNewPwd= findViewById(R.id.edtNewPwd);

        // initialiser le DAO
        dao = ServiceLocator.userDao(this);




    }

    // ============================ BOUTONS ============================

    public void onResetPassword(View v) {                 // click "Reset Password"
        String email = edtEmail.getText().toString().trim(); // lit email
        String newPwd = edtNewPwd.getText().toString();      // lit nv mdp

        if (!isValidEmail(email)) {                      // validation email
            toast("Invalid email"); return;
        }
        if (TextUtils.isEmpty(newPwd)) {                 // validation mdp
            toast("New password is required"); return;
        }
        if (!dao.exists(email)) {                        // user existe ?
            toast("User not found"); return;
        }

        int rows = dao.resetPassword(email, newPwd);     // UPDATE password
        toast(rows > 0 ? "Password reset" : "No change");// feedback
    }

    public void onUpdateProfile(View v) {                // click "Update Profile"
        String email = edtEmail.getText().toString().trim(); // email ciblé
        String first = edtFirst.getText().toString().trim(); // nv prénom
        String last  = edtLast.getText().toString().trim();  // nv nom

        if (!isValidEmail(email)) {                      // validation email
            toast("Invalid email"); return;
        }
        if (TextUtils.isEmpty(first) && TextUtils.isEmpty(last)) { // rien à changer ?
            toast("Nothing to update"); return;
        }
        if (!dao.exists(email)) {                        // user existe ?
            toast("User not found"); return;
        }

        int rows = dao.updateProfile(email, first, last);// UPDATE first/last
        toast(rows > 0 ? "Profile updated" : "No change");// feedback
    }

    public void onResetDatabase(View v) {                // click "Reset Database"
        dao.resetUsersTable();                           // drop + create table users
        toast("Users table reset");                      // feedback

        // si vous voulez wipe total (toutes tables) :
        // dao.resetWholeDatabase(this);
        // toast("Database file deleted");
    }


    private boolean isValidEmail(String email) {         // validation email Android
        return !TextUtils.isEmpty(email) &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void toast(String msg) {                     // petit helper de Toast
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
