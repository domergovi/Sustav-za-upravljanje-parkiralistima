/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.web.podaci.Korisnik;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@ManagedBean
@RequestScoped
public class PregledKorisnika {

    /**
     * varijabla za id korisnika
     */
    private int id;
    /**
     * varijabla za ime korisnika
     */
    private String ime;
    /**
     * varijabla za prezime korisnika
     */
    private String prezime;
    /**
     * varijabla za korisnicko ime korisnika
     */
    private String korisnickoIme;
    /**
     * varijabla za lozinka korisnika
     */
    private String lozinka;
    /**
     * varijabla za straničenje
     */
    private int brojPodatakaZaPrikaz;
    
    /**
     * konstruktor klase u kojem se dohvaca podatak o broju redova za prikaz u tablici
     */
    public PregledKorisnika() {
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        brojPodatakaZaPrikaz = Integer.valueOf(bpk.getTableNumRowsToShow());
    }
    
    
    /**
     * metoda za uspostavljanje veze prema bazi podataka na temelju parametara
     * iz konfiguracijske datoteke
     *
     * @return
     */
    private Connection postaviVezuNaBazu() {
        Connection con = null;
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        String url = bpk.getServerDatabase() + bpk.getUserDatabase();
        String korisnik = bpk.getUserUsername();
        String lozinka = bpk.getUserPassword();

        try {
            Class.forName(bpk.getDriverDatabase());
            con = DriverManager.getConnection(url, korisnik, lozinka);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("GRESKA: spajanje s bazom PregledKorisnika - " + ex.getMessage());
        }

        return con;
    }
    
    
    public List<Korisnik> dohvatiKorisnikeIzBaze(){
        List<Korisnik> sviKorisnici = new ArrayList<>();

        String upit = "SELECT * FROM korisnici";
        
        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);

            while (rs.next()) {
                Korisnik korisnik = new Korisnik(rs.getInt("id"), rs.getString("ime"), 
                        rs.getString("prezime"), rs.getString("korisnicko_ime"), 
                        rs.getString("lozinka"), rs.getInt("grupa_id"));
                sviKorisnici.add(korisnik);
            }
            stmt.close();
            con.close();
            return sviKorisnici;
        } catch (SQLException ex) {
            return null;
        }
    }

    /**
     * metoda za dohvacanje vrijednosti varijable brojPodatakaZaPrikaz
     * @return 
     */
    public int getBrojPodatakaZaPrikaz() {
        return brojPodatakaZaPrikaz;
    }

    /**
     * metoda za postavljanje vrijednosti varijable brojPodatakaZaPrikaz
     * @param brojPodatakaZaPrikaz 
     */
    public void setBrojPodatakaZaPrikaz(int brojPodatakaZaPrikaz) {
        this.brojPodatakaZaPrikaz = brojPodatakaZaPrikaz;
    }
        

    /**
     * mmetoda za dohvacanje vrijednosti varijable id
     * @return 
     */
    public int getId() {
        return id;
    }

    /**
     * metoda za postavljanje vrijednosti varijable id
     * @param id 
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * metoda za dohvacanje vrijednosti varijable ime
     * @return 
     */
    public String getIme() {
        return ime;
    }

    /**
     * metoda za postavljanje vrijednosti varijable ime
     * @param ime 
     */
    public void setIme(String ime) {
        this.ime = ime;
    }

    /**
     * metoda za dohvacanje vrijednosti varijable prezime
     * @return 
     */
    public String getPrezime() {
        return prezime;
    }

    /**
     * metoda za postavljanje vrijednosti varijable prezime
     * @param prezime 
     */
    public void setPrezime(String prezime) {
        this.prezime = prezime;
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
