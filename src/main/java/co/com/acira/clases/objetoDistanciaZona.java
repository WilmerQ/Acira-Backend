/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.clases;

import co.com.acira.modelo.Zona;

/**
 *
 * @author wilme
 */
public class objetoDistanciaZona {

    public objetoDistanciaZona(Zona zona, Double distancia) {
        this.zona = zona;
        this.distancia = distancia;
    }

    private Zona zona;
    private Double distancia;

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public Double getDistancia() {
        return distancia;
    }

    public void setDistancia(Double distancia) {
        this.distancia = distancia;
    }

}
