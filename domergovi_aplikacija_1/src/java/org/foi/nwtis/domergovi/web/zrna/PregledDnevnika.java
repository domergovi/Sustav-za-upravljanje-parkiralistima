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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.bean.SessionScoped;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.web.podaci.Dnevnik;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@ManagedBean
@SessionScoped
public class PregledDnevnika {

    /**
     * varijabla za id dnevnika
     */
    private int id;
    /**
     * varijabla za url iz dnevnika
     */
    private String url;
    /**
     * varijabla za ipAdresa iz dnevnika
     */
    private String ipAdresa;
    /**
     * varijabla za vrijemePrijema iz dnevnika
     */
    private String vrijemePrijema;
    /**
     * varijabla za trajanje iz dnevnika
     */
    private int trajanje;
    /**
     * varijabla za korisnici_id iz dnevnika
     */
    private int korisnici_id;
    /**
     * varijabla za uneseni pocetak intervala iz obrasca
     */
    private String odDatuma;
    /**
     * varijabla za uneseni kraj intervala iz obrasca
     */
    private String doDatuma;

    /**
     * lista za preuzimanje podataka iz dnevnika
     */
    private List<Dnevnik> listaPodatakaDnevnika;

    /**
     * metoda dohvaca vrijednost liste listaPodatakaDnevnika
     *
     * @return
     */
    public List<Dnevnik> getListaPodatakaDnevnika() {
        return listaPodatakaDnevnika;
    }

    /**
     * metoda postavlja vrijednost liste listaPodatakaDnevnika
     *
     * @param listaPodatakaDnevnika
     */
    public void setListaPodatakaDnevnika(List<Dnevnik> listaPodatakaDnevnika) {
        this.listaPodatakaDnevnika = listaPodatakaDnevnika;
    }

    /**
     * varijabla za straničenje
     */
    private int brojPodatakaZaPrikaz;

    /**
     * varijabla za dohvacanje vrsteZapisa sa obrasca
     */
    private String vrstaZapisa;

    /**
     * konstrukor klase PregledDnevnika u kojem se postavlja broj za stranicenje
     * iz konfiguracije
     */
    public PregledDnevnika() {
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

    /**
     * metoda dohvaca sve podatke iz dnevnika u danom intervalu zadanom u
     * obrascu ovisno o upisanim datumima izvrsavaju se upiti
     */
    public void dohvatiPodatkeDnevnikaIzBaze() {
        listaPodatakaDnevnika = new ArrayList();
        String upit = "SELECT * FROM dnevnik ";
        upit = odluciOUpitu(upit);
        if (!"".equals(upit)) {
            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);

                while (rs.next()) {
                    Dnevnik dnevnik = new Dnevnik(rs.getInt("id"), rs.getString("url"),
                            rs.getString("ip_adresa"), rs.getString("vrijeme_prijema"), rs.getInt("trajanje"), 
                            rs.getString("korisnicko_ime"), rs.getString("sadrzaj"), rs.getString("vrsta"));
                    listaPodatakaDnevnika.add(dnevnik);
                }
                stmt.close();
                con.close();
            } catch (SQLException ex) {
                System.out.println("GRESKA: greska kod rada s dnevnikom - PregledDnenika");
            }
        }
    }

    /**
     * pomocna metoda u kojoj se na temelju ispravnosti datuma odlucuje koji ce
     * se upit izvrsit
     *
     * @param upit
     * @return
     */
    public String odluciOUpitu(String upit) {
        if (vrstaZapisa.isEmpty()) {
            if (provjeriUneseniDatum(odDatuma) && provjeriUneseniDatum(doDatuma)) {
                upit += "WHERE vrijeme_prijema >= '" + odDatuma + "' AND vrijeme_prijema <= '" + doDatuma + "'";
                System.out.println("UPIT: " + upit);
                return upit;
            } else if (provjeriUneseniDatum(odDatuma) && !provjeriUneseniDatum(doDatuma)) {
                upit += "WHERE vrijeme_prijema >= '" + odDatuma + "'";
                System.out.println("UPIT: " + upit);
                return upit;
            } else if (!provjeriUneseniDatum(odDatuma) && provjeriUneseniDatum(doDatuma)) {
                upit += "WHERE vrijeme_prijema <= '" + doDatuma + "'";
                System.out.println("UPIT: " + upit);
                return upit;
            } else if (odDatuma.isEmpty() && doDatuma.isEmpty()) {
                upit = "SELECT * FROM dnevnik ";
                System.out.println("UPIT: " + upit);
                return upit;
            }
        } else {
            return odluciOUpituSaVrstomZapisa(upit);
        }

        return "";
    }

    /**
     * metoda koja sluzi za kreiranje upita kao i metoda odluciOUpitu samo što
     * je u ovoj u obzir uzeta i varijabla vrstaZapisa
     * @param upit
     * @return 
     */
    public String odluciOUpituSaVrstomZapisa(String upit) {
        if (provjeriUneseniDatum(odDatuma) && provjeriUneseniDatum(doDatuma)) {
            upit += "WHERE vrijeme_prijema >= '" + odDatuma + "' AND vrijeme_prijema <= '" + doDatuma + "' AND vrsta ='" + vrstaZapisa + "'";
            System.out.println("UPIT: " + upit);
            return upit;
        } else if (provjeriUneseniDatum(odDatuma) && !provjeriUneseniDatum(doDatuma)) {
            upit += "WHERE vrijeme_prijema >= '" + odDatuma + "' AND vrsta ='" + vrstaZapisa + "'";
            System.out.println("UPIT: " + upit);
            return upit;
        } else if (!provjeriUneseniDatum(odDatuma) && provjeriUneseniDatum(doDatuma)) {
            upit += "WHERE vrijeme_prijema <= '" + doDatuma + "' AND vrsta ='" + vrstaZapisa + "'";
            System.out.println("UPIT: " + upit);
            return upit;
        }else if (!provjeriUneseniDatum(odDatuma) && !provjeriUneseniDatum(doDatuma)) {
            upit += "WHERE vrsta ='" + vrstaZapisa + "'";
            System.out.println("UPIT: " + upit);
            return upit;
        }
        return "";
    }

    /**
     * metoda provjerava jel uneseni datum u trazenom formatu za pretragu baze
     * podataka
     *
     * @return true - ispravan format datuma, false - neispravan format datuma
     */
    private boolean provjeriUneseniDatum(String datum) {
        try {
            SimpleDateFormat trazeniFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
            Date datumOdValidacija = trazeniFormat.parse(datum);

            return true;
        } catch (ParseException ex) {
            return false;
        }
    }

    /**
     * metoda dohvaca vrijednosti varijable vrstaZapisa
     *
     * @return
     */
    public String getVrstaZapisa() {
        return vrstaZapisa;
    }

    /**
     * metoda postavlja vrijednost varijable vrstaZapisa
     *
     * @param vrstaZapisa
     */
    public void setVrstaZapisa(String vrstaZapisa) {
        this.vrstaZapisa = vrstaZapisa;
    }

    /**
     * metoda dohvaca vrijednosti varijable odDatuma
     *
     * @return
     */
    public String getOdDatuma() {
        return odDatuma;
    }

    /**
     * metoda postavlja vrijednosti varijable odDatuma
     *
     * @param odDatuma
     */
    public void setOdDatuma(String odDatuma) {
        this.odDatuma = odDatuma;
    }

    /**
     * metoda dohvaca vrijednosti varijable doDatuma
     *
     * @return
     */
    public String getDoDatuma() {
        return doDatuma;
    }

    /**
     * metoda postavlja vrijednosti varijable doDatuma
     *
     * @param doDatuma
     */
    public void setDoDatuma(String doDatuma) {
        this.doDatuma = doDatuma;
    }

    /**
     * metoda dohvaca vrijednosti varijable brojPodatakaZaPrikaz
     *
     * @return
     */
    public int getBrojPodatakaZaPrikaz() {
        return brojPodatakaZaPrikaz;
    }

    /**
     * metoda postavlja vrijednosti varijable brojPodatakaZaPrikaz
     *
     * @param brojPodatakaZaPrikaz
     */
    public void setBrojPodatakaZaPrikaz(int brojPodatakaZaPrikaz) {
        this.brojPodatakaZaPrikaz = brojPodatakaZaPrikaz;
    }

    /**
     * metoda dohvaca vrijednosti varijable id
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * metoda postavlja vrijednosti varijable id
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * metoda dohvaca vrijednosti varijable url
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * metoda postavlja vrijednosti varijable url
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * metoda dohvaca vrijednosti varijable ipAdresa
     *
     * @return
     */
    public String getIpAdresa() {
        return ipAdresa;
    }

    /**
     * metoda postavlja vrijednosti varijable ipAdresa
     *
     * @param ipAdresa
     */
    public void setIpAdresa(String ipAdresa) {
        this.ipAdresa = ipAdresa;
    }

    /**
     * metoda dohvaca vrijednosti varijable vrijemePrijema
     *
     * @return
     */
    public String getVrijemePrijema() {
        return vrijemePrijema;
    }

    /**
     * metoda postavlja vrijednosti varijable vrijemePrijema
     *
     * @param vrijemePrijema
     */
    public void setVrijemePrijema(String vrijemePrijema) {
        this.vrijemePrijema = vrijemePrijema;
    }

    /**
     * metoda dohvaca vrijednosti varijable trajanje
     *
     * @return
     */
    public int getTrajanje() {
        return trajanje;
    }

    /**
     * metoda postavlja vrijednosti varijable trajanje
     *
     * @param trajanje
     */
    public void setTrajanje(int trajanje) {
        this.trajanje = trajanje;
    }

    /**
     * metoda dohvaca vrijednosti varijable korisnici_id
     *
     * @return
     */
    public int getKorisnici_id() {
        return korisnici_id;
    }

    /**
     * metoda postavlja vrijednosti varijable korisnici_id
     *
     * @param korisnici_id
     */
    public void setKorisnici_id(int korisnici_id) {
        this.korisnici_id = korisnici_id;
    }

}
