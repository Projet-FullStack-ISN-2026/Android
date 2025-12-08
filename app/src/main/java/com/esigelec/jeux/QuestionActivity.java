package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class QuestionActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnValid;
    Button btnSuiv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_question);

        btnValid = findViewById(R.id.btnValidation);
        btnValid.setOnClickListener(this);


        btnSuiv = findViewById(R.id.btnSuivant);
        btnSuiv.setOnClickListener(this);

    }

    public void onClick(View v) {
        if (v == btnValid) {
            Intent toClassement = new Intent(QuestionActivity.this, ClassementActivity.class);
            startActivity(toClassement);
        } else if (v == btnSuiv) {
            Intent toQuestion = new Intent(QuestionActivity.this, ClassementActivity.class);
            startActivity(toQuestion);
        }

    }
}