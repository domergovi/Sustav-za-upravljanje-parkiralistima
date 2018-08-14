/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

/**
 *
 * @author Domagoj
 */
@ManagedBean
@RequestScoped
public class Prijava {

    /**
     * varijabla za preuzimanje korisnickog imena iz obrasca
     */
    private String korisnickoIme;
    /**
     * varijabla za preuzimanje lozinke iz obrasca
     */
    private String lozinka;
    
    /**
     * konstruktor klase
     */
    public Prijava() {
        
    }

    /**
     * metoda za dohvacanje vrijednosti varijable korisnickoIme 
     * @return 
     */
    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    /**
     * metoda za postavljanje vrijednosti varijable korisnickoIme
     * @param korisnickoIme 
     */
    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    /**
     * metoda za dohvacanje vrijednosti varijable lozinka
     * @return 
     */
    public String getLozinka() {
        return lozinka;
    }

    /**
     * metoda za postavljanje vrijednosti varijable lozinka
     * @param lozinka 
     */
    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }
    
    
}
