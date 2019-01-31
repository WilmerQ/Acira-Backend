/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 *
 * @author wilme
 */
@Entity
public class Usuario extends CamposComunesdeEntidad implements Serializable {

    public Usuario() {
    }

    private String nombrecompleto;
    private String nombreUsuario;
    private Long numeroIdentificacion;
    private String contrasena;
    private String email;
    private Long telefono;
    @ManyToOne
    private GrupoFamiliar grupoFamiliar;
    private String tokenFCM;
    private Long idDispositivo;
    @Transient
    private Integer informeDeError;
    private Long idSesion;
    private Boolean ToWeb;
    private String uuidDocumentoIdentidad;

    public String getNombrecompleto() {
        return nombrecompleto;
    }

    public void setNombrecompleto(String nombrecompleto) {
        this.nombrecompleto = nombrecompleto;
    }

    public Long getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(Long numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getTelefono() {
        return telefono;
    }

    public void setTelefono(Long telefono) {
        this.telefono = telefono;
    }

    public GrupoFamiliar getGrupoFamiliar() {
        return grupoFamiliar;
    }

    public void setGrupoFamiliar(GrupoFamiliar grupoFamiliar) {
        this.grupoFamiliar = grupoFamiliar;
    }

    public String getTokenFCM() {
        return tokenFCM;
    }

    public void setTokenFCM(String tokenFCM) {
        this.tokenFCM = tokenFCM;
    }

    public Integer getInformeDeError() {
        return informeDeError;
    }

    public void setInformeDeError(Integer informeDeError) {
        this.informeDeError = informeDeError;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Long idSesion) {
        this.idSesion = idSesion;
    }

    public Long getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(Long idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public Boolean getToWeb() {
        return ToWeb;
    }

    public void setToWeb(Boolean ToWeb) {
        this.ToWeb = ToWeb;
    }

    public String getUuidDocumentoIdentidad() {
        return uuidDocumentoIdentidad;
    }

    public void setUuidDocumentoIdentidad(String uuidDocumentoIdentidad) {
        this.uuidDocumentoIdentidad = uuidDocumentoIdentidad;
    }

}
