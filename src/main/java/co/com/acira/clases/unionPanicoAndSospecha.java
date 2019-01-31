/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import java.util.Date;

/**
 *
 * @author wilme
 */
public class unionPanicoAndSospecha {

    private EventoPanico eventoPanico;
    private EventoSospecha eventoSospecha;
    private Date fechacreacion;
    private String tipoEvento;

    public EventoPanico getEventoPanico() {
        return eventoPanico;
    }

    public void setEventoPanico(EventoPanico eventoPanico) {
        this.eventoPanico = eventoPanico;
    }

    public EventoSospecha getEventoSospecha() {
        return eventoSospecha;
    }

    public void setEventoSospecha(EventoSospecha eventoSospecha) {
        this.eventoSospecha = eventoSospecha;
    }

    public Date getFechacreacion() {
        return fechacreacion;
    }

    public void setFechacreacion(Date fechacreacion) {
        this.fechacreacion = fechacreacion;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

}
