/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import java.io.Serializable;

/**
 *
 * @author wilme
 */
public class nuevoUsuario implements Serializable {

    private String nombrecompleto;
    private Long identificacion;
    private Long celular;
    private String email;
    private String nombreusuario;
    private String contrasena;
    private String codigofamiliar;
    private Long idSesion;
    private String uuidDocumento;

    public String getNombrecompleto() {
        return nombrecompleto;
    }

    public void setNombrecompleto(String nombrecompleto) {
        this.nombrecompleto = nombrecompleto;
    }

    public Long getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(Long identificacion) {
        this.identificacion = identificacion;
    }

    public Long getCelular() {
        return celular;
    }

    public void setCelular(Long celular) {
        this.celular = celular;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreusuario() {
        return nombreusuario;
    }

    public void setNombreusuario(String nombreusuario) {
        this.nombreusuario = nombreusuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCodigofamiliar() {
        return codigofamiliar;
    }

    public void setCodigofamiliar(String codigofamiliar) {
        this.codigofamiliar = codigofamiliar;
    }

    public Long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Long idSesion) {
        this.idSesion = idSesion;
    }

    public String getUuidDocumento() {
        return uuidDocumento;
    }

    public void setUuidDocumento(String uuidDocumento) {
        this.uuidDocumento = uuidDocumento;
    }

}
