/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.Usuario;

/**
 *
 * @author wilme
 */
public class objetoXmpp {

    private Usuario usuario;
    private String idmensaje;
    private String mensaje;
    private EventoPanico eventoPanico;
    private EventoSospecha eventoSospecha;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getIdmensaje() {
        return idmensaje;
    }

    public void setIdmensaje(String idmensaje) {
        this.idmensaje = idmensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public EventoPanico getEventoPanico() {
        return eventoPanico;
    }

    public void setEventoPanico(EventoPanico eventoPanico) {
        this.eventoPanico = eventoPanico;
    }

    public void setEventoSospecha(EventoSospecha eventoSospecha) {
        this.eventoSospecha = eventoSospecha;
    }

    public EventoSospecha getEventoSospecha() {
        return eventoSospecha;
    }

}
