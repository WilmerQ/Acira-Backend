/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author wilme
 */
@Entity
public class EventoPanico extends CamposComunesdeEntidad implements Serializable {

    @ManyToOne
    private Usuario usuarioInformante;
    private String mensaje;
    private Double latitud;
    private Double longitud;
    private Double exactitud;
    private Boolean anonimo;
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date fechaRecolecion;
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date hora;
    @ManyToOne
    private Zona zonaRespondiente;
    private String uuidImagen;
    private String uuidAudio;

    public Usuario getUsuarioInformante() {
        return usuarioInformante;
    }

    public void setUsuarioInformante(Usuario usuarioInformante) {
        this.usuarioInformante = usuarioInformante;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getExactitud() {
        return exactitud;
    }

    public void setExactitud(Double exactitud) {
        this.exactitud = exactitud;
    }

    public Boolean getAnonimo() {
        return anonimo;
    }

    public void setAnonimo(Boolean anonimo) {
        this.anonimo = anonimo;
    }

    public Date getFechaRecolecion() {
        return fechaRecolecion;
    }

    public void setFechaRecolecion(Date fechaRecolecion) {
        this.fechaRecolecion = fechaRecolecion;
    }

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
    }

    public Zona getZonaRespondiente() {
        return zonaRespondiente;
    }

    public void setZonaRespondiente(Zona zonaRespondiente) {
        this.zonaRespondiente = zonaRespondiente;
    }

    public String getUuidImagen() {
        return uuidImagen;
    }

    public void setUuidImagen(String uuidImagen) {
        this.uuidImagen = uuidImagen;
    }

    public String getUuidAudio() {
        return uuidAudio;
    }

    public void setUuidAudio(String uuidAudio) {
        this.uuidAudio = uuidAudio;
    }

}
