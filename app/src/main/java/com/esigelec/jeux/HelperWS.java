package com.esigelec.jeux;

import com.client.ClientListener;
import com.client.ConfigWS;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Singleton HelperWS
 */
public class HelperWS implements ClientListener {

    private static final String TAG = "HelperWS";

    private static HelperWS instance;
    private ConfigWS configWS;
    private String baseUrl = "http://10.3.70.14:3128";
    private int currentQuizId = -1;

    public interface WebSocketListener {
        void onConnected();
        void onDisconnected();
        void onNextQuestion(int questionId, JsonObject question, JsonArray options);
        void onShowStats(int correctCount, int totalCount, JsonArray stats);
        void onShowAnswer(int questionId, JsonObject correctAnswer);
        void onShowClassement(int quizId, JsonArray leaderboardEntries);
    }

    private WebSocketListener webSocketListener;

    private HelperWS() {
        initializeWebSocket();
    }

    public static synchronized HelperWS getInstance() {
        if (instance == null) {
            instance = new HelperWS();
        }
        return instance;
    }

    private void initializeWebSocket() {
        try {
            String ip = "10.3.70.14";
            String port = "3128";
            configWS = new ConfigWS(ip, port, this);
            Log.d(TAG, "WebSocket initialisé sur " + ip + ":" + port);
        } catch (Exception e) {
            Log.e(TAG, "Erreur d'initialisation: " + e.getMessage());
        }
    }

    // Récupérer le classement via API REST selon la structure de l'API
    public void fetchLeaderboard(int quizId) {
        new Thread(() -> {
            try {
                String urlString = baseUrl + "/quiz/" + quizId + "/play/leaderboard";
                Log.d(TAG, "Récupération classement API: " + urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Code réponse classement: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    // Parser le JSON selon la structure Leaderboard de l'API
                    JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
                    Log.d(TAG, "Réponse classement brute: " + jsonResponse.toString());

                    // Extraire les entrées du classement selon la structure
                    JsonArray entries = new JsonArray();

                    if (jsonResponse.has("entries")) {
                        entries = jsonResponse.getAsJsonArray("entries");
                        Log.d(TAG, "Nombre d'entrées trouvées: " + entries.size());
                    } else if (jsonResponse.isJsonArray()) {
                        // Si la réponse est directement un tableau
                        entries = jsonResponse.getAsJsonArray();
                    }

                    // Notifier l'activité avec les données du classement
                    if (webSocketListener != null) {
                        webSocketListener.onShowClassement(quizId, entries);
                    }

                } else {
                    Log.e(TAG, "Erreur HTTP classement: " + responseCode);
                    // Gérer l'erreur
                    if (webSocketListener != null) {
                        webSocketListener.onShowClassement(quizId, new JsonArray());
                    }
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Erreur fetchLeaderboard: " + e.getMessage());
                e.printStackTrace();
                // Notifier avec un tableau vide en cas d'erreur
                if (webSocketListener != null) {
                    webSocketListener.onShowClassement(quizId, new JsonArray());
                }
            }
        }).start();
    }

    // Implémentation des méthodes ClientListener
    @Override
    public void onConnected() {
        Log.d(TAG, "WebSocket connecté");
        if (webSocketListener != null) {
            webSocketListener.onConnected();
        }
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "WebSocket déconnecté");
        if (webSocketListener != null) {
            webSocketListener.onDisconnected();
        }
    }

    @Override
    public void onNextQuestion(int questionId, JsonObject question, JsonArray options) {
        Log.d(TAG, "Question reçue: " + questionId);
        if (webSocketListener != null) {
            webSocketListener.onNextQuestion(questionId, question, options);
        }
    }

    @Override
    public void onShowStats(int correctCount, int totalCount, JsonArray stats) {
        Log.d(TAG, "Stats reçues - Correct: " + correctCount + "/" + totalCount);
        Log.d(TAG, "Détails stats JSON: " + (stats != null ? stats.toString() : "null"));

        // 1. D'abord notifier les stats normalement
        if (webSocketListener != null) {
            webSocketListener.onShowStats(correctCount, totalCount, stats);
        }

        // 2. Puis récupérer le classement (si un quiz est actif)
        if (currentQuizId != -1) {
            Log.d(TAG, "Déclenchement récupération classement pour quiz: " + currentQuizId);

            // Petit délai pour laisser les stats s'afficher d'abord
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                fetchLeaderboard(currentQuizId);
            }, 1000);

        } else {
            Log.w(TAG, "Quiz ID non défini, impossible de récupérer le classement");
        }
    }

    @Override
    public void onShowAnswer(int questionId, JsonObject correctAnswer) {
        Log.d(TAG, "Réponse reçue pour question: " + questionId);
        if (webSocketListener != null) {
            webSocketListener.onShowAnswer(questionId, correctAnswer);
        }
    }

    @Override
    public void onShowClassement(int gameId, JsonArray rankings) {
        // Cette méthode n'est PAS appelée depuis le WebSocket directement
        // Elle est appelée après fetchLeaderboard() via le listener
        Log.d(TAG, "Données classement prêtes pour game: " + gameId);
        if (webSocketListener != null) {
            webSocketListener.onShowClassement(gameId, rankings);
        }
    }

    // Getters et Setters
    public void setCurrentQuizId(int quizId) {
        this.currentQuizId = quizId;
        Log.d(TAG, "Quiz ID défini: " + quizId);
    }

    public int getCurrentQuizId() {
        return currentQuizId;
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void removeWebSocketListener() {
        this.webSocketListener = null;
    }

    public void connect() {
        if (configWS != null) {
            try {
                configWS.connect();
                Log.d(TAG, "Connexion WebSocket en cours...");
            } catch (Exception e) {
                Log.e(TAG, "Erreur connexion: " + e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (configWS != null) {
            try {
                configWS.disconnect();
                Log.d(TAG, "Déconnexion WebSocket...");
            } catch (Exception e) {
                Log.e(TAG, "Erreur déconnexion: " + e.getMessage());
            }
        }
    }

    public boolean isConnected() {
        return configWS != null && configWS.isConnected();
    }
}