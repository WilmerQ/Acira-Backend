/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.base;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Jorger Quintero Clase encarga de codificar con el algoritmo de
 * encriptacion md5
 */
public class Md5 {

    /**
     * Metodo que recibe un String sin encriptar y retorna una String Encriptado
     *
     * @param texto
     * @return
     */
    public static String getEncoddedString(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(texto.getBytes());
            byte[] hash = digest.digest();
            String encodded = toHexadecimal(hash);
            System.out.println(encodded);
            return encodded;
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Error: Md5-getEncoddedString: " + ex.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Metodo que recibe un Array de byte y los convierte a Hexadecimal y los
     * retorna
     *
     * @param datos
     * @return
     */
    private static String toHexadecimal(byte[] datos) {
        String resultado = "";
        ByteArrayInputStream input = new ByteArrayInputStream(datos);
        String cadAux;
        boolean ult0 = false;
        int leido = input.read();
        while (leido != -1) {
            cadAux = Integer.toHexString(leido);
            if (cadAux.length() < 2) { //Hay que a�adir un 0
                resultado += "0";
                if (cadAux.length() == 0) {
                    ult0 = true;
                }
            } else {
                ult0 = false;
            }
            resultado += cadAux;
            leido = input.read();
        }
        if (ult0)//quitamos el 0 si es un caracter aislado
        {
            resultado
                    = resultado.substring(0, resultado.length() - 2) + resultado.charAt(resultado.length() - 1);
        }
        return resultado;
    }

    public static void main(String[] args) {
        String pas = "39004995";
        System.out.println(Md5.getEncoddedString(pas));
    }
}
