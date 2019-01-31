/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import java.util.Date;
import javax.persistence.Temporal;

/**
 *
 * @author wilme
 */
public class objetoFcm {

    private String nombreReporto;
    private Double latitud;
    private Double longitud;
    private Double exactitud;
    @Temporal(javax.persistence.TemporalType.TIME)
    private Date hora;
    private String tipo;
    private String uuidImagen;
    private String uuidAudio;

    public String getNombreReporto() {
        return nombreReporto;
    }

    public void setNombreReporto(String nombreReporto) {
        this.nombreReporto = nombreReporto;
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

    public Date getHora() {
        return hora;
    }

    public void setHora(Date hora) {
        this.hora = hora;
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
