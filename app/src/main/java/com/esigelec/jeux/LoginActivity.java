package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;

import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;

    private static final String TAG = "LoginActivity";
    private static final String MOCK_SERVER_URL = "http://95fc8e26-8322-4331-8771-3f6edee908f5.mock.pstmn.io/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextText2);
        editTextPassword = findViewById(R.id.editTextText3);
        buttonLogin = findViewById(R.id.button);

        buttonLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            // Appel au mock server
            loginWithMockServer(email, password);
        });
    }

    private void loginWithMockServer(String email, String password) {
        OkHttpClient client = new OkHttpClient();

        // Créer le JSON body
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("email", email);
            jsonBody.put("password", password);
        } catch (JSONException e) {
            Log.e(TAG, "Erreur création JSON", e);
            return;
        }

        RequestBody body = RequestBody.create(
                jsonBody.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(MOCK_SERVER_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .build();

        Log.d(TAG, "Envoi à Mock Server...");

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this,
                            "Erreur de connexion", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Network error: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                // Récupérer les données
                                String token = jsonResponse.getString("token");
                                JSONObject user = jsonResponse.getJSONObject("user");

                                String userId = String.valueOf(user.getInt("id"));
                                String nom = user.getString("nom");
                                String prenom = user.getString("prenom");

                                // Message de succès
                                Toast.makeText(LoginActivity.this,
                                        "Connexion réussie!", Toast.LENGTH_SHORT).show();

                                // REDIRECTION VERS ACCUEIL
                                redirectToAccueil(userId, nom, prenom, token);

                            } else {
                                // Erreur métier
                                String message = jsonResponse.getString("message");
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Erreur HTTP
                            Toast.makeText(LoginActivity.this,
                                    "Erreur serveur: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this,
                                "Erreur de format", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "JSON error: " + e.getMessage());
                    }
                });
            }
        });
    }

    /**
     * Redirection vers AccueilActivity avec les données utilisateur
     */
    private void redirectToAccueil(String userId, String nom, String prenom, String token) {
        Intent intent = new Intent(LoginActivity.this, AccueilActivity.class);

        // Passer les données à AccueilActivity
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USER_NOM", nom);
        intent.putExtra("USER_PRENOM", prenom);
        intent.putExtra("USER_TOKEN", token);
        intent.putExtra("USER_EMAIL", editTextEmail.getText().toString().trim());

        // Démarrer l'activité
        startActivity(intent);

        // Optionnel: fermer LoginActivity pour ne pas pouvoir y revenir avec "back"
        finish();
    }
}