/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.base;

import java.io.Serializable;

/**
 * Esta clase define para hacer consultas sql
 *
 * @author Alvaro Padilla
 */
public class FieldtoQuery implements Serializable {

    //Campos de la clase
    private String nombreCampo;
    private Object valorCampo;
    private Boolean usarLike;

    public FieldtoQuery() {
    }

    /**
     * Constructor para obtener objeto con nombre y valor del campo
     *
     * @param nombreCampo
     * @param valorCampo
     */
    public FieldtoQuery(String nombreCampo, Object valorCampo) {
        this.nombreCampo = nombreCampo;
        this.valorCampo = valorCampo;
    }//Cierre del constructor

    /**
     * Constructor para obtener objeto completo
     *
     * @param nombreCampo
     * @param valorCampo
     * @param usarLike
     */
    public FieldtoQuery(String nombreCampo, Object valorCampo, Boolean usarLike) {
        this.nombreCampo = nombreCampo;
        this.valorCampo = valorCampo;
        this.usarLike = usarLike;
    }//Cierre del constructor

    /**
     * Método que devuelve el nombre del campo que se quiere consultar
     *
     * @return El texto con el nombre del campos que se quiere consultar
     */
    public String getNombreCampo() {
        return nombreCampo;
    }//Cierre del método

    /**
     * Método que encasula el nombre del campo que se quiere consultar
     *
     * @param nombreCampo
     */
    public void setNombreCampo(String nombreCampo) {
        this.nombreCampo = nombreCampo;
    }//Cierre del método

    /**
     * Método que devuelve el campo para hacer flitro en la consulta
     *
     * @return El objeto con el campos para hacer el flitro en la consulta
     */
    public Object getValorCampo() {
        return valorCampo;
    }//Cierre del método

    /**
     * Método que encasula el valor de campo
     *
     * @param valorCampo
     */
    public void setValorCampo(Object valorCampo) {
        this.valorCampo = valorCampo;
    }//Cierre del método

    /**
     * Método que devuelve el booleando hacer consulta con like
     *
     * @return El boleando para hacer consulta con like
     */
    public Boolean getUsarLike() {
        if (usarLike == null) {
            usarLike = false;
        }
        return usarLike;
    }//Cierre del método

    /**
     * Método que encasula el boleando para usar consulta like
     *
     * @param usarLike
     */
    public void setUsarLike(Boolean usarLike) {
        this.usarLike = usarLike;
    }//Cierre del método
} //Cierre de la clase
