/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.Usuario;
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
public class LogicaHistorial implements Serializable {

    @PersistenceContext(unitName = "Acirav1PU")
    private EntityManager em;

    public List<EventoPanico> getAllPanicoxBarrio(Long identUsuario) {
        try {
            List<EventoPanico> panicos = new ArrayList<>();
            Usuario u = (Usuario) em.createQuery("SELECT u from Usuario u WHERE u.numeroIdentificacion=:ident").setParameter("ident", identUsuario).getSingleResult();
//            Query query = em.createNativeQuery("SELECT \n"
//                    + "  ev.*\n"
//                    + "FROM \n"
//                    + "  public.eventopanico ev\n"
//                    + "  join public.zona z on ev.zonarespondiente_id = z.id  \n"
//                    + "  or ev.zonarespondiente_id IS NULL \n"
//                    + "  join public.barrio b on z.barrio_id = b.id\n"
//                    + "  and b.id = " + u.getGrupoFamiliar().getCodigoAsignado().getBarrio().getId() + "\n"
//                    + "  group by ev.id\n"
//                    + "  ORDER BY ev.fechaCreacion DESC;", EventoPanico.class);

            Query query = em.createNativeQuery("SELECT \n"
                    + "ev.* \n"
                    + "FROM \n"
                    + "public.eventopanico ev \n"
                    + "join public.zona z on ev.zonarespondiente_id = z.id \n"
                    + "or ev.zonarespondiente_id IS NULL \n"
                    + "join public.barrio b on z.barrio_id = b.id \n"
                    + "and b.id = " + u.getGrupoFamiliar().getCodigoAsignado().getBarrio().getId() + " \n"
                    + "group by ev.id \n"
                    + "ORDER BY ev.fechaCreacion DESC;", EventoPanico.class);

            panicos.addAll((List<EventoPanico>) query.getResultList());
            List<EventoPanico> temp = (List<EventoPanico>) em.createQuery("SELECT evp FROM EventoPanico evp WHERE EVP.usuarioInformante.grupoFamiliar.codigoAsignado.barrio.id<>:id AND EVP.zonaRespondiente IS NULL").setParameter("id", u.getGrupoFamiliar().getCodigoAsignado().getBarrio().getId()).getResultList();
            System.out.println("temp size" + temp.size());
            System.out.println(panicos.removeAll(temp));
            System.out.println("panicos.size " + panicos.size());
            return panicos;
        } catch (Exception e) {
            System.out.println("Error: LogicaHistorial-getAllPanicoxBarrio: " + e.getLocalizedMessage());
            List<EventoPanico> panicos = new ArrayList<>();
            return panicos;
        }
    }

    public List<EventoSospecha> getAllSospechaxBarrio(Long identUsuario) {
        try {
            List<EventoSospecha> sospechas = new ArrayList<>();
            Usuario u = (Usuario) em.createQuery("SELECT u from Usuario u WHERE u.numeroIdentificacion=:ident").setParameter("ident", identUsuario).getSingleResult();
//            Query query = em.createNativeQuery("SELECT \n"
//                    + "ev.*\n"
//                    + "FROM \n"
//                    + "public.eventosospecha ev\n"
//                    + "join public.zona z on ev.zonarespondiente_id = z.id  \n"
//                    + "or ev.zonarespondiente_id IS NULL \n"
//                    + "join public.barrio b on z.barrio_id = b.id\n"
//                    + "and b.id = " + u.getGrupoFamiliar().getCodigoAsignado().getBarrio().getId() + "\n"
//                    + "group by ev.id\n"
//                    + "  ORDER BY ev.fechaCreacion DESC;", EventoSospecha.class);

            Query query = em.createNativeQuery("SELECT \n"
                    + "ev.* \n"
                    + "FROM \n"
                    + "public.eventosospecha ev \n"
                    + "join public.zona z on ev.zonarespondiente_id = z.id  \n"
                    + "or ev.zonarespondiente_id IS NULL \n"
                    + "join public.barrio b on z.barrio_id = b.id \n"
                    + "and b.id = " + u.getGrupoFamiliar().getCodigoAsignado().getBarrio().getId() + " \n"
                    + "group by ev.id \n"
                    + "ORDER BY ev.fechaCreacion DESC;", EventoSospecha.class);

            sospechas.addAll((List<EventoSospecha>) query.getResultList());
            List<EventoPanico> temp = (List<EventoPanico>) em.createQuery("SELECT evp FROM EventoPanico evp WHERE EVP.usuarioInformante.grupoFamiliar.codigoAsignado.barrio.id<>:id AND EVP.zonaRespondiente IS NULL").setParameter("id", u.getGrupoFamiliar().getCodigoAsignado().getBarrio().getId()).getResultList();
            System.out.println("temp size" + temp.size());
            System.out.println(sospechas.removeAll(temp));
            System.out.println("panicos.size " + sospechas.size());
            return sospechas;
        } catch (Exception e) {
            List<EventoSospecha> sospechas = new ArrayList<>();
            return sospechas;
        }
    }
}
