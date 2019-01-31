/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import co.com.acira.clases.respuestaSoapUnimagdalena;
import co.com.acira.unimagdalena.soap.WCFValidaciones;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.util.EncodingUtils;

/**
 *
 * @author wilme
 */
@Stateless
@LocalBean
public class LogicaUnimagdalena implements Serializable {

    @PersistenceContext(unitName = "Acirav1PU")
    private EntityManager em;

    public respuestaSoapUnimagdalena ValidarEstudiante(String cedula, String codigoEstudiantil) {
        try {
            String key = Token(codigoEstudiantil + cedula + "ACIRA");
            System.out.println("key " + key);
            WCFValidaciones cFValidaciones = new WCFValidaciones();
            String res = cFValidaciones.getBasicHttpBindingIWCFValidaciones().getValidarusuario(codigoEstudiantil, cedula, key);
            Gson gson = new Gson();
            String aux = Desencriptar(FixSPChart(res), key);
            aux = aux.substring(1, aux.length() - 1);
            System.out.println("aux: " + aux);
            //respuestaSoapUnimagdalena temp = gson.fromJson(aux, respuestaSoapUnimagdalena.class);
            return gson.fromJson(aux, respuestaSoapUnimagdalena.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Error: LogicaUnimagdalena-ValidarEstudiante: " + e.getLocalizedMessage());
            return null;
        }
    }

    private String Desencriptar(String res, String key) {
        try {
            byte[] decodedBytes = Base64.decodeBase64(res);
            System.out.println("Res codificado " + new String(decodedBytes, StandardCharsets.UTF_8));

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(key.getBytes(StandardCharsets.UTF_8));
            String md5 = EncodingUtils.getString(digestOfPassword, "UTF-8");
            System.out.println("keyBytes codificado " + md5);
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
            for (int j = 0, k = 16; j < 8;) {
                keyBytes[k++] = keyBytes[j++];
            }
            String textDesencriptado = decrypt(keyBytes, decodedBytes);
            System.out.println(textDesencriptado);
            return textDesencriptado;
        } catch (NoSuchAlgorithmException ex) {
            System.out.println("Error: LogicaUnimagdalena-Desencriptar: " + ex.getLocalizedMessage());
            return null;
        }
    }

    private String decrypt(byte[] keyArray, byte[] encrypted) {
        try {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
            SecretKeySpec key = new SecretKeySpec(keyArray, 0, 24, "DESede");
            Cipher cipher = Cipher.getInstance("DESEDE/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] plainText2 = cipher.doFinal(encrypted);
            return new String(plainText2, StandardCharsets.UTF_8);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException ex) {
            System.out.println("Error: LogicaUnimagdalena-decrypt: " + ex.getLocalizedMessage());
            return null;
        }
    }

    private String FixSPChart(String sTheInput) {
        sTheInput = sTheInput.replace('-', '+');
        sTheInput = sTheInput.replace('*', '/');
        sTheInput = sTheInput.replace('!', '=');
        return sTheInput;
    }

    private String Token(String codigo) {
        Date d = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String DateToStr = dateFormat.format(d);
        String temp = codigo + "*" + DateToStr + "+.+";
        System.out.println(temp);
        String token = getMD5Hash(temp);
        return token.toUpperCase();
    }

    private String getMD5Hash(String data) {
        String result = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] array = md.digest(data.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : array) {
                sb.append(String.format("%02X", b));
            }
            result = sb.toString();
        } catch (UnsupportedEncodingException | NoSuchAlgorithmException e) {
            System.out.println("Error: LogicaUnimagdalena-getMD5Hash: " + e.getLocalizedMessage());
        }
        return result;
    }
}
