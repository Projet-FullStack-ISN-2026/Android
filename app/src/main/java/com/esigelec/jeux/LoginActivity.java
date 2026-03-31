package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;

    private static final String BASE_URL = "http://10.3.70.14:8080/auth/login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail = findViewById(R.id.editTextText2);
        editTextPassword = findViewById(R.id.editTextText3);
        buttonLogin = findViewById(R.id.button);

        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
                return;
            }

            loginWithApi(email, password);
        });
    }

    private void loginWithApi(String email, String password) {

        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();

            JSONObject json = new JSONObject();
            try {
                json.put("email", email);
                json.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            RequestBody body = RequestBody.create(
                    json.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String responseBody = response.body() != null ? response.body().string() : "";

                runOnUiThread(() -> handleLoginResponse(response, responseBody, email));

            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(LoginActivity.this,
                                "Erreur réseau : " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

        }).start();
    }

    private void handleLoginResponse(Response response, String responseBody, String email) {
        try {
            if (response.isSuccessful()) {

                JSONObject jsonResponse = new JSONObject(responseBody);
                String status = jsonResponse.optString("status");

                if ("success".equals(status)) {

                    String token = jsonResponse.optString("token");

                    Toast.makeText(this, "Connexion réussie !", Toast.LENGTH_SHORT).show();

                    if (jsonResponse.has("user")) {
                        JSONObject user = jsonResponse.getJSONObject("user");
                        String nom = user.optString("nom");
                        String prenom = user.optString("prenom");
                    }

                    // 👉 Redirection vers la liste des quiz
                    redirectToList(email);

                } else {
                    String message = jsonResponse.optString("message", "Identifiants incorrects");
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this,
                        "Erreur serveur : " + response.code(), Toast.LENGTH_SHORT).show();
            }

        } catch (JSONException e) {
            Toast.makeText(this, "Erreur JSON", Toast.LENGTH_SHORT).show();
        }
    }

    private void redirectToList(String email) {
        Intent intent = new Intent(LoginActivity.this, ListQuizActivity.class);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClick(View v) {
        if (v == buttonLogin) {
            Intent toListquiz = new Intent(LoginActivity.this, ListQuizActivity.class);
            startActivity(toListquiz);
        }
    }

}
