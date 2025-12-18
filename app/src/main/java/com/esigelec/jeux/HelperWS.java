package com.esigelec.jeux;

import com.client.ClientListener;
import com.client.ConfigWS;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Singleton HelperWS
 * Cette classe agit comme un pont unique entre le JAR (qui permet de communiquer avec le serveur WS) et l'application Android
 * Son rôle est de maintenir une seule connexion constante au serveur WS et de transmette les messages aux Activités
 */
public class HelperWS implements ClientListener {

    // L'instance unique de cette classe (Pattern Singleton)
    private static HelperWS instance;

    // L'objet du SDK qui gère la connexion réseau réelle
    private ConfigWS configWS;

    // IP et PORT de connexion au serveur WS
    //private String ip = "10.3.70.9";
    private String ip = "10.3.70.14"; // SERVEUR TEMPORAIRE POUR LA SALLE B1186

    private String port = "3128";