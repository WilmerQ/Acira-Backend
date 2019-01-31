/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import co.com.acira.modelo.Ciudad;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author wilme
 */
@Stateless
@LocalBean
public class LogicaObservatorio implements Serializable {

    @PersistenceContext(unitName = "Acirav1PU")
    private EntityManager em;

    public List<EventoPanico> panicosXciudad(Ciudad c) {
        try {
            Query query = em.createNativeQuery("select evp.* \n"
                    + "from eventopanico evp\n"
                    + "join usuario u on evp.usuarioinformante_id = u.id\n"
                    + "join grupofamiliar gf on gf.id = u.grupofamiliar_id\n"
                    + "join codigoseguridad cod on gf.codigoasignado_id = cod.id\n"
                    + "join barrio b on cod.barrio_id = b.id\n"
                    + "join ciudad c on b.ciudad_id = c.id\n"
                    + "and c.id = " + c.getId() + "\n"
                    + "order by evp.fechacreacion desc\n"
                    + "limit 5;", EventoPanico.class);

            List<EventoPanico> panicos = query.getResultList();
            return panicos;
        } catch (Exception e) {
            System.out.println("panicosXciudad ERROR - " + e.getMessage());
            List<EventoPanico> panicos = new ArrayList<>();
            return panicos;
        }
    }

    public List<EventoSospecha> sospechasXciudad(Ciudad c) {
        try {
            Query query = em.createNativeQuery("select evp.* \n"
                    + "from eventosospecha evp\n"
                    + "join usuario u on evp.usuarioinformante_id = u.id\n"
                    + "join grupofamiliar gf on gf.id = u.grupofamiliar_id\n"
                    + "join codigoseguridad cod on gf.codigoasignado_id = cod.id\n"
                    + "join barrio b on cod.barrio_id = b.id\n"
                    + "join ciudad c on b.ciudad_id = c.id\n"
                    + "and c.id = " + c.getId() + "\n"
                    + "order by evp.fechacreacion desc\n"
                    + "limit 5;", EventoSospecha.class);

            List<EventoSospecha> sospechas = query.getResultList();
            return sospechas;
        } catch (Exception e) {
            System.out.println("sospechasXciudad ERROR - " + e.getMessage());
            List<EventoSospecha> sospechas = new ArrayList<>();
            return sospechas;
        }
    }

    public EventoPanico getUltimoEventoPanico(Ciudad c) {
        try {
            Query query = em.createNativeQuery("select evp.* \n"
                    + "from eventopanico evp\n"
                    + "join usuario u on evp.usuarioinformante_id = u.id\n"
                    + "join grupofamiliar gf on gf.id = u.grupofamiliar_id\n"
                    + "join codigoseguridad cod on gf.codigoasignado_id = cod.id\n"
                    + "join barrio b on cod.barrio_id = b.id\n"
                    + "join ciudad c on b.ciudad_id = c.id\n"
                    + "and c.id = " + c.getId() + "\n"
                    + "order by evp.fechacreacion desc\n"
                    + "limit 1;", EventoPanico.class);

            EventoPanico ep = (EventoPanico) query.getSingleResult();
            return ep;
        } catch (Exception e) {
            System.out.println("getUltimoEventoPanico ERROR - " + e.getMessage());
            return null;
        }
    }

    public EventoSospecha getUltimoEventoSospecha(Ciudad c) {
        try {
            Query query = em.createNativeQuery("select evp.* \n"
                    + "from eventosospecha evp\n"
                    + "join usuario u on evp.usuarioinformante_id = u.id\n"
                    + "join grupofamiliar gf on gf.id = u.grupofamiliar_id\n"
                    + "join codigoseguridad cod on gf.codigoasignado_id = cod.id\n"
                    + "join barrio b on cod.barrio_id = b.id\n"
                    + "join ciudad c on b.ciudad_id = c.id\n"
                    + "and c.id = " + c.getId() + "\n"
                    + "order by evp.fechacreacion desc\n"
                    + "limit 1;", EventoSospecha.class);

            EventoSospecha es = (EventoSospecha) query.getSingleResult();
            return es;
        } catch (Exception e) {
            System.out.println("getUltimoEventoPanico ERROR - " + e.getMessage());
            return null;
        }
    }

}
