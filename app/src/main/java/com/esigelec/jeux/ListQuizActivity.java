package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListQuizActivity extends AppCompatActivity {

    private LinearLayout quizContainer;
    private String API_URL = "http://10.3.70.14:8080/quiz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listquiz);

        // Initialiser les vues
        quizContainer = findViewById(R.id.quizContainer);

        // Bouton retour
        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Charger les quiz au démarrage
        loadQuizzes();
    }

    private void loadQuizzes() {
        // Afficher un indicateur de chargement
        quizContainer.removeAllViews();

        TextView loadingText = new TextView(this);
        loadingText.setText("Chargement des quiz...");
        loadingText.setTextColor(getResources().getColor(android.R.color.white));
        loadingText.setTextSize(18);
        loadingText.setPadding(0, 40, 0, 40);
        loadingText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        quizContainer.addView(loadingText);

        // Créer la requête GET
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    quizContainer.removeAllViews();
                    showErrorMessage("Erreur de connexion au serveur");
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> quizContainer.removeAllViews());

                if (!response.isSuccessful()) {
                    runOnUiThread(() ->
                            showErrorMessage("Erreur serveur: " + response.code())
                    );
                    return;
                }

                try {
                    String responseData = response.body().string();
                    JsonArray quizzesArray = new Gson().fromJson(responseData, JsonArray.class);

                    runOnUiThread(() -> {
                        if (quizzesArray.size() == 0) {
                            showNoQuizzesMessage();
                        } else {
                            displayQuizzes(quizzesArray);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            showErrorMessage("Erreur de lecture des données")
                    );
                }
            }
        });
    }

    private void displayQuizzes(JsonArray quizzesArray) {
        for (int i = 0; i < quizzesArray.size(); i++) {
            JsonObject quiz = quizzesArray.get(i).getAsJsonObject();

            // Créer un bouton pour chaque quiz
            Button quizButton = new Button(this);

            // Style du bouton
            quizButton.setTextColor(getResources().getColor(android.R.color.white));
            quizButton.setTextSize(16);
            quizButton.setAllCaps(false);

            // Marge entre les boutons
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 0, 20);
            quizButton.setLayoutParams(params);

            // Déterminer le texte du bouton
            String buttonText = formatQuizButtonText(quiz, i + 1);
            quizButton.setText(buttonText);

            // Tag pour stocker l'ID du quiz
            String quizId = quiz.has("id") ? quiz.get("id").getAsString() : String.valueOf(i);
            quizButton.setTag(quizId);

            // Action au clic
            quizButton.setOnClickListener(v -> {
                String selectedQuizId = (String) v.getTag();
                String quizTitle = extractQuizTitle(quiz);

                // Aller à l'écran d'attente
                Intent intent = new Intent(ListQuizActivity.this, WaitingActivity.class);
                intent.putExtra("quiz_id", selectedQuizId);
                intent.putExtra("quiz_title", quizTitle);
                startActivity(intent);
            });

            quizContainer.addView(quizButton);
        }
    }

    private String formatQuizButtonText(JsonObject quiz, int position) {
        StringBuilder sb = new StringBuilder();

        // Titre du quiz
        if (quiz.has("title")) {
            sb.append(quiz.get("title").getAsString());
        } else if (quiz.has("subject")) {
            sb.append("Quiz: ").append(quiz.get("subject").getAsString());
        } else {
            sb.append("Quiz ").append(position);
        }

        // Statut (si disponible)
        if (quiz.has("status")) {
            String status = quiz.get("status").getAsString();
            sb.append("\nStatut: ").append(status);
        }

        // Nombre de questions (si disponible)
        if (quiz.has("questionCount")) {
            int count = quiz.get("questionCount").getAsInt();
            sb.append(" | ").append(count).append(" questions");
        }

        return sb.toString();
    }

    private String extractQuizTitle(JsonObject quiz) {
        if (quiz.has("title")) {
            return quiz.get("title").getAsString();
        } else if (quiz.has("subject")) {
            return quiz.get("subject").getAsString();
        } else {
            return "Quiz";
        }
    }

    private void showErrorMessage(String message) {
        TextView errorText = new TextView(this);
        errorText.setText(message);
        errorText.setTextColor(getResources().getColor(android.R.color.white));
        errorText.setTextSize(16);
        errorText.setPadding(0, 40, 0, 20);
        errorText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        quizContainer.addView(errorText);

        // Bouton pour réessayer
        Button retryButton = new Button(this);
        retryButton.setText("Réessayer");
        retryButton.setBackgroundResource(R.drawable.button_transparent_white);
        retryButton.setTextColor(getResources().getColor(android.R.color.white));
        retryButton.setOnClickListener(v -> loadQuizzes());

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 0);
        retryButton.setLayoutParams(params);

        quizContainer.addView(retryButton);
    }

    private void showNoQuizzesMessage() {
        TextView noQuizText = new TextView(this);
        noQuizText.setText("Aucun quiz disponible pour le moment");
        noQuizText.setTextColor(getResources().getColor(android.R.color.white));
        noQuizText.setTextSize(18);
        noQuizText.setPadding(0, 40, 0, 40);
        noQuizText.setGravity(View.TEXT_ALIGNMENT_CENTER);
        quizContainer.addView(noQuizText);
    }
}