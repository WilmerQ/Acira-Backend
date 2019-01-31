/**
 *
 */
package co.com.acira.base;

import javax.faces.context.FacesContext;

/**
 * @author Jorger Quintero clase que contiene metodos para enviar y leer
 * variables desde sa Session
 */
public class SessionOperations {

    /**
     * metodo que recibe dos parametros:
     *
     * @param key es identificador de la variable
     * @param object sera el objeto a enviar a la sesion
     */
    public static void setSessionValue(String key, Object object) {
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put(key, object);
    }

    /**
     * metodo que a traves de la key realiza la busca del objeto en la sesion y
     * lo retorna
     *
     * @param key
     * @return
     */
    public static Object getSessionValue(String key) {
        return FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(key);
    }
}
