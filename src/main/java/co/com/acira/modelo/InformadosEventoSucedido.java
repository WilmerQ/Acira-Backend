/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.modelo;

import co.com.acira.base.SessionOperations;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.Version;

/**
 *
 * @author wilme
 */
@Entity
public class InformadosEventoSucedido implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Boolean estado;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fechaCreacion;
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date fechaAnulacion;
    private String usuarioCreacion;
    private String usuarioAnulacion;
    @Version
    private Long version;
    @ManyToOne
    private EventoPanico eventoPanico;
    @ManyToOne
    private EventoSospecha eventoSospecha;
    @ManyToOne
    private PersonaSeguridadXZona personaSeguridadXZona;
    @ManyToOne
    private Usuario usuarioInformado;
    private String medioUtlizado;
    private String codigoEntrega;
    @Column(columnDefinition = "TEXT")
    private String mensajeNativo;
    private String extra;

    @PrePersist
    public void prepersistir() {
        //System.out.println("ejecutando prepersist para " + this.getClass().getName());
        fechaCreacion = new Date();
        if (estado == null) {
            estado = Boolean.TRUE;
        }
        try {
            Usuario u = (Usuario) SessionOperations.getSessionValue("USUARIO");
            usuarioCreacion = u.getNombreUsuario();
        } catch (Exception e) {
            usuarioCreacion = "Sistema";
        }
    }

    @PreUpdate
    public void premerge() {
//        System.out.println("ejecutando premerge para "+this.getClass().getName());
        if (Objects.equals(estado, Boolean.FALSE)) {
            Usuario u = (Usuario) SessionOperations.getSessionValue("USUARIO");
            usuarioAnulacion = u.getNombreUsuario();
            fechaAnulacion = new Date();
        }
    }

    public EventoPanico getEventoPanico() {
        return eventoPanico;
    }

    public void setEventoPanico(EventoPanico eventoPanico) {
        this.eventoPanico = eventoPanico;
    }

    public EventoSospecha getEventoSospecha() {
        return eventoSospecha;
    }

    public void setEventoSospecha(EventoSospecha eventoSospecha) {
        this.eventoSospecha = eventoSospecha;
    }

    public Usuario getUsuarioInformado() {
        return usuarioInformado;
    }

    public void setUsuarioInformado(Usuario usuarioInformado) {
        this.usuarioInformado = usuarioInformado;
    }

    public String getMedioUtlizado() {
        return medioUtlizado;
    }

    public void setMedioUtlizado(String medioUtlizado) {
        this.medioUtlizado = medioUtlizado;
    }

    public String getCodigoEntrega() {
        return codigoEntrega;
    }

    public void setCodigoEntrega(String codigoEntrega) {
        this.codigoEntrega = codigoEntrega;
    }

    public String getMensajeNativo() {
        return mensajeNativo;
    }

    public void setMensajeNativo(String mensajeNativo) {
        this.mensajeNativo = mensajeNativo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaAnulacion() {
        return fechaAnulacion;
    }

    public void setFechaAnulacion(Date fechaAnulacion) {
        this.fechaAnulacion = fechaAnulacion;
    }

    public String getUsuarioCreacion() {
        return usuarioCreacion;
    }

    public void setUsuarioCreacion(String usuarioCreacion) {
        this.usuarioCreacion = usuarioCreacion;
    }

    public String getUsuarioAnulacion() {
        return usuarioAnulacion;
    }

    public void setUsuarioAnulacion(String usuarioAnulacion) {
        this.usuarioAnulacion = usuarioAnulacion;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public PersonaSeguridadXZona getPersonaSeguridadXZona() {
        return personaSeguridadXZona;
    }

    public void setPersonaSeguridadXZona(PersonaSeguridadXZona personaSeguridadXZona) {
        this.personaSeguridadXZona = personaSeguridadXZona;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

}
