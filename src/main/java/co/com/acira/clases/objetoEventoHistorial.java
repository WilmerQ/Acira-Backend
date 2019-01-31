/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import java.util.Date;

/**
 *
 * @author wilme
 */
public class objetoEventoHistorial {

    private Long id;
    private String usuarioInformante;
    private String mensaje;
    private Double latitud;
    private Double longitud;
    private Double exactitud;
    private Boolean anonimo;
    private Date fechaRecolecion;
    private String tipo;
    private String uuidImagen;
    private String uuidAudio;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsuarioInformante() {
        return usuarioInformante;
    }

    public void setUsuarioInformante(String usuarioInformante) {
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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
