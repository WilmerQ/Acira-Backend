/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 *
 * @author wilme
 */
@Entity
public class CodigoSeguridad extends CamposComunesdeEntidad implements Serializable {

    private String codigoGenerado;
    private Long numeroIdentificacionUsuarioPrincipal;
    private String nombreUsuarioPrincipal;
    @ManyToOne
    private Barrio barrio;
    private String direccion;
    private Double latitud;
    private Double Longitud;

    public String getCodigoGenerado() {
        return codigoGenerado;
    }

    public void setCodigoGenerado(String codigoGenerado) {
        this.codigoGenerado = codigoGenerado;
    }

    public Long getNumeroIdentificacionUsuarioPrincipal() {
        return numeroIdentificacionUsuarioPrincipal;
    }

    public void setNumeroIdentificacionUsuarioPrincipal(Long numeroIdentificacionUsuarioPrincipal) {
        this.numeroIdentificacionUsuarioPrincipal = numeroIdentificacionUsuarioPrincipal;
    }

    public String getNombreUsuarioPrincipal() {
        return nombreUsuarioPrincipal;
    }

    public void setNombreUsuarioPrincipal(String nombreUsuarioPrincipal) {
        this.nombreUsuarioPrincipal = nombreUsuarioPrincipal;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return Longitud;
    }

    public void setLongitud(Double Longitud) {
        this.Longitud = Longitud;
    }

}
