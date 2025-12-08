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

public class GenerationActivity extends AppCompatActivity implements View.OnClickListener{

    Button btnGnr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_generation);

        btnGnr = findViewById(R.id.btnGeneration);
        btnGnr.setOnClickListener(this);

    }

    public void onClick(View v){
        if(v== btnGnr){
            Intent toGenere = new Intent(GenerationActivity.this, GenereActivity.class);
            startActivity(toGenere);
        }

    }
}