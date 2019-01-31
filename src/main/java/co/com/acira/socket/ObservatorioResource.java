/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.socket;

//import javax.websocket.OnMessage;
import javax.faces.application.FacesMessage;
import org.primefaces.push.annotation.OnMessage;
import org.primefaces.push.annotation.PushEndpoint;
import org.primefaces.push.impl.JSONEncoder;
//import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author wilme
 */
//@ServerEndpoint("/observatorio")
@PushEndpoint("/observatorio")
public class ObservatorioResource {

    @OnMessage(encoders = {JSONEncoder.class})
    public FacesMessage onMessage(FacesMessage message) {
        System.out.println("aqui en ObservatorioResource");
        return message;
    }

}
