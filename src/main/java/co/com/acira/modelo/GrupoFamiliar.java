/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 *
 * @author wilme
 */
@Entity
public class GrupoFamiliar extends CamposComunesdeEntidad implements Serializable {

    @OneToOne
    private CodigoSeguridad codigoAsignado;
    private Long numeroUsuarios;
    @ManyToOne
    private Usuario usuarioPrincipal;

    public CodigoSeguridad getCodigoAsignado() {
        return codigoAsignado;
    }

    public void setCodigoAsignado(CodigoSeguridad codigoAsignado) {
        this.codigoAsignado = codigoAsignado;
    }

    public Long getNumeroUsuarios() {
        return numeroUsuarios;
    }

    public void setNumeroUsuarios(Long numeroUsuarios) {
        this.numeroUsuarios = numeroUsuarios;
    }

    public Usuario getUsuarioPrincipal() {
        return usuarioPrincipal;
    }

    public void setUsuarioPrincipal(Usuario usuarioPrincipal) {
        this.usuarioPrincipal = usuarioPrincipal;
    }

}
