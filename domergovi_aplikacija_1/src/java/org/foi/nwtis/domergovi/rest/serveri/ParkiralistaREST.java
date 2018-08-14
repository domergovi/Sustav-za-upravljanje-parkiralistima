/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.rest.serveri;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.rest.klijenti.GMKlijent;
import org.foi.nwtis.domergovi.web.podaci.Lokacija;
import org.foi.nwtis.domergovi.web.podaci.Parkiraliste;
import org.foi.nwtis.domergovi.ws.klijenti.Vozilo;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.domergovi.ws.klijenti.ParkiranjeWS;

/**
 * REST Web Service
 *
 * @author Domagoj
 */
@Path("parkiralista")
public class ParkiralistaREST {

    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest httpRequest;

    /**
     * Creates a new instance of ParkiralistaREST
     */
    public ParkiralistaREST() {
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
            System.out.println("GRESKA: spajanje s bazom REST - " + ex.getMessage());
        }

        return con;
    }

    /**
     * Retrieves representation of an instance of
     * org.foi.nwtis.domergovi.rest.serveri.ParkiralistaREST
     *
     * metoda vraća popis svih parkirališta, njihovih adresa, geo lokacija, broj
     * parkirnih mjesta u application/json formatu.
     * + zapis podataka u dnevnik
     *
     * @param korisnickoIme korisnicko ime
     * @param lozinka lozinka
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String getJson(@HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        long pocetak = System.currentTimeMillis();
        String odgovor = "";
        List<Parkiraliste> svaParkiralista = new ArrayList<>();

        String selectUpit = "SELECT * FROM parkiralista WHERE grupa_id = (SELECT grupa_id FROM korisnici WHERE korisnicko_ime = '" + korisnickoIme + "')";

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(selectUpit);
                while (rs.next()) {
                    Lokacija lokacija = new Lokacija(rs.getString("latitude"), rs.getString("longitude"));

                    Parkiraliste parkiraliste = new Parkiraliste(Integer.valueOf(rs.getString("id")), rs.getString("naziv"),
                            rs.getString("adresa"), lokacija, Integer.valueOf(rs.getString("broj_parkirnih_mjesta")),
                            Integer.valueOf(rs.getString("ulazno_izlazna_mjesta")));
                    svaParkiralista.add(parkiraliste);
                }
                stmt.close();
                con.close();

                String jsonOdgovor = new Gson().toJson(svaParkiralista);
                odgovor = "{\"odgovor\": " + jsonOdgovor + ",\"status\": \"OK\"}";

            } catch (SQLException ex) {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguće dohvatiti popis svih parkiralista!\"}";
            }
        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Korisnik ne postoji u bazi podataka!\"}";
        }

        zapisiPodatkeUDnevnikRada("", korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda na temelju dobivenih podataka o parkiralištu dodaje novo
     * parkiralište u bazu o obavljenoj radnji vraca poruku u application/json
     * formatu
     *
     * + dodavanje parkiralista na ParkiralisteWS + postavljanje parkiralista u
     * stanje AKTIVAN na ParkiranjeWS jer je prvo u stanju PASIVAN
     * 
     * + zapis podataka u dnevnik rada
     *
     * @param podaci podaci o parkiralistu
     * @param korisnickoIme korisnicko ime
     * @param lozinka lozinka
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String postJson(String podaci, @HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        long pocetak = System.currentTimeMillis();
        String odgovor = "";
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            String[] parsiraniPodaci = parsirajPodatkeParkiralista(podaci);
            Lokacija lokacija = new GMKlijent(bpk.getGMApiKey()).getGeoLocation(parsiraniPodaci[1]);

            if (provjeriPostojanjeId(parsiraniPodaci[0])) {
                String upit = "INSERT INTO parkiralista (naziv, adresa, latitude, longitude, broj_parkirnih_mjesta, ulazno_izlazna_mjesta, grupa_id)"
                        + " VALUES('" + parsiraniPodaci[0] + "','" + parsiraniPodaci[1] + "'," + lokacija.getLatitude() + "," + lokacija.getLongitude()
                        + ",'" + parsiraniPodaci[2] + "', '" + parsiraniPodaci[3] + "', (SELECT id FROM grupa WHERE naziv = '" + korisnickoIme + "'))";

                String upitID = "SELECT MAX(id) as ukupno FROM parkiralista";

                try {
                    Connection con = postaviVezuNaBazu();
                    Statement stmt = con.createStatement();
                    stmt.execute(upit);
                    ResultSet rs = stmt.executeQuery(upitID);
                    int id = 0;
                    if (rs.next()) {
                        id = rs.getInt("ukupno");
                    }
                    stmt.close();
                    con.close();
                    odgovor = "{\"odgovor\": [], \"status\": \"OK\"}";
                    ParkiranjeWS.dodajNovoParkiralisteGrupi(korisnickoIme, lozinka, id, parsiraniPodaci[0], parsiraniPodaci[1], Integer.valueOf(parsiraniPodaci[2]));
                    ParkiranjeWS.aktivirajParkiralisteGrupe(korisnickoIme, lozinka, id);
                } catch (SQLException ex) {
                    odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguće dodati novo parkiralište u bazu!\"}";
                }
            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Navedeno parkiraliste već postoji u bazi podataka!\"}";
            }

        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Korisnik ne postoji u bazi podataka!\"}";
        }
        zapisiPodatkeUDnevnikRada(podaci, korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda služi za parsiranje podataka o parkiralištiu i spremanje u niz
     * redom naziv, adresa, brojParkirnihMjesta i brojUlaznoIzlaznihMjesta
     *
     * @param podaci
     * @return
     */
    private String[] parsirajPodatkeParkiralista(String podaci) {
        JsonParser parser = new JsonParser();
        JsonObject json = parser.parse(podaci).getAsJsonObject();
        String[] poljeParametara = new String[5];

        poljeParametara[0] = json.get("naziv").toString().replace("\"", "");
        poljeParametara[1] = json.get("adresa").toString().replace("\"", "");
        poljeParametara[2] = json.get("brojParkirnihMjesta").toString().replace("\"", "");
        poljeParametara[3] = json.get("brojUlaznoIzlaznihMjesta").toString().replace("\"", "");

        return poljeParametara;
    }

    /**
     * metoda provjerava postojanje id-a parkiralista na temelju dobivenog
     * naziva u bazi podataka te ukoliko postoji vraca false, a u suprotnom
     * vraca true
     *
     * @param naziv
     * @return
     */
    private boolean provjeriPostojanjeId(String naziv) {
        String upit = "SELECT COUNT(id) as ukupno FROM parkiralista WHERE naziv = '" + naziv + "'";

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            int broj = 0;
            while (rs.next()) {
                broj = rs.getInt("ukupno");
            }
            stmt.close();
            con.close();

            if (broj > 0) {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("Greska kod SQL upita: " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * metoda vraca poruku o pogresci u application/json formatu
     *
     * @param korisnickoIme
     * @param lozinka
     * @return
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putJson(@HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";
        long pocetak = System.currentTimeMillis();
        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            odgovor = "{\"odgovor\": [], "
                    + "\"status\": \"ERR\", "
                    + "\"poruka\": \"Nije dozvoljeno\"}";
        } else {
            odgovor = "{\"odgovor\": [], "
                    + "\"status\": \"ERR\", "
                    + "\"poruka\": \"Korisnik ne postoji u bazi podataka\"}";
        }
        zapisiPodatkeUDnevnikRada("", korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda vraca poruku o pogresci u application/json formatu
     *
     * @param korisnickoIme
     * @param lozinka
     * @return
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String deleteJson(@HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";
        long pocetak = System.currentTimeMillis();
        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            odgovor = "{\"odgovor\": [], "
                    + "\"status\": \"ERR\", "
                    + "\"poruka\": \"Nije dozvoljeno\"}";
        } else {
            odgovor = "{\"odgovor\": [], "
                    + "\"status\": \"ERR\", "
                    + "\"poruka\": \"Korisnik ne postoji u bazi podataka\"}";
        }
        zapisiPodatkeUDnevnikRada("", korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda vraća na bazi putanje {id} podatke izabranog parkirališta kao
     * odgovor u application/json formatu a u suprotnom se vraca pogreska
     *
     * + dodano je povlacenje statusa sa servisa ParkiranjeWS za odabrano
     * parkiraliste u odgovor
     *
     * @param id id parkiralista
     * @param korisnickoIme
     * @param lozinka
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String getJson(@PathParam("id") String id, @HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";
        String naziv = "";
        String adresa = "";
        int brojParkMjesta = 0;
        int brojUIMjesta = 0;
        long pocetak = System.currentTimeMillis();
        if (provjeriPostojanjeDobivenogID(id) && autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            String selectUpit = "SELECT id, naziv, adresa, broj_parkirnih_mjesta, "
                    + "ulazno_izlazna_mjesta FROM parkiralista WHERE id = " + Integer.valueOf(id);

            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();

                String status = "";
                ResultSet rs = stmt.executeQuery(selectUpit);
                while (rs.next()) {
                    naziv = rs.getString("naziv");
                    adresa = rs.getString("adresa");
                    brojParkMjesta = rs.getInt("broj_parkirnih_mjesta");
                    brojUIMjesta = rs.getInt("ulazno_izlazna_mjesta");
                    status = ParkiranjeWS.dajStatusParkiralistaGrupe(korisnickoIme, lozinka, rs.getInt("id")).value();
                }
                stmt.close();
                con.close();
                odgovor = "{\"odgovor\": [{\"id\": " + id + ", \"naziv\": \"" + naziv + "\", \"adresa\": \"" + adresa
                        + "\", \"brojParkirnihMjesta\": \"" + brojParkMjesta + "\", \"brojUlaznoIzlaznihMjesta\": \""
                        + brojUIMjesta + "\", \"statusParkiralista\": \""+status+"\"}], \"status\": \"OK\"}";
            } catch (SQLException ex) {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Pogreska u radu s bazom\"}";
            }

        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Parkiraliste ili korisnik ne postoji u bazi podataka\"}";
        }
        zapisiPodatkeUDnevnikRada("", korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda provjerava postojanje id-a parkiralista u bazi podataka na temelju
     * dobivenog id-a, ako parkiraliste postoji tada vraca true, u suprotnom
     * false
     *
     * @param id
     * @return
     */
    private boolean provjeriPostojanjeDobivenogID(String id) {
        String upit = "SELECT COUNT(*) as ukupno FROM parkiralista WHERE id = " + Integer.valueOf(id);

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            int broj = 0;
            while (rs.next()) {
                broj = rs.getInt("ukupno");
            }
            stmt.close();
            con.close();

            if (broj == 1) {
                return true;
            }

        } catch (SQLException ex) {
            System.out.println("Greska kod SQL upita: " + ex.getMessage());
            return false;
        }

        return false;
    }

    /**
     * metoda na temelju dobivenih id-a, naziva i adrese ažurira parkiralište u
     * bazi podataka ako ono postoji u suportnom javlja poruku o pogrešci i sve
     * to u u application/json formatu
     *
     * @param id
     * @param podaci
     * @param korisnickoIme
     * @param lozinka
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String putJson(@PathParam("id") String id, String podaci, @HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";
        long pocetak = System.currentTimeMillis();

        if (provjeriPostojanjeDobivenogID(id) && autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");

            String[] parsiraniPodaci = parsirajPodatkeParkiralista(podaci);

            GMKlijent gmk = new GMKlijent(bpk.getGMApiKey());
            Lokacija lokacija = gmk.getGeoLocation(parsiraniPodaci[1]);

            String upit = "UPDATE parkiralista SET naziv = '" + parsiraniPodaci[0] + "', adresa = '" + parsiraniPodaci[1] + "', "
                    + "latitude = " + lokacija.getLatitude() + ", longitude = " + lokacija.getLongitude()
                    + ", broj_parkirnih_mjesta = '" + parsiraniPodaci[2] + "', ulazno_izlazna_mjesta = '" + parsiraniPodaci[3] + "' WHERE id = " + Integer.valueOf(id);

            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                stmt.executeUpdate(upit);
                stmt.close();
                con.close();
                odgovor = "{\"odgovor\": [], \"status\": \"OK\"}";
                dohvatiSvaParkiralistaIPostaviNaServis(korisnickoIme, lozinka);
            } catch (SQLException ex) {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguce azurirati parkiraliste u bazi\"}";
            }
        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Parkiraliste ili korisnik ne postoje u bazi podataka\"}";
        }
        zapisiPodatkeUDnevnikRada(podaci, korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda na bazi putanje {id} briše izabrano parkiralište te vraća podatke
     * u application/json formatu.
     *
     * + poziv metode obrisiParkiralisteGrupe koja brise parkiraliste i sa servisa ParkiranjeWS
     * @param id
     * @param korisnickoIme
     * @param lozinka
     * @return
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public String deleteJson(@PathParam("id") String id, @HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";
        long pocetak = System.currentTimeMillis();

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            if (provjeriPostojanjeDobivenogID(id) && provjeriPostojanjeVozilaZaParkiraliste(id)) {

                String upit = "DELETE FROM parkiralista WHERE id = " + Integer.valueOf(id);

                try {
                    Connection con = postaviVezuNaBazu();
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(upit);
                    stmt.close();
                    con.close();
                    odgovor = "{\"odgovor\": [], \"status\": \"OK\"}";
                    ParkiranjeWS.obrisiParkiralisteGrupe(korisnickoIme, lozinka, Integer.valueOf(id));
                } catch (SQLException ex) {
                    odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguce obrisati parkiraliste u bazi\"}";
                }

            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Parkiraliste ne postoji ili na odabranom parkiralistu postoje vozila\"}";
            }
        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Korisnik ne postoji u bazi podataka\"}";
        }
        zapisiPodatkeUDnevnikRada("", korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda provjerava postojanje vozila za parkiraliste na temelju dobivenog
     * id-a parkiralista
     *
     * @param id
     * @return
     */
    private boolean provjeriPostojanjeVozilaZaParkiraliste(String id) {
        String upit = "SELECT COUNT(*) as ukupno FROM vozila WHERE parkiralista_id = " + Integer.valueOf(id);

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            int broj = 0;
            while (rs.next()) {
                broj = rs.getInt("ukupno");
            }
            stmt.close();
            con.close();

            if (broj > 0) {
                return false;
            }

        } catch (SQLException ex) {
            System.out.println("Greska kod SQL upita: " + ex.getMessage());
            return false;
        }

        return true;
    }

    /**
     * metoda vraća na bazi putanje {id}/vozila/ vraća podatke o svim vozilima
     * odabranog parkirališta kao odgovor u application/json formatu a u
     * suprotnom se vraca pogreska
     *
     * @param idParkiralista
     * @param vozila
     * @param korisnickoIme
     * @param lozinka
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/vozila/")
    public String getJson(@PathParam("id") String idParkiralista, @PathParam("vozila") String vozila, @HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";
        long pocetak = System.currentTimeMillis();

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            if (provjeriPostojanjeDobivenogID(idParkiralista)) {
                String status = ParkiranjeWS.dajStatusGrupe(korisnickoIme, lozinka).value();
                
                if (status.equals("BLOKIRAN") || status.equals("AKTIVAN")){
                    List<Vozilo> svaVozilaOdabranogParkiralista = ParkiranjeWS.dajSvaVozilaParkiralistaGrupe(korisnickoIme, lozinka, Integer.valueOf(idParkiralista));

                    String jsonOdgovor = new Gson().toJson(svaVozilaOdabranogParkiralista);
                    odgovor = "{\"odgovor\": " + jsonOdgovor + ",\"status\": \"OK\"}";
                }else{
                    odgovor = "{\"odgovor\": [],\"status\": \"OK\"}";
                }
                
            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Parkiraliste ne postoji\"}";
            }
        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\", \"poruka\": \"Korisnik ne postoji u bazi podataka\"}";
        }

        zapisiPodatkeUDnevnikRada("", korisnickoIme, pocetak);
        return odgovor;
    }

    /**
     * metoda radi autentikaciju korisnika u bazi podataka na temelju dobivenih
     * parametara korisnickog imena i lozinke ako je pronaden korisnik, vraća
     * true, inace vraca false
     *
     * @return
     */
    private boolean autenticirajKorisnikaUBazi(String korisnickoIme, String lozinka) {
        String upit = "SELECT COUNT(id) as ukupno FROM korisnici WHERE korisnicko_ime = '" + korisnickoIme + "' AND lozinka = '" + lozinka + "'";

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            int broj = 0;
            while (rs.next()) {
                broj = rs.getInt("ukupno");
            }
            stmt.close();
            con.close();

            if (broj == 1) {
                return true;
            }

        } catch (SQLException ex) {
            System.out.println("GRESKA: kod izvrsavanja provjere korisnika u bazi - REST - " + ex.getMessage());
        }
        return false;
    }

    /**
     * metoda sluzi za zapis podataka u dnevnik rada dohvaca trenutnu ip adresu,
     * url te ostale parametre potrebne za unos u dnevnik rada
     *
     * @param naredba
     */
    private void zapisiPodatkeUDnevnikRada(String naredba, String korisnickoIme, long pocetakObrade) {

        try {
            long trajanje = System.currentTimeMillis() - pocetakObrade;
            String ipAdresa = Inet4Address.getLocalHost().getHostAddress();
            String url = httpRequest.getRequestURI();

            String upit = "INSERT INTO dnevnik (url, ip_adresa, trajanje, korisnicko_ime, sadrzaj, vrsta) VALUES "
                    + "('" + url + "','" + ipAdresa + "'," + trajanje + ",'" + korisnickoIme + "','" + naredba + "','REST')";

            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                stmt.execute(upit);
                stmt.close();
                con.close();
                System.out.println("Podaci zapisani u dnevnik rada - ParkiralistaREST");

            } catch (SQLException ex) {
                System.out.println("GRESKA: kod dodavanja novog zapisa u dnevnik - ParkiralistaREST - " + ex.getMessage());
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(ParkiralistaREST.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * metoda prvo obrise sva parkiralista sa servisa i potom dohvati sva
     * parkiralista iz baze i postavi na WS Parkiranje
     *
     * @param korisnickoIme
     * @param lozinka
     */
    public void dohvatiSvaParkiralistaIPostaviNaServis(String korisnickoIme, String lozinka) {
        ParkiranjeWS.obrisiSvaParkiralistaGrupe(korisnickoIme, lozinka);

        String upit = "SELECT * FROM parkiralista";

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                ParkiranjeWS.dodajNovoParkiralisteGrupi(korisnickoIme, lozinka, rs.getInt("id"), rs.getString("naziv"), rs.getString("adresa"), rs.getInt("broj_parkirnih_mjesta"));
            }
            stmt.close();
            con.close();

        } catch (SQLException ex) {
            System.out.println("GRESKA: greska kod dohvacanja korisnika iz baze i postavljanja na servis ParkiranjeWS - ParkiralisteREST");
        }

    }

}
