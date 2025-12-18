package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GenerationActivity extends AppCompatActivity implements View.OnClickListener {

    private Button btnGnr;
    private EditText champTheme, champNbquest;

    private static final String API_URL = "http://10.3.186.13:3001/quiz";
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generation);

        btnGnr = findViewById(R.id.btnGeneration);
        champTheme = findViewById(R.id.champTheme);
        champNbquest = findViewById(R.id.champNbquest);

        btnGnr.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnGnr) {
            handleGenerate();
        }
    }

    private void handleGenerate() {
        if (isLoading) return;

        String theme = champTheme.getText().toString().trim();
        String nombre = champNbquest.getText().toString().trim();

        if (theme.isEmpty() || nombre.isEmpty()) {
            showAlert("Veuillez remplir tous les champs !");
            return;
        }

        isLoading = true;
        btnGnr.setEnabled(false);
        btnGnr.setText("Génération en cours...");

        JsonObject jsonBody = new JsonObject();
        jsonBody.addProperty("subject", theme);
        jsonBody.addProperty("count", Integer.parseInt(nombre));

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    isLoading = false;
                    btnGnr.setEnabled(true);
                    btnGnr.setText("Générer");
                    showAlert("Erreur de connexion");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    isLoading = false;
                    btnGnr.setEnabled(true);
                    btnGnr.setText("Générer");
                });

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            showAlert("Erreur serveur: " + response.code())
                    );
                    return;
                }

                try {
                    String responseData = response.body().string();

                    // Parser la réponse
                    Gson gson = new Gson();
                    JsonObject quizData = gson.fromJson(responseData, JsonObject.class);

                    // Passer à l'activité suivante avec toutes les données
                    runOnUiThread(() -> {
                        Intent toGenere = new Intent(GenerationActivity.this, GenereActivity.class);

                        // Passer le thème et les données du quiz
                        toGenere.putExtra("theme", theme);
                        toGenere.putExtra("quiz_data", responseData);
                        toGenere.putExtra("nombre_questions", Integer.parseInt(nombre));

                        startActivity(toGenere);
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            showAlert("Erreur lors du traitement")
                    );
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(String message) {
        runOnUiThread(() ->
                Toast.makeText(GenerationActivity.this, message, Toast.LENGTH_LONG).show()
        );
    }
}