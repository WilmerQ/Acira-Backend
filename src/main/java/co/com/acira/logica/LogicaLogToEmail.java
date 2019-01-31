/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import co.com.acira.base.ConfiguracionGeneral;
import co.com.acira.modelo.EventoPanico;
import co.com.acira.modelo.EventoSospecha;
import co.com.acira.modelo.Usuario;
import io.sentry.Sentry;
import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.annotation.Resource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

/**
 *
 * @author wilme
 */
@Stateless
public class LogicaLogToEmail implements Serializable {

    @Resource(name = "java:jboss/mail/gmail")
    private Session session;

    public void Registro(Usuario usuario) {
        try {
            String registro = "<table style=\"width: 100%;\" border=\"0\">\n"
                    + "<tbody>\n"
                    + "<tr style=\"height: 130px;\">\n"
                    + "<td style=\"height: 130px; width: 52.8322%; text-align: right;\"><img src=\"https://drive.google.com/uc?export=download&amp;id=1iX0na5bhzCaHbJ1rOgAZgLXlFk6T315C\" alt=\"\" width=\"288\" height=\"125\" /></td>\n"
                    + "<td style=\"height: 130px; width: 40.1678%;\"><img src=\"https://drive.google.com/uc?export=download&amp;id=1hzPnNvIJ9-WeXjzOMx4x_yJA7_15_s0C\" alt=\"\" width=\"123\" height=\"125\" /></td>\n"
                    + "</tr>\n"
                    + "</tbody>\n"
                    + "</table>\n"
                    + "<hr />\n"
                    + "<h1 style=\"text-align: center;\"><em>Bienvenido a Acira Security</em></h1>\n"
                    + "<p><em><strong>Hola " + usuario.getNombrecompleto() + ",</strong>&nbsp;</em></p>\n"
                    + "<p style=\"text-align: justify;\">Acira security es un servicio de seguridad inteligente basado en una aplicaci&oacute;n m&oacute;vil donde podr&aacute; reportar eventos de sospecha y p&aacute;nico seg&uacute;n su percepci&oacute;n. le invitamos cordialmente a observar los videos y familiarizarse con la metodologia de funcionamiento del sistema.&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\"><a href=\"https://www.youtube.com/watch?v=4Lr7qNPInuI&amp;t=8s\">https://www.youtube.com/watch?v=4Lr7qNPInuI&amp;t=8s</a>&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\"><a href=\"https://www.youtube.com/watch?v=z89wCpFWShU&amp;rel=0\">https://www.youtube.com/watch?v=z89wCpFWShU&amp;rel=0</a>&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\">Cualquier intento de prueba por favor escribir la palabra simulacro&nbsp;&nbsp;y presionar el bot&oacute;n de sospecha, con el fin de no mover recursos humanos de la polic&iacute;a nacional y seguridad privada de su barrio(si cuenta con esta).&nbsp;Se recomienda el uso responsable de esta herramienta, cualquier abuso&nbsp;traer&iacute;a consecuencias con lo descrito en el articulo 35 de el c&oacute;digo de la polic&iacute;a.</p>\n"
                    + "<p style=\"text-align: justify;\">Si tiene alguna duda con respecto al servicio o con el uso de la aplicaci&oacute;n puede responder a este correo y nosotros le atenderemos.</p>\n"
                    + "<p>Saludos,<br /> <em><strong>El equipo de Leaf Company</strong></em></p>\n"
                    + "<hr />\n"
                    + "<p>&nbsp;</p>";

            Message message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(usuario.getEmail()));
            message.setSubject("Registro en Acira Security");
            message.setFrom(new InternetAddress("soporte.acira@gmail.com", "Acira Security"));
            message.setText(registro);
            message.setHeader("Content-Type", "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println("Error: LogicaLogToEmail-Registro: " + e.getLocalizedMessage());
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error: LogicaLogToEmail-Registro: " + ex.getLocalizedMessage());
        }
    }

    public void MailToPanico(EventoPanico panico, List<String> destinarios) {
        try {
            String usuarioReporte = "Usuario anonimo";
            if (!panico.getAnonimo()) {
                usuarioReporte = panico.getUsuarioInformante().getNombrecompleto();
            }
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  hh:mm aaa");
            String dateToString = format.format(panico.getFechaCreacion());
            String mensaje = "No definido";
            if (!panico.getMensaje().isEmpty()) {
                mensaje = panico.getMensaje();
            }
            String html2 = "<table style=\"width: 100%;\">\n"
                    + "<tbody>\n"
                    + "<tr>\n"
                    + "<td style=\"width: 54.9933%; text-align: right;\"><img src=\"https://drive.google.com/uc?export=download&amp;id=1iX0na5bhzCaHbJ1rOgAZgLXlFk6T315C\" alt=\"\" width=\"289\" height=\"125\" /></td>\n"
                    + "<td style=\"width: 41.0067%;\"><img src=\"https://drive.google.com/uc?export=download&amp;id=1hzPnNvIJ9-WeXjzOMx4x_yJA7_15_s0C\" alt=\"\" width=\"122\" height=\"125\" /></td>\n"
                    + "</tr>\n"
                    + "</tbody>\n"
                    + "</table>\n"
                    + "<p>Cordial saludo,</p>\n"
                    + "<p style=\"text-align: justify;\">Acira Security ha recibido un evento de p&aacute;nico que de una u otra manera le puede afectar a usted o a su familia, a continuaci&oacute;n, presentamos los datos importantes de dicho evento para que usted tenga cuidado.</p>\n"
                    + "<p><strong>Fecha y hora:</strong>&nbsp;" + dateToString + "</p>\n"
                    + "<p><strong>Persona que reporta:&nbsp;</strong>" + usuarioReporte + "</p>\n"
                    + "<p><strong>Mensaje:</strong>&nbsp;" + mensaje + "</p>\n"
                    + "<p><strong>Ubicaci&oacute;n:&nbsp;</strong></p>\n"
                    + "<table style=\"width: 100%;\">\n"
                    + "<tbody>\n"
                    + "<tr>\n"
                    + "<td><a href=\"https://maps.google.com/maps?q=loc:" + panico.getLatitud() + "," + panico.getLongitud() + "\"><img src=\"https://maps.googleapis.com/maps/api/staticmap?autoscale=2&amp;size=640x400&amp;maptype=roadmap&amp;key=AIzaSyCUlEecVWgqILozALwCNTeV7SkvRfXiOe4&amp;format=png&amp;visual_refresh=true&amp;markers=size:mid%7Ccolor:0xff0000%7Clabel:%7C" + panico.getLatitud() + "," + panico.getLongitud() + "\" alt=\"Google Map\" /></a></td>\n"
                    + "</tr>\n"
                    + "</tbody>\n"
                    + "</table>\n"
                    + "<p>&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\"><strong>Nota:&nbsp;</strong>Este mensaje ha sido generado por el sistema de manera autom&aacute;tica, se aclara que la intenci&oacute;n de este es informar y no el causar el p&aacute;nico en la comunidad, si usted desconoce el motivo por el cual ha recibido este mensaje, por favor responda a este mismo correo y nosotros le ayudaremos.&nbsp;</p>\n"
                    + "<p>Saludos,<br /> <em><strong>El equipo de Leaf Company</strong></em></p>";

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress("soporte.acira@gmail.com", "Acira Security"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("wilmer.quintero@outlook.com"));
            message.setSubject("Acira Security recibe un evento de pánico");
            BodyPart bodyPart = new MimeBodyPart();
            bodyPart.setContent(html2, "text/html; charset=ISO-8859-1");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(bodyPart);
            if (panico.getUuidImagen() != null) {
                bodyPart = new MimeBodyPart();
                String filename = ConfiguracionGeneral.RUTA + "imagenes" + File.separator + panico.getUuidImagen() + ".jpg";
                System.out.println("filename " + filename);
                DataSource source = new FileDataSource(filename);
                bodyPart.setDataHandler(new DataHandler(source));
                bodyPart.setFileName("imagen adjunta.jpg");
                multipart.addBodyPart(bodyPart);
            }
            if (panico.getUuidAudio() != null) {
                bodyPart = new MimeBodyPart();
                String filename = ConfiguracionGeneral.RUTA + "audios" + File.separator + panico.getUuidAudio() + ".3gp";
                System.out.println("filename " + filename);
                DataSource source = new FileDataSource(filename);
                bodyPart.setDataHandler(new DataHandler(source));
                bodyPart.setFileName("audio adjunto.3gp");
                multipart.addBodyPart(bodyPart);
            }
            //message.setText(html2);
            //message.setHeader("Content-Type", "text/html");
            message.setContent(multipart);
            Transport.send(message);
        } catch (MessagingException e) {
            Sentry.init();
            Sentry.capture(e);
            System.out.println("Error: LogicaLogToEmail-MailToPanico: " + e.getMessage());
        } catch (UnsupportedEncodingException ex) {
            System.out.println("Error: LogicaLogToEmail-MailToPanico: " + ex.getMessage());
        }
    }

    public void MailToSospecha(EventoSospecha sospecha, List<String> destinarios) {

    }

    public void EnvioErrorGrave(Usuario usuario, String addresses, String topic, String textMessage) {
        try {
            String registro = "<table style=\"width: 100%;\" border=\"0\">\n"
                    + "<tbody>\n"
                    + "<tr style=\"height: 130px;\">\n"
                    + "<td style=\"height: 130px; width: 52.8322%; text-align: right;\"><img src=\"https://drive.google.com/uc?export=download&amp;id=1iX0na5bhzCaHbJ1rOgAZgLXlFk6T315C\" alt=\"\" width=\"288\" height=\"125\" /></td>\n"
                    + "<td style=\"height: 130px; width: 40.1678%;\"><img src=\"https://drive.google.com/uc?export=download&amp;id=1hzPnNvIJ9-WeXjzOMx4x_yJA7_15_s0C\" alt=\"\" width=\"123\" height=\"125\" /></td>\n"
                    + "</tr>\n"
                    + "</tbody>\n"
                    + "</table>\n"
                    + "<hr />\n"
                    + "<h1 style=\"text-align: center;\"><em>Bienvenido a Acira Security</em></h1>\n"
                    + "<p><em><strong>Hola wilmerq,</strong>&nbsp;</em></p>\n"
                    + "<p style=\"text-align: justify;\">Acira security es un servicio de seguridad inteligente basado en una aplicaci&oacute;n m&oacute;vil donde podr&aacute; reportar eventos de sospecha y p&aacute;nico seg&uacute;n su percepci&oacute;n. le invitamos cordialmente a observar los videos y familiarizarse con la metodologia de funcionamiento del sistema.&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\"><a href=\"https://www.youtube.com/watch?v=4Lr7qNPInuI&amp;t=8s\">https://www.youtube.com/watch?v=4Lr7qNPInuI&amp;t=8s</a>&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\"><a href=\"https://www.youtube.com/watch?v=z89wCpFWShU&amp;rel=0\">https://www.youtube.com/watch?v=z89wCpFWShU&amp;rel=0</a>&nbsp;</p>\n"
                    + "<p style=\"text-align: justify;\">Cualquier intento de prueba por favor escribir la palabra simulacro&nbsp;&nbsp;y presionar el bot&oacute;n de sospecha, con el fin de no mover recursos humanos de la polic&iacute;a nacional y seguridad privada de su barrio(si cuenta con esta).&nbsp;Se recomienda el uso responsable de esta herramienta, cualquier abuso&nbsp;traer&iacute;a consecuencias con lo descrito en el articulo 35 de el c&oacute;digo de la polic&iacute;a.</p>\n"
                    + "<p style=\"text-align: justify;\">Si tiene alguna duda con respecto al servicio o con el uso de la aplicaci&oacute;n puede responder a este correo y nosotros le atenderemos.</p>\n"
                    + "<p>Saludos,<br /> <em><strong>El equipo de Leaf Company</strong></em></p>\n"
                    + "<hr />\n"
                    + "<p>&nbsp;</p>";

            Message message = new MimeMessage(session);
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(addresses));
            message.setSubject(topic);
            message.setText(textMessage);
            message.setHeader("Content-Type", "text/html");
            Transport.send(message);
        } catch (MessagingException e) {
            System.out.println("Error enviando correo");
            e.printStackTrace();
        }
    }

    private void enviarCorreoelectronico(List<String> destinatarioTo, List<String> destinatarioCC, List<String> destinatarioBCC, String asunto, String cuerpoMensaje) {
        try {
            Properties props = new Properties();
            String host = "smtp.gmail.com";
            String username = "soporte.sipnat@gmail.com";
            String password = "sipnat.2016";
            props.put("mail.smtps.auth", "true");
            Session session = Session.getDefaultInstance(props);
            session.setDebug(true);

            MimeMessage message = new MimeMessage(session);
            message.setHeader("Content-Type", "text/html");
            // Quien envia el correo
            message.setFrom(new InternetAddress("soporte.sipnat@gmail.com"));

            for (int i = 0; i < destinatarioTo.size(); i++) {
                if (destinatarioTo.get(i) != null) {
                    message.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(destinatarioTo.get(i)));
                }
            }

            for (int i = 0; i < destinatarioCC.size(); i++) {
                if (destinatarioCC.get(i) != null) {
                    message.addRecipient(Message.RecipientType.CC,
                            new InternetAddress(destinatarioCC.get(i)));
                }
            }
            for (int i = 0; i < destinatarioBCC.size(); i++) {
                if (destinatarioBCC.get(i) != null) {
                    message.addRecipient(Message.RecipientType.BCC,
                            new InternetAddress(destinatarioBCC.get(i)));
                }
            }
            message.setSubject(asunto);
            message.setContent(cuerpoMensaje, "text/html");
            Transport t = session.getTransport("smtps");
            t.connect(host, username, password);
            t.sendMessage(message, message.getAllRecipients());
            t.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

    }
}
