/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.com.acira.webService.base;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Alvaro Padilla
 */
public class ResponseMessenger {

    public Response getResponseOk(String repuesta) {
        return Response.ok(repuesta, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS").build();
    }

    public Response getResponseError(String repuesta) {
        return Response.serverError().entity(repuesta).header("Content-Type", MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Methods", "GET, POST, PATCH, PUT, DELETE, OPTIONS").build();
    }
}
