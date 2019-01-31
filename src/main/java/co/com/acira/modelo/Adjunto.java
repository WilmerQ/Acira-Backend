/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;

/**
 *
 * @author wilme
 */
@Entity
public class Adjunto extends CamposComunesdeEntidad implements Serializable {

    private String tipo;
//    @Column(columnDefinition = "TEXT")
//    private String contenido;
//    private Long idDispositivo;
//    private Long idSesion;
    private String uuid;
//    private Boolean finalizada;
    private String ruta;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

}
