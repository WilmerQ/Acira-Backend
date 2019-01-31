/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author wilme
 */
@javax.ws.rs.ApplicationPath("webresources")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method. It is automatically
     * populated with all resources defined in the project. If required, comment
     * out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(co.com.acira.webService.CodigoFamiliarResource.class);
        resources.add(co.com.acira.webService.DispositivosResource.class);
        resources.add(co.com.acira.webService.EventosResource.class);
        resources.add(co.com.acira.webService.HistoricoResource.class);
        resources.add(co.com.acira.webService.UsuarioResource.class);
        resources.add(co.com.acira.webService.ValidacionesResource.class);
    }

}
