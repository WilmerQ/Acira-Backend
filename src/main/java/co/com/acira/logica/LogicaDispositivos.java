/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.clases.resultadoFirebaseFCM;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.json.simple.JSONObject;

/**
 *
 * @author wilme
 */
@Stateless
@LocalBean
public class LogicaDispositivos implements Serializable {

//    @PersistenceContext(unitName = "Acirav1PU")
//    private EntityManager em;
    public void SendFcmManual(String token) {
        try {
            System.out.println("--------------------------------------------------------");
            String FCMurl = "https://fcm.googleapis.com/fcm/send";

            URL url = new URL(FCMurl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "key=" + ConfiguracionGeneral.AuthKey);
            connection.setRequestProperty("Content-Type", "application/json");

            JSONObject json = new JSONObject();
            json.put("to", token);
            json.put("time_to_live", 0);
            json.put("priority", "high");

            JSONObject data = new JSONObject();
            data.put("content-available", 1);
            data.put("comandosSecundarios", "{\"estado\":\"cierraSesion\"}");

            json.put("data", data);

            System.out.println("json " + json);

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(json.toString());
            wr.flush();

            BufferedReader in;
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String str;
            StringBuilder finalStr = new StringBuilder();
            while ((str = in.readLine()) != null) {
                finalStr.append(str);
                System.out.println("leyendo datos");
            }
            in.close();
            System.out.println("result:  " + finalStr.toString());
            resultadoFirebaseFCM result = new Gson().fromJson(finalStr.toString(), resultadoFirebaseFCM.class);
            if (result != null) {
                System.out.println("result != null");
                if (result.getSuccess() != 1) {
                    System.out.println("fallo al enviar FCM");
                } else {
                    System.out.println("envio FCM");
                }
            }
        } catch (JsonSyntaxException | IOException ex) {
            System.out.println("Error: LogicaDispositivos-SendFcmManual: " + ex.getLocalizedMessage());

        }
    }
}
