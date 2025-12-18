package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import com.esigelec.jeux.HelperWS;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WaitingActivity extends AppCompatActivity {

    private TextView tvQuizTitle, tvWaitingMessage;
    private Button btnCancel;
    private String quizId;
    private String quizTitle;
    private Handler handler;
    private Runnable checkStatusRunnable;

    // URL pour vérifier le statut du quiz
    private String STATUS_URL = "http://10.3.70.14:8080/quiz"; // À compléter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting);

        // Récupérer les données du quiz
        Intent intent = getIntent();
        quizId = intent.getStringExtra("quiz_id");
        quizTitle = intent.getStringExtra("quiz_title");

        // Initialiser les vues
        tvQuizTitle = findViewById(R.id.tvQuizTitle);
        tvWaitingMessage = findViewById(R.id.tvWaitingMessage);
        btnCancel = findViewById(R.id.btnCancel);

        // Afficher le titre du quiz
        tvQuizTitle.setText(quizTitle);

        // Bouton annuler
        btnCancel.setOnClickListener(v -> {
            stopCheckingStatus();
            finish();
        });

        // Démarrer la vérification du statut
        handler = new Handler();
        startCheckingStatus();
    }

    private void startCheckingStatus() {
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                checkQuizStatus();
                // Vérifier toutes les 5 secondes
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(checkStatusRunnable);
    }

    private void stopCheckingStatus() {
        if (handler != null && checkStatusRunnable != null) {
            handler.removeCallbacks(checkStatusRunnable);
        }
    }

    private void checkQuizStatus() {
        String url = STATUS_URL + quizId + "/status"; // Adapter selon votre API

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Ne rien faire en cas d'échec, on réessaiera
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    // Analyser la réponse pour voir si le quiz a commencé
                    // Supposons que la réponse contient {"status": "STARTED"}

                    if (responseData.contains("\"status\":\"STARTED\"") ||
                            responseData.contains("STARTED")) {

                        runOnUiThread(() -> {
                            stopCheckingStatus();
                            // Démarrer l'activité du quiz
                            Intent intent = new Intent(WaitingActivity.this, QuestionActivity.class);
                            intent.putExtra("quiz_id", quizId);
                            startActivity(intent);
                            finish();
                        });
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCheckingStatus();
    }
}