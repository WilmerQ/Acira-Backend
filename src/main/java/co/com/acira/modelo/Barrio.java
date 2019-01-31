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
public class Barrio extends CamposComunesdeEntidad implements Serializable {

    @ManyToOne
    private Ciudad ciudad;
    private Long codigo;
    private Long numeroTelefono;
    private String nombre;

    public Ciudad getCiudad() {
        return ciudad;
    }

    public void setCiudad(Ciudad ciudad) {
        this.ciudad = ciudad;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public Long getNumeroTelefono() {
        return numeroTelefono;
    }

    public void setNumeroTelefono(Long numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

}
