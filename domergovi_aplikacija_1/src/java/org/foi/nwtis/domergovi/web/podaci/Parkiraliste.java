/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.foi.nwtis.domergovi.web.podaci;

/**
 *
 * @author dkermek
 */
public class Parkiraliste {
    private int id;
    private String naziv;
    private String adresa;
    private Lokacija geoloc;
    private int brojParkirnihMjesta;
    private int brojUlaznoIzlaznihMjesta;

    public Parkiraliste(int id, String naziv, String adresa, Lokacija geoloc, int brojMjesta, int brojUlazIzlaz) {
        this.id = id;
        this.naziv = naziv;
        this.adresa = adresa;
        this.geoloc = geoloc;
        this.brojParkirnihMjesta = brojMjesta;
        this.brojUlaznoIzlaznihMjesta = brojUlazIzlaz;
    }
    
    public int getBrojUlaznoIzlaznihMjesta() {
        return brojUlaznoIzlaznihMjesta;
    }

    public void setBrojUlaznoIzlaznihMjesta(int brojUlaznoIzlaznihMjesta) {
        this.brojUlaznoIzlaznihMjesta = brojUlaznoIzlaznihMjesta;
    }
        
    public int getBrojParkirnihMjesta() {
        return brojParkirnihMjesta;
    }

    public void setBrojParkirnihMjesta(int brojParkirnihMjesta) {
        this.brojParkirnihMjesta = brojParkirnihMjesta;
    }

    public Lokacija getGeoloc() {
        return geoloc;
    }

    public void setGeoloc(Lokacija geoloc) {
        this.geoloc = geoloc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }      
	
    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }        
}
