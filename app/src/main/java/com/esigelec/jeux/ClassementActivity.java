package com.esigelec.jeux;

import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class ClassementActivity extends AppCompatActivity
        implements HelperWS.WebSocketListener {

    private HelperWS helperWS;
    private LinearLayout quizContainer;
    private TextView titleTextView;

    // Stocker les données du classement
    private List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
    private int correctCount = 0;
    private int totalCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classement);

        // Initialiser les vues
        titleTextView = findViewById(R.id.titleTextView);
        quizContainer = findViewById(R.id.quizContainer);

        // Initialiser HelperWS
        helperWS = HelperWS.getInstance();
        helperWS.setWebSocketListener(this);

        // Récupérer les données passées depuis l'intent (optionnel)
        if (getIntent() != null) {
            correctCount = getIntent().getIntExtra("CORRECT_COUNT", 0);
            totalCount = getIntent().getIntExtra("TOTAL_COUNT", 0);

            // Afficher les stats personnelles dans le titre
            updateTitleWithStats();

            // Vérifier si on a déjà des données de classement
            String leaderboardJson = getIntent().getStringExtra("LEADERBOARD_JSON");
            if (leaderboardJson != null) {
                try {
                    JsonArray entries = com.google.gson.JsonParser.parseString(leaderboardJson).getAsJsonArray();
                    displayLeaderboard(entries);
                } catch (Exception e) {
                    Log.e("ClassementActivity", "Erreur parsing JSON: " + e.getMessage());
                }
            }
        }

        Toast.makeText(this, "En attente du classement...", Toast.LENGTH_SHORT).show();
    }

    private void updateTitleWithStats() {
        String title = "Classement";
        if (totalCount > 0) {
            title += " - Votre score: " + correctCount + "/" + totalCount;
        }
        titleTextView.setText(title);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (helperWS != null) {
            helperWS.setWebSocketListener(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (helperWS != null) {
            helperWS.removeWebSocketListener();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (helperWS != null) {
            helperWS.removeWebSocketListener();
        }
    }

    // Implémentation des méthodes du listener
    @Override
    public void onConnected() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Connecté au serveur", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDisconnected() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Déconnecté du serveur", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onShowStats(int correctCount, int totalCount, JsonArray stats) {
        runOnUiThread(() -> {
            // Stocker les stats personnelles
            this.correctCount = correctCount;
            this.totalCount = totalCount;

            // Mettre à jour le titre
            updateTitleWithStats();

            // Afficher un message
            Toast.makeText(this,
                    "Statistiques reçues: " + correctCount + "/" + totalCount +
                            "\nRécupération du classement...",
                    Toast.LENGTH_LONG).show();

            // Le classement sera automatiquement récupéré par HelperWS
        });
    }

    @Override
    public void onShowClassement(int quizId, JsonArray entries) {
        runOnUiThread(() -> {
            // Afficher le classement
            displayLeaderboard(entries);
        });
    }

    private void displayLeaderboard(JsonArray entries) {
        quizContainer.removeAllViews();
        leaderboardEntries.clear();

        if (entries == null || entries.size() == 0) {
            // Afficher un message si pas de données
            TextView noDataText = new TextView(this);
            noDataText.setText("Aucun classement disponible");
            noDataText.setTextSize(18);
            noDataText.setTextColor(getColor(android.R.color.white));
            noDataText.setPadding(20, 40, 20, 40);
            noDataText.setGravity(android.view.Gravity.CENTER);
            quizContainer.addView(noDataText);
            return;
        }

        try {
            // Parser les données selon la structure LeaderboardEntry
            for (int i = 0; i < entries.size(); i++) {
                JsonObject entry = entries.get(i).getAsJsonObject();

                String playerName = "Joueur";
                int score = 0;
                int rank = i + 1;

                // Récupérer le rang
                if (entry.has("rank")) {
                    rank = entry.get("rank").getAsInt();
                }

                // Récupérer le score
                if (entry.has("score")) {
                    score = entry.get("score").getAsInt();
                }

                // Récupérer le nom du joueur
                if (entry.has("user")) {
                    JsonObject user = entry.getAsJsonObject("user");
                    if (user.has("firstName") && user.has("lastName")) {
                        String firstName = user.get("firstName").getAsString();
                        String lastName = user.get("lastName").getAsString();
                        playerName = firstName + " " + lastName;
                    } else if (user.has("email")) {
                        // Utiliser l'email si nom non disponible
                        String email = user.get("email").getAsString();
                        playerName = email.split("@")[0];
                    }
                }

                leaderboardEntries.add(new LeaderboardEntry(playerName, score, rank));
            }

            // Créer les vues pour chaque entrée
            for (LeaderboardEntry entry : leaderboardEntries) {
                addLeaderboardEntryView(entry);
            }

            Toast.makeText(this,
                    "Classement mis à jour: " + leaderboardEntries.size() + " joueurs",
                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.e("ClassementActivity", "Erreur affichage classement: " + e.getMessage());
            e.printStackTrace();

            TextView errorText = new TextView(this);
            errorText.setText("Erreur d'affichage du classement");
            errorText.setTextSize(16);
            errorText.setTextColor(getColor(android.R.color.white));
            errorText.setGravity(android.view.Gravity.CENTER);
            quizContainer.addView(errorText);
        }
    }

    private void addLeaderboardEntryView(LeaderboardEntry entry) {
        // Créer un layout pour chaque entrée
        LinearLayout entryLayout = new LinearLayout(this);
        entryLayout.setOrientation(LinearLayout.HORIZONTAL);
        entryLayout.setPadding(20, 15, 20, 15);
        entryLayout.setBackgroundResource(R.drawable.leaderboard_item_bg);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 10);
        entryLayout.setLayoutParams(layoutParams);

        // TextView pour le rang
        TextView rankView = new TextView(this);
        rankView.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        rankView.setText("#" + entry.getRank());
        rankView.setTextSize(24);
        rankView.setTextColor(getColor(android.R.color.white));
        rankView.setTypeface(null, android.graphics.Typeface.BOLD);

        // Appliquer des couleurs spéciales pour le podium
        switch (entry.getRank()) {
            case 1:
                rankView.setText("🥇");
                entryLayout.setBackgroundResource(R.drawable.gold_item_bg);
                break;
            case 2:
                rankView.setText("🥈");
                entryLayout.setBackgroundResource(R.drawable.silver_item_bg);
                break;
            case 3:
                rankView.setText("🥉");
                entryLayout.setBackgroundResource(R.drawable.bronze_item_bg);
                break;
        }

        // TextView pour le nom
        TextView nameView = new TextView(this);
        nameView.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 3));
        nameView.setText(entry.getPlayerName());
        nameView.setTextSize(20);
        nameView.setTextColor(getColor(android.R.color.white));
        nameView.setPadding(10, 0, 10, 0);

        // TextView pour le score
        TextView scoreView = new TextView(this);
        scoreView.setLayoutParams(new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        scoreView.setText(String.valueOf(entry.getScore()) + " pts");
        scoreView.setTextSize(20);
        scoreView.setTextColor(getColor(android.R.color.white));
        scoreView.setTypeface(null, android.graphics.Typeface.BOLD);
        scoreView.setGravity(android.view.Gravity.END);

        // Ajouter les vues au layout
        entryLayout.addView(rankView);
        entryLayout.addView(nameView);
        entryLayout.addView(scoreView);

        // Ajouter au container principal
        quizContainer.addView(entryLayout);
    }

    // Les autres méthodes du listener (non utilisées)
    @Override
    public void onNextQuestion(int questionId, JsonObject question, JsonArray options) {
        // Non utilisé
    }

    @Override
    public void onShowAnswer(int questionId, JsonObject correctAnswer) {
        // Non utilisé
    }

    // Classe interne pour les entrées du classement
    private static class LeaderboardEntry {
        private String playerName;
        private int score;
        private int rank;

        public LeaderboardEntry(String playerName, int score, int rank) {
            this.playerName = playerName;
            this.score = score;
            this.rank = rank;
        }

        public String getPlayerName() { return playerName; }
        public int getScore() { return score; }
        public int getRank() { return rank; }
    }
}