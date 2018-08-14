/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.dretve;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.rest.klijenti.OWMKlijent;
import org.foi.nwtis.domergovi.web.podaci.Lokacija;
import org.foi.nwtis.domergovi.web.podaci.MeteoPodaci;
import org.foi.nwtis.domergovi.web.podaci.Parkiraliste;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */

/*
    dretva PreuzmiMeteoPodatke koja za spremljena parkirališta 
    odnosno njihove geo lokacije preuzima meterološke podatke i sprema ih u 
    odgovarajuću tablicu pod nazivom METEO. Dretva se pokreće u slušaču 
    aplikacije kod kreiranje konteksta. Interval dretve određen je konfiguracijskim podatkom.
 */
public class PreuzmiMeteoPodatke extends Thread {

    /**
     * varijabla true ili false na temelju koje dretva radi
     */
    public static boolean radi = true;

    /**
     * varijabla za vrijeme cekanja dretve ucitan iz konfiguracije
     */
    private int spavanje;

    /**
     * statična varijabla koja se regulira iz radne dretve preko nje se server
     * postavlja u pasivno stanje i radi provjera jel server već u pasivnom
     * stanju
     */
    public static boolean parametarPasivno = false;

    /**
     * metoda za slucaj prekida rada dretve
     */
    @Override
    public void interrupt() {
        super.interrupt();
    }

    /**
     * metoda u intervalima (određdenim konfiguracijom) dodaje nove meteo
     * podatke u tablicu meteo na temelju postojecih parkiralista u bazi
     */
    @Override
    public void run() {
        while (radi) {
            long pocetak = System.currentTimeMillis();
            System.out.println("DRETVA METEO - POCETAK: "+new Date());
            
            if (!preuzmiParkiralista().isEmpty()) {
                dodajMeteoPodatkeZaPrimljenaParkiralista(preuzmiParkiralista());
                System.out.println("Dodani su meteo podaci o prakiralistima u tablicu meteo!");
            }else
                System.out.println("Lista parkiralista je prazna pa nije moguce dodati nove meteo podatke!");


            long kraj = System.currentTimeMillis() - pocetak;
            long vrijemeSpavanja = (spavanje - kraj);

            try {
                sleep(vrijemeSpavanja);
            } catch (InterruptedException ex) {
                System.out.println("Greska kod spavanja dretve meteo - PreuzmiMeteoPodatke!");
            }
            
            if (parametarPasivno == true) {
                System.out.println("Postavljam dretvu u pasivno stanje!");
                postaviDretvuUPasivnoStanje();
            }
        }

    }

    /**
     * metoda za uspostavljanje veze prema bazi podataka na temelju parametara
     * iz konfiguracijske datoteke
     *
     * @return
     */
    public Connection postaviVezuNaBazu() {
        Connection con = null;
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        String url = bpk.getServerDatabase() + bpk.getUserDatabase();
        String korisnik = bpk.getUserUsername();
        String lozinka = bpk.getUserPassword();

        try {
            Class.forName(bpk.getDriverDatabase());
            con = DriverManager.getConnection(url, korisnik, lozinka);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("GRESKA: spajanje s bazom - dretva PreuzmiMeteoPodatke " + ex.getMessage());
        }

        return con;
    }

    /**
     * metoda preuzima podatke o parkiralištima iz baze podataka i sprema ih u
     * listu koju vraća
     *
     * @return
     */
    public List<Parkiraliste> preuzmiParkiralista() {
        List<Parkiraliste> listaParkiralista = new ArrayList();
        try {
            String upit = "SELECT * FROM parkiralista";
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);

            while (rs.next()) {
                String latitude = rs.getString("latitude");
                String longitude = rs.getString("longitude");
                Lokacija lokacija = new Lokacija(latitude, longitude);
                Parkiraliste parkiraliste = new Parkiraliste(rs.getInt("id"), rs.getString("naziv"),
                        rs.getString("adresa"), lokacija, rs.getInt("broj_parkirnih_mjesta"), rs.getInt("ulazno_izlazna_mjesta"));

                listaParkiralista.add(parkiraliste);
            }

            stmt.close();
            con.close();

        } catch (SQLException ex) {
            System.out.println("GRESKA: metoda preuzmiParkiralista - dretva PreuzmiMeteoPodatke " + ex.getMessage());
        }
        return listaParkiralista;
    }

    /**
     * metoda na temelju dobivene liste parkiralista dodaje vazece meteo podatke u bazu podataka
     * @param listaParkiralista 
     */
    public void dodajMeteoPodatkeZaPrimljenaParkiralista(List<Parkiraliste> listaParkiralista) {
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        OWMKlijent owmk = new OWMKlijent(bpk.getApiKey());
        
        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            for (Parkiraliste parkiraliste : listaParkiralista) {
                MeteoPodaci mp = owmk.getRealTimeWeather(parkiraliste.getGeoloc().getLatitude(), parkiraliste.getGeoloc().getLongitude());
                
                String upit = "INSERT INTO meteo (vrijeme, vrijemeOpis, temp, tempMin, tempMax, vlaga, tlak, vjetar, "
                        + "vjetarSmjer, parkiralista_id) VALUES "
                        + "('"+mp.getWeatherValue()+"','"+mp.getCloudsName()+"',"+mp.getTemperatureValue()+","
                        +mp.getTemperatureMin()+","+mp.getTemperatureMax()+","+mp.getHumidityValue()+","+mp.getPressureValue()+","
                        + mp.getWindSpeedValue()+","+mp.getWindDirectionValue()+","+parkiraliste.getId()+")";
                
                stmt.execute(upit);
            }
            
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("GRESKA: metoda preuzmiParkiralista - dretva dodajMeteoPodatkeZaPrimljenaParkiralista " + ex.getMessage());
        }

    }

    /**
     * metoda provjera jel parametarPasivno postavljen na true, ako je onda
     * stavi dretvu u WAIT
     */
    public synchronized void postaviDretvuUPasivnoStanje() {
        try {
            this.wait();
        } catch (InterruptedException ex) {
            System.out.println("Greska kod stavljanja dretve u pasivno stanje - PreuzmiMeteoPodatke!");
        }
    }

    /**
     * metoda za postavljanje dretve u aktivno stanje probudi dretvu ako je
     * varijabla parametarPasivno postavljena na false i dretva u stanju čekanja
     */
    public void postaviDretvuUAktivnoStanje() {
        synchronized (this) {
            if (this.getState().equals(State.WAITING)) {
                this.notify();
            }
        }

    }

    /**
     * metoda za postavljanje inicijalnih podataka za rad dretve kao što je
     * preuzimanje intervala iz konfiguracije
     */
    @Override
    public synchronized void start() {
        System.out.println("POKRENUO DRETVU METEOPODACI!");
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        spavanje = Integer.valueOf(bpk.getIntervalDretveZaMeteoPodatke()) * 1000;
        super.start();
    }

    /**
     * metoda za prekid rada dretve za preuzimanje Meteo podataka i buđenje u
     * slučaju da je postavljena na sleep
     */
    public void prekiniRadDretveMeteo() {
        radi = false;
    }

}
