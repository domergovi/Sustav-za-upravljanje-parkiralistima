/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.ws.serveri;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.rest.klijenti.OWMKlijent;
import org.foi.nwtis.domergovi.web.podaci.MeteoPodaci;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@WebService(serviceName = "SOAP_WS")
public class SOAP_WS {

    /**
     * Web service operation metoda koja na temelju dobivenog id-a parkirališta
     * vraća zadnje meteo podatke zabilježene u bazi podataka u obliku objekta
     * meteoPodaci, u suprotnom vraća null
     *
     * @param korisnickoIme
     * @param lozinka
     * @param idParkiralista
     * @return
     */
    @WebMethod(operationName = "dajZadnjeMeteoPodatke")
    public MeteoPodaci dajZadnjeMeteoPodatke(@WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "idParkiralista") int idParkiralista) {
        MeteoPodaci meteoPodaci = new MeteoPodaci();

        String upit = "SELECT * FROM meteo WHERE parkiralista_id = " + idParkiralista + " "
                + "AND preuzeto = (SELECT MAX(preuzeto) FROM meteo WHERE parkiralista_id = " + idParkiralista + ") "
                + "AND id = (SELECT MAX(id) FROM meteo WHERE parkiralista_id = " + idParkiralista + ")";

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery(upit);
                while (rs.next()) {
                    upisiParametreZaMeteoPodatke(meteoPodaci, rs);
                }
                stmt.close();
                con.close();
                return meteoPodaci;
            } catch (SQLException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Web service operation metoda vraća posljednjih N meteo podataka za
     * odabrano parkiralište, ovisno o tipu SQL/DERBY
     *
     * @param korisnickoIme
     * @param lozinka
     * @param idParkiralista id parkiralista
     * @param brojPodataka koliko posljednjih meteo podataka zelimo
     * @return
     */
    @WebMethod(operationName = "dajPosljednjihNMeteoPodataka")
    public List<MeteoPodaci> dajPosljednjihNMeteoPodataka(@WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "idParkiralista") int idParkiralista, @WebParam(name = "brojPodataka") int brojPodataka) {
        List<MeteoPodaci> sviMeteoPodaci = new ArrayList<>();
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        String serverDatabase = bpk.getServerDatabase();
        String upit = "";

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            if (serverDatabase.contains("derby")) {
                upit = "SELECT * FROM METEO FETCH FIRST " + brojPodataka + " ROWS ONLY WHERE parkiralista_id=" + idParkiralista;
            } else {
                upit = "SELECT * FROM meteo WHERE parkiralista_id=" + idParkiralista + " ORDER BY id DESC LIMIT 0," + brojPodataka;
            }

            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);

                while (rs.next()) {
                    MeteoPodaci meteoPodaci = new MeteoPodaci();

                    upisiParametreZaMeteoPodatke(meteoPodaci, rs);

                    sviMeteoPodaci.add(meteoPodaci);
                }
                stmt.close();
                con.close();
                return sviMeteoPodaci;
            } catch (SQLException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * metoda služi kao dodatak na metodu dajPosljednjihNMeteoPodataka u kojoj
     * se i poziva, a u njoj se postavljaju vrijednosti u objekt meteoPodaci iz
     * rezultata upita
     *
     * @param meteoPodaci
     * @param rs
     */
    private void upisiParametreZaMeteoPodatke(MeteoPodaci meteoPodaci, ResultSet rs) {
        try {
            meteoPodaci.setHumidityValue(rs.getFloat("vlaga"));
            meteoPodaci.setPressureValue(rs.getFloat("tlak"));
            meteoPodaci.setTemperatureMax(rs.getFloat("tempmax"));
            meteoPodaci.setTemperatureMin(rs.getFloat("tempmin"));
            meteoPodaci.setTemperatureValue(rs.getFloat("temp"));
            meteoPodaci.setWindDirectionValue(rs.getFloat("vjetarsmjer"));
            meteoPodaci.setWindSpeedValue(rs.getFloat("vjetar"));
            meteoPodaci.setCloudsName(rs.getString("vrijemeopis"));
            meteoPodaci.setWeatherValue(rs.getString("vrijeme"));
            meteoPodaci.setLastUpdate(rs.getDate("preuzeto"));
        } catch (SQLException ex) {
            System.out.println("Greska: Parametre za metodu dajSveMeteoPodatkeUIntervalu nije moguće dodati!");
        }
    }

    /**
     * Web service operation metoda koja vraća sve meteo podatke u obliku liste
     * za parkiralište čiji je id dobiven kao argument u intervalu između datuma
     * koji su također dobiveni preko argumenata, u suprotnom vraća null
     *
     * @param korisnickoIme
     * @param lozinka
     * @param idParkiralista
     * @param odDatuma
     * @param doDatuma
     * @return
     */
    @WebMethod(operationName = "dajSveMeteoPodatkeUIntervalu")
    public List<MeteoPodaci> dajSveMeteoPodatkeUIntervalu(@WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "idParkiralista") int idParkiralista, @WebParam(name = "odDatuma") String odDatuma, @WebParam(name = "doDatuma") String doDatuma) {
        List<MeteoPodaci> sviMeteoPodaci = new ArrayList<>();

        String upit = "SELECT * FROM meteo WHERE parkiralista_id = " + idParkiralista + " AND preuzeto <= '"
                + doDatuma + "' AND preuzeto >= '" + odDatuma + "'";

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(upit);

                while (rs.next()) {
                    MeteoPodaci meteoPodaci = new MeteoPodaci();

                    upisiParametreZaMeteoPodatke(meteoPodaci, rs);

                    sviMeteoPodaci.add(meteoPodaci);
                }
                stmt.close();
                con.close();
                return sviMeteoPodaci;
            } catch (SQLException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Web service operation metoda koja za traženo parkiralište vraća važeće
     * meteo podatke, u suprotnom vraća null
     *
     * @param korisnickoIme
     * @param lozinka
     * @param idParkiralista
     * @return
     */
    @WebMethod(operationName = "dajVazeceMeteoPodatke")
    public MeteoPodaci dajVazeceMeteoPodatke(@WebParam(name = "korisnickoIme") String korisnickoIme, @WebParam(name = "lozinka") String lozinka, @WebParam(name = "idParkiralista") int idParkiralista) {
        MeteoPodaci mp = new MeteoPodaci();

        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        OWMKlijent owmk = new OWMKlijent(bpk.getApiKey());
        String selectUpit = "SELECT * FROM parkiralista WHERE id = " + idParkiralista;

        if (autenticirajKorisnikaUBazi(korisnickoIme, lozinka)) {
            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery(selectUpit);
                while (rs.next()) {
                    String latituda = rs.getString("latitude");
                    String longituda = rs.getString("longitude");
                    mp = owmk.getRealTimeWeather(latituda, longituda);
                }
                stmt.close();
                con.close();
                return mp;
            } catch (SQLException ex) {
                return null;
            }
        } else {
            return null;
        }
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
            System.out.println("GRESKA: spajanje s bazom WS- " + ex.getMessage());
        }

        return con;
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
            System.out.println("GRESKA: kod izvrsavanja provjere korisnika u bazi - SOAP WS - " + ex.getMessage());
        }
        return false;
    }
}
