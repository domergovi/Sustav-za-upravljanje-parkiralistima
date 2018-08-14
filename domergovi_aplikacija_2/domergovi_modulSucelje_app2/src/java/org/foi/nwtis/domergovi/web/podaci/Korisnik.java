/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.podaci;

/**
 *
 * @author Domagoj
 */
public class Korisnik {
    
    private int id;
    private String ime;
    private String prezime;
    private String korisnicko_ime;
    private String lozinka;
    private int grupa_id;

    public Korisnik(int id, String ime, String prezime, String korisnicko_ime, String lozinka, int grupa_id) {
        this.id = id;
        this.ime = ime;
        this.prezime = prezime;
        this.korisnicko_ime = korisnicko_ime;
        this.lozinka = lozinka;
        this.grupa_id = grupa_id;
    }

    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public String getKorisnicko_ime() {
        return korisnicko_ime;
    }

    public void setKorisnicko_ime(String korisnicko_ime) {
        this.korisnicko_ime = korisnicko_ime;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public int getGrupa_id() {
        return grupa_id;
    }

    public void setGrupa_id(int grupa_id) {
        this.grupa_id = grupa_id;
    }
    
    
    
    
    
}
