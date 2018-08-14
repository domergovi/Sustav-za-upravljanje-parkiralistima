/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.ejb.sb;

import javax.ejb.Stateful;
import javax.ejb.LocalBean;
import org.foi.nwtis.domergovi.rest.klijenti.KorisniciRESTAutentikacija;

/**
 *
 * @author Domagoj
 */
@Stateful
@LocalBean
public class StatefulMQTTZrno {

    /**
     * metoda za autenticiranje korisnika u aplikaciji 2 koja vraća true ako su ispravni podaci korisnika, inače false
     * @param korisnickoIme
     * @param lozinka
     * @return 
     */
    public boolean autenticirajKorisnika(String korisnickoIme, String lozinka){
        KorisniciRESTAutentikacija objektREST = new KorisniciRESTAutentikacija("korisnickoIme", "autentikacija");
        String porukaREST = objektREST.getJson(String.class, korisnickoIme, lozinka);
        
        return porukaREST.contains("OK");
    }
}
