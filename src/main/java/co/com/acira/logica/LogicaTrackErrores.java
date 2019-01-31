/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.logica;

import io.sentry.Sentry;
import io.sentry.SentryClient;
import io.sentry.SentryClientFactory;
import io.sentry.context.Context;
import io.sentry.event.BreadcrumbBuilder;
import io.sentry.event.UserBuilder;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

/**
 *
 * @author wilme
 */
@Singleton
@LocalBean
@Startup
public class LogicaTrackErrores implements Serializable {

    public static SentryClient sentry;

    @PostConstruct
    public void init() {
        try {
            System.out.println("******************************** aqui en la contruccion ");
            String dsn = "https://1e98603fe0b148e4bafee00a9f9673d6@sentry.io/1223887";
            Sentry.init(dsn);
            sentry = SentryClientFactory.sentryClient();
            
            //logWithInstanceAPI();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    void logWithInstanceAPI() {
        // Retrieve the current context.
        Context context = sentry.getContext();

        // Record a breadcrumb in the current context. By default the last 100 breadcrumbs are kept.
        context.recordBreadcrumb(new BreadcrumbBuilder().setMessage("User made an action").build());

        // Set the user in the current context.
        context.setUser(new UserBuilder().setEmail("hello@sentry.io").build());

        Sentry.capture("This is a test");
    }
}
