/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.rest.klijenti;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;

/**
 * Jersey REST client generated for REST resource:we
 * [korisnici/{korisnickoIme}/{autentikacija}]<br>
 * USAGE:
 * <pre>
 *        KorisniciRESTAutentikacija client = new KorisniciRESTAutentikacija();
 *        Object response = client.XXX(...);
 *        // do whatever with response
 *        client.close();
 * </pre>
 *
 * @author Domagoj
 */
public class KorisniciRESTAutentikacija {

    private WebTarget webTarget;
    private Client client;
    private static final String BASE_URI = "http://localhost:8080/domergovi_WebModul_app3/webresources/";

    public KorisniciRESTAutentikacija(String korisnickoIme, String autentikacija) {
        client = javax.ws.rs.client.ClientBuilder.newClient();
        String resourcePath = java.text.MessageFormat.format("korisnici/{0}/{1}", new Object[]{korisnickoIme, autentikacija});
        webTarget = client.target(BASE_URI).path(resourcePath);
    }

    public void setResourcePath(String korisnickoIme, String autentikacija) {
        String resourcePath = java.text.MessageFormat.format("korisnici/{0}/{1}", new Object[]{korisnickoIme, autentikacija});
        webTarget = client.target(BASE_URI).path(resourcePath);
    }

    /**
     * @param responseType Class representing the response
     * @return response object (instance of responseType class)@param korisnickoIme header parameter[REQUIRED]
     * @param lozinka header parameter[REQUIRED]
     */
    public <T> T getJson(Class<T> responseType, String korisnickoIme, String lozinka) throws ClientErrorException {
        return webTarget.request(javax.ws.rs.core.MediaType.APPLICATION_JSON).header("korisnickoIme", korisnickoIme).header("lozinka", lozinka).get(responseType);
    }

    /**
     * @param responseType Class representing the response
     * @return response object (instance of responseType class)@param korisnickoIme header parameter[REQUIRED]
     * @param lozinka header parameter[REQUIRED]
     */
    public <T> T deleteJson(Class<T> responseType, String korisnickoIme, String lozinka) throws ClientErrorException {
        return webTarget.request().header("korisnickoIme", korisnickoIme).header("lozinka", lozinka).delete(responseType);
    }

    /**
     * @param responseType Class representing the response
     * @return response object (instance of responseType class)@param korisnickoIme header parameter[REQUIRED]
     * @param lozinka header parameter[REQUIRED]
     */
    public <T> T postJson(Class<T> responseType, String korisnickoIme, String lozinka) throws ClientErrorException {
        return webTarget.request().header("korisnickoIme", korisnickoIme).header("lozinka", lozinka).post(null, responseType);
    }

    /**
     * @param responseType Class representing the response
     * @return response object (instance of responseType class)@param korisnickoIme header parameter[REQUIRED]
     * @param lozinka header parameter[REQUIRED]
     */
    public <T> T putJson(Class<T> responseType, String korisnickoIme, String lozinka) throws ClientErrorException {
        return webTarget.request().header("korisnickoIme", korisnickoIme).header("lozinka", lozinka).put(null, responseType);
    }

    public void close() {
        client.close();
    }
    
}
