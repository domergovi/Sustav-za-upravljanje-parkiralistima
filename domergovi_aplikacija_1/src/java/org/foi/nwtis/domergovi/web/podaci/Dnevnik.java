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
public class Dnevnik {

    private int id;
    private String url;
    private String ipAdresa;
    private String vrijemePrijema;
    private int trajanje;
    private String korisnickoIme;
    private String sadrzaj;
    private String vrsta;

    public Dnevnik(int id, String url, String ipAdresa, String vrijemePrijema, int trajanje, String korisnickoIme, String sadrzaj, String vrsta) {
        this.id = id;
        this.url = url;
        this.ipAdresa = ipAdresa;
        this.vrijemePrijema = vrijemePrijema;
        this.trajanje = trajanje;
        this.korisnickoIme = korisnickoIme;
        this.sadrzaj = sadrzaj;
        this.vrsta = vrsta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIpAdresa() {
        return ipAdresa;
    }

    public void setIpAdresa(String ipAdresa) {
        this.ipAdresa = ipAdresa;
    }

    public String getVrijemePrijema() {
        return vrijemePrijema;
    }

    public void setVrijemePrijema(String vrijemePrijema) {
        this.vrijemePrijema = vrijemePrijema;
    }

    public int getTrajanje() {
        return trajanje;
    }

    public void setTrajanje(int trajanje) {
        this.trajanje = trajanje;
    }

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getSadrzaj() {
        return sadrzaj;
    }

    public void setSadrzaj(String sadrzaj) {
        this.sadrzaj = sadrzaj;
    }

    public String getVrsta() {
        return vrsta;
    }

    public void setVrsta(String vrsta) {
        this.vrsta = vrsta;
    }

    
    
    
}

