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
public class AudioEventos extends HttpServlet {

    @EJB
    private CommonsBean cb;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idAudio = request.getParameter("id");
        System.out.println("+++++++++ " + idAudio);
        try {
            Gson gson = new Gson();
            Adjunto adjunto = (Adjunto) cb.getByOneFieldWithOneResult(Adjunto.class, "uuid", idAudio);

            if (adjunto != null) {
                List<EventoPanico> eventosPanico = cb.getByOneField(EventoPanico.class, "uuidAudio", idAudio);
                List<EventoSospecha> eventosSospecha = cb.getByOneField(EventoSospecha.class, "uuidAudio", idAudio);

                if (!eventosPanico.isEmpty() || !eventosSospecha.isEmpty()) {
                    File f = new File(ConfiguracionGeneral.RUTA + "audios" + File.separator + idAudio + ".3gp");
                    byte[] data = FileUtils.readFileToByteArray(f);
                    response.setContentType("audio/3gp");
                    response.setHeader("Content-Disposition", "filename=\"" + idAudio + ".3gp" + "\"");
                    response.setContentLength(data.length);
                    response.getOutputStream().write(data);

                }
            }
        } catch (Exception e) {
            System.out.println("Error: AudioEventos-doGet: " + e.getLocalizedMessage());
        }
    }

}
