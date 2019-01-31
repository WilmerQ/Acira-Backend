/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Entity;

/**
 *
 * @author wilme
 */
@Entity
public class EmailToSoporte extends CamposComunesdeEntidad implements Serializable {

    private String nombre;
    private String email;
    private String contrasena;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

}
