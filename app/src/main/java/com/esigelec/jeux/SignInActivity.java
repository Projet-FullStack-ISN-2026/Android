package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    // Déclaration des vues
    private EditText editTextNom, editTextPrenom, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button btnValider;
    private RequestQueue requestQueue;

    // TAG pour les logs
    private static final String TAG = "SignInActivity";

    // URL complète de votre API (selon votre YAML)
    private static final String BASE_URL = "http://10.3.70.14:8080/";
    private static final String REGISTER_URL = BASE_URL + "auth/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        Log.d(TAG, "Activity créée");
        Log.d(TAG, "URL d'inscription: " + REGISTER_URL);

        // Initialiser Volley
        requestQueue = Volley.newRequestQueue(this);

        // Initialiser les vues
        initializeViews();
    }

    private void initializeViews() {
        try {
            // Récupérer les références des EditText
            editTextNom = findViewById(R.id.editTextNom);
            editTextPrenom = findViewById(R.id.editTextPrenom);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

            // Récupérer le bouton et ajouter le listener
            btnValider = findViewById(R.id.btnValider);
            btnValider.setOnClickListener(this);

            Log.d(TAG, "Vues initialisées avec succès");
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'initialisation des vues: " + e.getMessage());
            Toast.makeText(this, "Erreur d'initialisation", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnValider) {
            Log.d(TAG, "Bouton cliqué");
            // Valider les champs
            if (validateInputs()) {
                Log.d(TAG, "Validation OK, appel API...");
                // Envoyer les données à l'API
                registerUser();
            } else {
                Log.d(TAG, "Validation échouée");
            }
        }
    }

    private boolean validateInputs() {
        // Récupérer les valeurs
        String nom = editTextNom.getText().toString().trim();
        String prenom = editTextPrenom.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        boolean isValid = true;

        // Validation du nom
        if (TextUtils.isEmpty(nom)) {
            editTextNom.setError("Le nom est requis");
            editTextNom.requestFocus();
            isValid = false;
        }

        // Validation du prénom
        if (TextUtils.isEmpty(prenom)) {
            editTextPrenom.setError("Le prénom est requis");
            editTextPrenom.requestFocus();
            isValid = false;
        }

        // Validation de l'email
        if (TextUtils.isEmpty(email)) {
            editTextEmail.setError("L'email est requis");
            editTextEmail.requestFocus();
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Format d'email invalide");
            editTextEmail.requestFocus();
            isValid = false;
        }

        // Validation du mot de passe
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Le mot de passe est requis");
            editTextPassword.requestFocus();
            isValid = false;
        } else if (password.length() < 6) {
            editTextPassword.setError("Le mot de passe doit contenir au moins 6 caractères");
            editTextPassword.requestFocus();
            isValid = false;
        }

        // Validation de la confirmation du mot de passe
        if (TextUtils.isEmpty(confirmPassword)) {
            editTextConfirmPassword.setError("Veuillez confirmer votre mot de passe");
            editTextConfirmPassword.requestFocus();
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Les mots de passe ne correspondent pas");
            editTextConfirmPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    // Fonction pour hacher le mot de passe en SHA-256
    private String hashPasswordSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Convertir en hexadécimal (comme dans l'exemple du YAML)
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "Erreur de hachage SHA-256: " + e.getMessage());
            // En cas d'erreur, retourner le mot de passe en clair (non recommandé)
            return password;
        }
    }

    private void registerUser() {
        // Afficher un indicateur de chargement
        Toast.makeText(this, "Inscription en cours...", Toast.LENGTH_SHORT).show();

        // Récupérer les données
        String nom = editTextNom.getText().toString().trim();
        String prenom = editTextPrenom.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // HACHER le mot de passe en SHA-256 (OBLIGATOIRE selon le YAML)
        String hashedPassword = hashPasswordSHA256(password);

        Log.d(TAG, "Mot de passe original: " + password);
        Log.d(TAG, "Mot de passe haché (SHA-256): " + hashedPassword);

        // Créer l'objet JSON EXACTEMENT comme le YAML le spécifie
        Map<String, String> params = new HashMap<>();
        params.put("firstName", nom);        // "firstName" selon le YAML
        params.put("lastName", prenom);      // "lastName" selon le YAML
        params.put("email", email);          // "email" selon le YAML
        params.put("password", hashedPassword); // "password" HAché en SHA-256 selon le YAML

        // EXEMPLE du JSON attendu (selon le YAML):
        // {
        //   "firstName": "Guillaume",
        //   "lastName": "RENOUARD",
        //   "email": "Guillaume.renouard@groupe-esigelec.org",
        //   "password": "5a84b2325c345ab46b04a984c3c328957a056581335a4b5f8846141445167667"
        // }

        JSONObject jsonBody = new JSONObject(params);

        Log.d(TAG, "Envoi POST à: " + REGISTER_URL);
        Log.d(TAG, "Body JSON: " + jsonBody.toString());

        // Créer la requête POST
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                REGISTER_URL,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Réponse API (201): " + response.toString());
                        try {
                            // Selon le YAML, l'API retourne un objet "User" avec les champs:
                            // id, email, firstName, lastName, role

                            // Extraire les données de la réponse
                            String userEmail = response.getString("email");
                            String firstName = response.getString("firstName");
                            String lastName = response.getString("lastName");
                            int role = response.getInt("role");

                            // Vérifier si l'inscription est réussie (code 201)
                            Toast.makeText(SignInActivity.this,
                                    "Inscription réussie! Bienvenue " + firstName,
                                    Toast.LENGTH_SHORT).show();

                            // Rediriger vers l'accueil
                            redirectToHome(userEmail, firstName, lastName, role);

                        } catch (JSONException e) {
                            Log.e(TAG, "Erreur parsing JSON: " + e.getMessage());
                            // Si la réponse n'est pas au format User, mais peut-être un autre format
                            handleAlternativeResponse(response);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Erreur Volley: " + error.toString());

                        String errorMessage = "Erreur serveur";
                        String responseBody = "";

                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            Log.e(TAG, "Code HTTP: " + statusCode);

                            if (error.networkResponse.data != null) {
                                try {
                                    responseBody = new String(error.networkResponse.data, "utf-8");
                                    Log.e(TAG, "Body erreur: " + responseBody);

                                    // Essayer de parser le JSON d'erreur
                                    JSONObject errorJson = new JSONObject(responseBody);
                                    errorMessage = extractErrorMessage(errorJson);

                                } catch (Exception e) {
                                    errorMessage = "Erreur " + statusCode + ": " +
                                            (responseBody.length() > 100 ?
                                                    responseBody.substring(0, 100) + "..." :
                                                    responseBody);
                                }
                            }

                            // Messages spécifiques selon le code HTTP
                            switch (statusCode) {
                                case 400:
                                    errorMessage = "Erreur 400 - Données invalides. Vérifiez:\n" +
                                            "1. Tous les champs sont remplis\n" +
                                            "2. Format email valide\n" +
                                            "3. Mot de passe hashé en SHA-256";
                                    break;
                                case 409:
                                    errorMessage = "Cet email est déjà utilisé";
                                    break;
                                case 422:
                                    errorMessage = "Données de validation incorrectes";
                                    break;
                                case 500:
                                    errorMessage = "Erreur interne du serveur";
                                    break;
                            }
                        } else if (error.getMessage() != null) {
                            errorMessage = error.getMessage();
                            if (errorMessage.contains("Failed to connect")) {
                                errorMessage = "Impossible de se connecter au serveur. Vérifiez:\n" +
                                        "1. L'URL est correcte\n" +
                                        "2. Le serveur est démarré\n" +
                                        "3. Vous êtes sur le même réseau";
                            }
                        }

                        showError(errorMessage);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        // Configurer le timeout
        request.setRetryPolicy(new com.android.volley.DefaultRetryPolicy(
                15000, // 15 secondes timeout
                com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Ajouter la requête à la file
        requestQueue.add(request);
    }

    private String extractErrorMessage(JSONObject errorJson) {
        try {
            // Essayer différents formats d'erreur
            if (errorJson.has("message")) {
                return errorJson.getString("message");
            } else if (errorJson.has("error")) {
                return errorJson.getString("error");
            } else if (errorJson.has("details")) {
                return errorJson.getString("details");
            }
        } catch (JSONException e) {
            Log.e(TAG, "Erreur extraction message: " + e.getMessage());
        }
        return "Erreur inconnue";
    }

    private void handleAlternativeResponse(JSONObject response) {
        try {
            // Si l'API retourne autre chose qu'un objet User
            if (response.has("token")) {
                // C'est peut-être une AuthResponse (avec token et user)
                String token = response.getString("token");
                JSONObject user = response.getJSONObject("user");

                String email = user.getString("email");
                String firstName = user.getString("firstName");
                String lastName = user.getString("lastName");

                // Sauvegarder le token
                saveUserToken(token);

                Toast.makeText(this, "Connexion réussie!", Toast.LENGTH_SHORT).show();
                redirectToHome(email, firstName, lastName, 3); // 3 = JOUEUR par défaut
            } else {
                // Autre format inattendu
                Toast.makeText(this,
                        "Inscription réussie (format réponse inattendu)",
                        Toast.LENGTH_SHORT).show();
                redirectToHome("", "", "", 3);
            }
        } catch (JSONException e) {
            showError("Format de réponse non reconnu: " + response.toString());
        }
    }

    private void saveUserToken(String token) {
        // Sauvegarder le token dans SharedPreferences
        android.content.SharedPreferences prefs = getSharedPreferences("TF8_Prefs", MODE_PRIVATE);
        android.content.SharedPreferences.Editor editor = prefs.edit();
        editor.putString("auth_token", token);
        editor.apply();
        Log.d(TAG, "Token sauvegardé: " + token.substring(0, Math.min(20, token.length())) + "...");
    }

    private void redirectToHome(String email, String firstName, String lastName, int role) {
        Intent intent = new Intent(SignInActivity.this, AccueilActivity.class);

        // Passer les données utilisateur
        intent.putExtra("email", email);
        intent.putExtra("firstName", firstName);
        intent.putExtra("lastName", lastName);
        intent.putExtra("role", role);

        // Nettoyer la pile d'activités
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        Toast.makeText(SignInActivity.this,
                "Erreur: " + message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Erreur affichée: " + message);
    }
}