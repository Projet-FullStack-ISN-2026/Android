package com.esigelec.jeux;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class GenereActivity extends AppCompatActivity {

    private LinearLayout questionsContainer;
    private Button btnAjouterQuestion;
    private TextView tvTheme;
    private ScrollView scrollView;

    private List<Question> questionsList = new ArrayList<>();
    private String theme;
    private int questionCounter = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_genere);

        // Initialiser les vues
        questionsContainer = findViewById(R.id.questionsContainer);
        btnAjouterQuestion = findViewById(R.id.btnAjouterQuestion);
        tvTheme = findViewById(R.id.tvTheme);
        scrollView = findViewById(R.id.scrollView);

        // Récupérer les données de l'intent
        Intent intent = getIntent();
        theme = intent.getStringExtra("theme");
        String quizData = intent.getStringExtra("quiz_data");

        // Afficher le thème
        tvTheme.setText("Thème: " + theme);

        // Parser les données du quiz
        if (quizData != null && !quizData.isEmpty()) {
            parseAndDisplayQuestions(quizData);
        }

        // Bouton pour ajouter une nouvelle question
        btnAjouterQuestion.setOnClickListener(v -> ajouterNouvelleQuestion());
    }

    private void parseAndDisplayQuestions(String jsonData) {
        try {
            Gson gson = new Gson();
            JsonObject quizJson = gson.fromJson(jsonData, JsonObject.class);

            // Structure attendue: {"questions": [...]}
            if (quizJson.has("questions")) {
                JsonArray questionsArray = quizJson.getAsJsonArray("questions");

                for (int i = 0; i < questionsArray.size(); i++) {
                    JsonObject questionObj = questionsArray.get(i).getAsJsonObject();

                    String questionText = questionObj.has("question") ?
                            questionObj.get("question").getAsString() : "Question " + (i + 1);

                    JsonArray propositionsArray = questionObj.getAsJsonArray("propositions");
                    List<String> propositions = new ArrayList<>();

                    for (int j = 0; j < propositionsArray.size(); j++) {
                        propositions.add(propositionsArray.get(j).getAsString());
                    }

                    // Créer et ajouter la question
                    Question question = new Question(questionText, propositions);
                    questionsList.add(question);

                    // Afficher la question dans l'UI
                    afficherQuestionUI(questionText, propositions, questionCounter);
                    questionCounter++;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur de parsing des questions", Toast.LENGTH_SHORT).show();
        }
    }

    private void afficherQuestionUI(String questionText, List<String> propositions, int questionNum) {
        // Conteneur pour une question
        LinearLayout questionLayout = new LinearLayout(this);
        questionLayout.setOrientation(LinearLayout.VERTICAL);
        questionLayout.setPadding(0, 20, 0, 20);

        // Titre de la question
        TextView tvQuestionNum = new TextView(this);
        tvQuestionNum.setText("Question " + questionNum);
        tvQuestionNum.setTextSize(18);
        tvQuestionNum.setTextColor(getResources().getColor(android.R.color.black));
        tvQuestionNum.setPadding(16, 8, 16, 8);

        // Zone de texte pour l'intitulé de la question
        EditText etQuestion = new EditText(this);
        etQuestion.setText(questionText);
        etQuestion.setHint("Intitulé de la question");
        etQuestion.setTextSize(16);
        etQuestion.setPadding(16, 8, 16, 16);
        etQuestion.setBackgroundResource(R.drawable.edittext_background);
        etQuestion.setTag("question_" + questionNum);

        // Ajouter les propositions
        for (int i = 0; i < propositions.size(); i++) {
            LinearLayout propLayout = new LinearLayout(this);
            propLayout.setOrientation(LinearLayout.HORIZONTAL);
            propLayout.setPadding(16, 4, 16, 4);

            TextView tvPropNum = new TextView(this);
            tvPropNum.setText("Proposition " + (i + 1) + ": ");
            tvPropNum.setTextSize(14);
            tvPropNum.setWidth(120);

            EditText etProposition = new EditText(this);
            etProposition.setText(propositions.get(i));
            etProposition.setHint("Texte de la proposition");
            etProposition.setTextSize(14);
            etProposition.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            etProposition.setBackgroundResource(R.drawable.edittext_background);
            etProposition.setTag("question_" + questionNum + "_prop_" + i);

            propLayout.addView(tvPropNum);
            propLayout.addView(etProposition);
            questionLayout.addView(propLayout);
        }

        // Boutons d'action pour la question
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        buttonsLayout.setPadding(16, 8, 16, 0);

        // Bouton Modifier
        Button btnModifier = new Button(this);
        btnModifier.setText("Modifier");
        btnModifier.setBackgroundResource(R.drawable.button_transparent_blue);
        btnModifier.setTextColor(getResources().getColor(android.R.color.white));
        btnModifier.setOnClickListener(v -> {
            // Logique pour modifier
            Toast.makeText(this, "Modifier question " + questionNum, Toast.LENGTH_SHORT).show();
        });

        // Bouton Supprimer
        Button btnSupprimer = new Button(this);
        btnSupprimer.setText("Supprimer");
        btnSupprimer.setTextColor(getResources().getColor(android.R.color.white));
        btnSupprimer.setOnClickListener(v -> {
            questionsContainer.removeView(questionLayout);
            questionsList.remove(questionNum - 1);
            renumeroterQuestions();
        });

        buttonsLayout.addView(btnModifier);
        buttonsLayout.addView(btnSupprimer);

        questionLayout.addView(tvQuestionNum);
        questionLayout.addView(etQuestion);
        questionLayout.addView(buttonsLayout);

        // Séparateur
        View separator = new View(this);
        separator.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 2));
        separator.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        questionLayout.addView(separator);

        questionsContainer.addView(questionLayout);
    }

    private void ajouterNouvelleQuestion() {
        // Créer une nouvelle question vide
        List<String> propositionsVides = new ArrayList<>();
        propositionsVides.add("");
        propositionsVides.add("");
        propositionsVides.add("");
        propositionsVides.add("");

        Question nouvelleQuestion = new Question("", propositionsVides);
        questionsList.add(nouvelleQuestion);

        afficherQuestionUI("", propositionsVides, questionCounter);
        questionCounter++;

        // Scroll vers le bas
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }

    private void renumeroterQuestions() {
        // Réinitialiser le compteur
        questionCounter = 1;

        // Parcourir toutes les questions et mettre à jour les numéros
        for (int i = 0; i < questionsContainer.getChildCount(); i++) {
            View child = questionsContainer.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout questionLayout = (LinearLayout) child;
                if (questionLayout.getChildAt(0) instanceof TextView) {
                    TextView tvQuestionNum = (TextView) questionLayout.getChildAt(0);
                    tvQuestionNum.setText("Question " + questionCounter);
                    questionCounter++;
                }
            }
        }
    }

    // Classe Question pour stocker les données
    class Question {
        String texte;
        List<String> propositions;

        Question(String texte, List<String> propositions) {
            this.texte = texte;
            this.propositions = propositions;
        }
    }
}