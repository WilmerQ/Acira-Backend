/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.servlet;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.logica.CommonsBean;
import co.com.acira.modelo.Adjunto;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import com.google.gson.Gson;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author wilme
 */
//@WebServlet(urlPatterns = {"/imagenperfilservlet"})
public class ImagenEventos extends HttpServlet {

    @EJB
    private CommonsBean cb;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idDeImagen = request.getParameter("id");
        System.out.println("+++++++++ " + idDeImagen);
        try {
            Gson gson = new Gson();
            Adjunto adjunto = (Adjunto) cb.getByOneFieldWithOneResult(Adjunto.class, "uuid", idDeImagen);

            if (adjunto != null) {
                List<EventoPanico> eventosPanico = cb.getByOneField(EventoPanico.class, "uuidImagen", idDeImagen);
                List<EventoSospecha> eventosSospecha = cb.getByOneField(EventoSospecha.class, "uuidImagen", idDeImagen);

                if (!eventosPanico.isEmpty() || !eventosSospecha.isEmpty()) {
                    File f = new File(ConfiguracionGeneral.RUTA + "imagenes" + File.separator + idDeImagen + ".jpg");
                    byte[] data = FileUtils.readFileToByteArray(f);
                    response.setContentType("image");
                    response.setContentLength(data.length);
                    response.getOutputStream().write(data);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: ImagenEventos-doGet: " + e.getLocalizedMessage());
        }

    }

}
