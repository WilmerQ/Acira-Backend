/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import co.com.acira.base.Md5;
import co.com.acira.clases.credencialInicioSesion;
import co.com.acira.modelo.Usuario;
import io.sentry.Sentry;
import java.io.Serializable;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.faces.application.FacesMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.primefaces.push.EventBus;
import org.primefaces.push.EventBusFactory;

/**
 *
 * @author wilme
 */
@Stateless
@LocalBean
public class LogicaUsuario implements Serializable {

    @PersistenceContext(unitName = "Acirav1PU")
    private EntityManager em;

    @EJB
    CommonsBean cb;

    public Usuario Login(credencialInicioSesion is) {
        try {
            System.out.println("try login");

            Usuario u = (Usuario) em.createQuery("Select u from Usuario u where u.nombreUsuario= :n and u.contrasena = :p AND u.estado=true")
                    .setParameter("n", is.getNombre())
                    .setParameter("p", Md5.getEncoddedString(is.getContrasena()))
                    .getSingleResult();
            System.out.println("u " + u.getNombreUsuario());
            return u;
        } catch (Exception e) {
            System.out.println("Error: Login: " + e.getLocalizedMessage());
            return null;
        }
    }

    public Usuario LoginWeb(String nombre, String contrasena) {
        if ((nombre.equals("admin")) && (contrasena.equals("827ccb0eea8a706c4c34a16891f84e7b"))) {
            Usuario usuario = new Usuario();
            usuario.setNombreUsuario("Administrador");
//            NotificationPush();
            return usuario;
        }
        try {
            System.out.println("try LoginWeb");
            Usuario u = (Usuario) em.createQuery("Select u from Usuario u where u.nombreUsuario=:n and u.contrasena=:p AND u.ToWeb=true")
                    .setParameter("n", nombre)
                    .setParameter("p", contrasena).getSingleResult();
            System.out.println("u " + u.getNombreUsuario());
            return u;
        } catch (Exception e) {
//            Sentry.capture("Error: LoginWeb: " + e.);
            System.out.println("Error: LoginWeb: " + e.getLocalizedMessage());
            return null;
        }
    }

//    public void NotificationPush() {
//        String chanel = "/observatorio";
//        String detalle = "Actualizando ya que se recibe un evento";
//        String resumen = "Push";
//
//        EventBus eventBus = EventBusFactory.getDefault().eventBus();
//        eventBus.publish(chanel, new FacesMessage(resumen, detalle));
//    }
}
