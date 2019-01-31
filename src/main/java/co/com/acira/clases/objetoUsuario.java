/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import co.com.acira.modelo.Usuario;

/**
 *
 * @author wilme
 */
public class objetoUsuario {

    public objetoUsuario() {

    }

    public objetoUsuario(Usuario u) {
        this.nombrecompleto = u.getNombrecompleto();
        this.nombreUsuario = u.getNombreUsuario();
        this.numeroIdentificacion = u.getNumeroIdentificacion();
        this.email = u.getEmail();
        this.telefono = u.getTelefono();
        this.idSesion = u.getIdSesion();
        this.codigofamiliar = u.getGrupoFamiliar().getCodigoAsignado().getCodigoGenerado();
    }

    private String nombrecompleto;
    private String nombreUsuario;
    private Long numeroIdentificacion;
    private String email;
    private Long telefono;
    private Long idSesion;
    private String codigofamiliar;

    public String getNombrecompleto() {
        return nombrecompleto;
    }

    public void setNombrecompleto(String nombrecompleto) {
        this.nombrecompleto = nombrecompleto;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public Long getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(Long numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
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

    public Long getIdSesion() {
        return idSesion;
    }

    public void setIdSesion(Long idSesion) {
        this.idSesion = idSesion;
    }

    public String getCodigofamiliar() {
        return codigofamiliar;
    }

    public void setCodigofamiliar(String codigofamiliar) {
        this.codigofamiliar = codigofamiliar;
    }

}
