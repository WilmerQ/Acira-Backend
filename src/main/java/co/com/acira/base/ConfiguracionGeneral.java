/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.base;

/**
 *
 * @author wilme
 */
public class ConfiguracionGeneral {

    //Configuracion de conexion de base de datos auxiliar
    static final public String URL = "jdbc:postgresql://localhost:5432/acirav1";
//    static final public String USUARIO = "postgres";
    static final public String PASS = "acira";
    static final public String USUARIO = "postgres";
//    static final public String PASS = "2Ht5LVHn";

    //ruta para almacenar archivos
//    static final public String RUTA = "D:\\AciraAdjuntos\\";
    static final public String RUTA = "/home/acira/AciraAdjuntos/";

    //carpetas que deben existir
    //audios
    //imagenes
    //documentos
    //configuracion firebase 
    static final public String AuthKey = "AAAAa7zwMYQ:APA91bEdScvbceh2IV6AEXvG6MM1SKz9YER-Gj-uTYTp5VFLATtatHH8JPg15S42rXEuOMl9ycRmgUwn9cBqlU6TqxeG93pbLoMrVU795bwnUHPrhPGcdzFLsw0sk0QPmjheD-ULYsmd";
    static final public String idProjectFirebase = "462731358596";

    //configuracion para estudientes
    static final public String nombreBarrioMadalena = "universidad del magdalena";

    //configuracion de mensajeria para los estudiantes
    static final public Boolean SMS = Boolean.TRUE;

    //configuracion de rutas para conexion
    static final public String IP = "acira.ddns.net";
    static final public String PORT = "80";
//    static final public String IP = "10.10.10.103";
//    static final public String PORT = "8080";

    private final String urlServletImagen = "http://" + IP + ":" + PORT + "/Acira/ImagenEventos?id=";

    private final String urlServletAudios = "http://" + IP + ":" + PORT + "/Acira/AudioEventos?id=";

    public String getUrlServletImagen() {
        return urlServletImagen;
    }

    public String getUrlServletAudios() {
        return urlServletAudios;
    }

}
