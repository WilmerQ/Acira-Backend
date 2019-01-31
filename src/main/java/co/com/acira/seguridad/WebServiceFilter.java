///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package co.com.acira.seguridad;
//
//import java.io.IOException;
//import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.ContainerResponseContext;
//import javax.ws.rs.container.ContainerResponseFilter;
//import javax.ws.rs.ext.Provider;
//
///**
// *
// * @author wilme
// */
//@Provider
//public class WebServiceFilter implements ContainerResponseFilter {
//
//    @Override
//    public void filter(final ContainerRequestContext requestContext,
//            final ContainerResponseContext cres) throws IOException {
//        System.out.println("ejecutando filter cors");
//        cres.getHeaders().add("Access-Control-Allow-Origin", "*");
//        //requestContext.getSecurityContext().
//        cres.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, userkey, tokenkey");
//        cres.getHeaders().add("Access-Control-Allow-Credentials", "true");
//        cres.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
//        //cres.getHeaders().add("Access-Control-Max-Age", "1209600");
//    }
//
//}
