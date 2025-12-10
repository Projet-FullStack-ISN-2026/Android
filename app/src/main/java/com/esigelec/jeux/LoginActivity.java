package com.esigelec.jeux;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;

    private final String MOCK_SERVER_URL = "https://95fc8e26-8322-4331-8771-3f6edee908f5.mock.pstmn.io";

    // Comptes en dur pour tests locaux
    private final String[][] HARDCODED_ACCOUNTS = {
            {"admin@test.com", "admin123"},
            {"user@test.com", "user123"},
            {"test@example.com", "password123"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextText2);
        editTextPassword = findViewById(R.id.editTextText3);
        buttonLogin = findViewById(R.id.button);

        buttonLogin.setOnClickListener(view -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            // Validation locale d'abord
            if (validateLocalCredentials(email, password)) {
                // Appel au mock server
                loginWithMockServer(email, password);
            } else {
                Toast.makeText(LoginActivity.this,
                        "Identifiants locaux incorrects", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateLocalCredentials(String email, String password) {
        for (String[] account : HARDCODED_ACCOUNTS) {
            if (account[0].equals(email) && account[1].equals(password)) {
                return true;
            }
        }
        return false;
    }

    private void loginWithMockServer(String email, String password) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            // Créer le JSON body
            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
                json.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(json.toString(), mediaType);

            Request request = new Request.Builder()
                    .url(MOCK_SERVER_URL + "/login")
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful()) {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            String status = jsonResponse.getString("status");

                            if ("success".equals(status)) {
                                String token = jsonResponse.getString("token");
                                Toast.makeText(LoginActivity.this,
                                        "Connexion réussie!", Toast.LENGTH_SHORT).show();

                                // Récupérer les infos utilisateur si disponibles
                                if (jsonResponse.has("user")) {
                                    JSONObject user = jsonResponse.getJSONObject("user");
                                    String nom = user.getString("nom");
                                    String prenom = user.getString("prenom");
                                    // Traiter les données utilisateur...
                                }

                            } else {
                                String message = jsonResponse.getString("message");
                                Toast.makeText(LoginActivity.this,
                                        message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Erreur serveur: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(LoginActivity.this,
                                "Erreur de parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Erreur réseau: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
