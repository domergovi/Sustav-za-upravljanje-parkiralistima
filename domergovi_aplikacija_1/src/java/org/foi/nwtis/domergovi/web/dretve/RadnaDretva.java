/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.dretve;

import org.foi.nwtis.domergovi.pomocniPaket.SlanjeEmailPoruke;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import org.foi.nwtis.domergovi.konfiguracije.Konfiguracija;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.posluzitelj.Posluzitelj;
import org.foi.nwtis.domergovi.ws.klijenti.ParkiranjeWS;

/**
 *
 * @author Domagoj
 */
public class RadnaDretva extends Thread {

    /**
     * varijabla za preuzimanje važećeg socketa
     */
    private Socket socket;

    /**
     * varijabla za vrijednost konteksta
     */
    public ServletContext kontekst;

    /**
     * objekt dretve PreuzmiMeteoPodatke za rad sa neavedenom dretvom
     */
    public PreuzmiMeteoPodatke dretvaMeteo;

    /**
     * varijabla za spremanje vremena početka obrade zahtjeva
     */
    public long pocetakObrade = 0;

    /**
     * konstruktor radne dretve koji preuzima i postavlja vrijednost socketa,
     * konteksta i dretve za meteo podatke
     *
     * @param socket
     * @param kontekst
     * @param dretva
     */
    public RadnaDretva(Socket socket, ServletContext kontekst, PreuzmiMeteoPodatke dretva) {
        this.socket = socket;
        this.kontekst = kontekst;
        this.dretvaMeteo = dretva;
    }

    /**
     * run metoda dretve u kojoj se dohvaća naredba korisnika i na temelju nje
     * izvršavaju određene radnje
     */
    @Override
    public void run() {
        try {
            String naredba = Posluzitelj.dohvatiNaredbu(socket);
            pocetakObrade = System.currentTimeMillis();
            System.out.println("NAREDBA: " + naredba);

            // provjeri ispravnost naredbe za posluzitelja i za grupu, inače greška
            // provjera za autentikaciju korisnika - samo korisnicko ime i lozinka
            if (provjeriIspravnostKorisnika(naredba)) {
                // provjeri dobivene parametre u bazi podataka
                System.out.println("KORISNIK OK");
                if (autenticirajKorisnikaUBazi(naredba)) {
                    posaljiPorukuKorisniku("OK 10; Korisnik autenticiran");
                } else {
                    posaljiPorukuKorisniku("ERR 11; Ne postoji korisnik ili ne odgovara lozinka");
                }
            } else if (provjeriIspravnostKomandeZaPosluzitelja(naredba)) {
                System.out.println("NAREDBA ZA posluzitelja - OK");
                String komanda = dohvatiNaredbuIzKomande(naredba, 0);

                if ("DODAJ".equals(komanda) || "PAUZA".equals(komanda) || "KRENI".equals(komanda)
                        || "PASIVNO".equals(komanda) || "AZURIRAJ".equals(komanda)) {
                    izaberiMetoduNaTemeljuKomandePosluzitelj(komanda, naredba);
                } else if ("AKTIVNO".equals(komanda) || "STANI".equals(komanda) || "STANJE".equals(komanda) || "LISTAJ".equals(komanda)) {
                    izaberiMetoduNaTemeljuKomandePosluzitelj_v2(komanda, naredba);
                }

            } else if (provjeriIspravnostKomandeZaGrupu(naredba)) {
                System.out.println("NAREDBA ZA grupu - OK");
                String komanda = dohvatiNaredbuIzKomande(naredba, 1);
                izaberiMetoduNaTemeljuKomandeGrupa(komanda, naredba);

            } else {
                System.out.println("GRESKA: Neispravna komanda!");
            }

        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod rada dretve RADNA DRETVA!");
        }
    }

    /**
     * start metoda dretve
     */
    @Override
    public synchronized void start() {
        super.start();
    }

    /**
     * interrupt metoda dretve
     */
    @Override
    public void interrupt() {
        super.interrupt();
    }

    /**
     * metoda za provjeru naredbe za posluzitelja dobivene od korisnika metoda
     * vraća istinu ili laž ovisno o tome zadovoljava li dobivena naredba
     * regularni izraz
     */
    private boolean provjeriIspravnostKomandeZaPosluzitelja(String komanda) {

        String regex = "(KORISNIK ([A-Za-z0-9_\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž]+); "
                + "LOZINKA ([A-Za-z0-9_\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž-]+);( "
                + "((AZURIRAJ \"[A-Za-z0-9\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž-]+\" \"[A-Za-z0-9\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž]+\";)|"
                + "(DODAJ \"[A-Za-z0-9\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž-]+\" \"[A-Za-z0-9\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž]+\";)|"
                + "(PAUZA;)|(KRENI;)|(PASIVNO;)|(AKTIVNO;)|(STANI;)|(STANJE;)|(LISTAJ;))){0,1})";

        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(komanda);
        boolean status = m.matches();
        if (status) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * metoda za provjeru naredbe za grupu dobivene od korisnika metoda vraća
     * istinu ili laž ovisno o tome zadovoljava li dobivena naredba regularni
     * izraz
     */
    private boolean provjeriIspravnostKomandeZaGrupu(String komanda) {

        String regex = "KORISNIK ([A-Za-z0-9_\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž]+); "
                + "LOZINKA ([A-Za-z0-9_\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž-]+);"
                + "( ((GRUPA ((DODAJ;)|(PREKID;)|(KRENI;)|(PAUZA;)|(STANJE;)){1}))){0,1}";

        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(komanda);
        boolean status = m.matches();
        if (status) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * metoda za provjeru naredbe za grupu dobivene od korisnika metoda vraća
     * istinu ili laž ovisno o tome zadovoljava li dobivena naredba regularni
     * izraz
     */
    private boolean provjeriIspravnostKorisnika(String komanda) {

        String regex = "KORISNIK ([A-Za-z0-9_\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž]+); LOZINKA ([A-Za-z0-9_\\ć\\š\\č\\đ\\ž\\Ć\\Š\\Č\\Đ\\Ž-]+);";

        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(komanda);
        boolean status = m.matches();
        if (status) {
            return true;
        } else {
            return false;
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
        BP_Konfiguracija bpk = (BP_Konfiguracija) kontekst.getAttribute("BP_Konfig");
        String url = bpk.getServerDatabase() + bpk.getUserDatabase();
        String korisnik = bpk.getUserUsername();
        String lozinka = bpk.getUserPassword();

        try {
            Class.forName(bpk.getDriverDatabase());
            con = DriverManager.getConnection(url, korisnik, lozinka);
        } catch (SQLException | ClassNotFoundException ex) {
            System.out.println("GRESKA: spajanje s bazom - " + ex.getMessage());
        }

        return con;
    }

    /**
     * metoda za dodavanje novog korisnika u bazu podataka na temelju dobivene
     * naredbe i komande DODAJ
     *
     * @param naredba
     */
    public void dodajKorisnikaUBazuPodataka(String naredba) {
        String[] nizZaParsiranje = naredba.split(";");
        String[] podaciKomandeDodaj = nizZaParsiranje[2].trim().split(" ");
        String prezime = podaciKomandeDodaj[1].replace("\"", "");
        String ime = podaciKomandeDodaj[2].replace("\"", "");

        if (!autenticirajKorisnikaUBazi(naredba)) {
            String[] podaciKorisnika = dohvatiKorisnickoImeILozinkuIzKomande(naredba);

            dodajGrupuZaKorisnika(podaciKorisnika[0]);

            String upit = "INSERT INTO korisnici (ime, prezime, korisnicko_ime, lozinka, grupa_id) VALUES "
                    + "('" + ime + "','" + prezime + "','" + podaciKorisnika[0] + "','" + podaciKorisnika[1] + "', "
                    + "(SELECT id FROM grupa WHERE naziv = '" + podaciKorisnika[0] + "'))";

            try {
                Connection con = postaviVezuNaBazu();
                Statement stmt = con.createStatement();
                stmt.execute(upit);
                stmt.close();
                con.close();
                posaljiPorukuKorisniku("OK 10; Korisnik je dodan u bazu podataka");

            } catch (SQLException ex) {
                System.out.println("GRESKA: kod dodavanja novog korisnika u bazu podataka - RadnaDretva - " + ex.getMessage());
            }
        } else {
            posaljiPorukuKorisniku("ERR 10; Korisnik vec postoji u bazi podataka");
        }
    }

    /**
     * metoda za dodavanje grupe u tablicu grupa na temelju korisnickog imena
     * novog korisnika
     *
     * @param korisnickoIme
     */
    public void dodajGrupuZaKorisnika(String korisnickoIme) {
        String upit = "INSERT INTO grupa (naziv) VALUES ('" + korisnickoIme + "')";

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            stmt.execute(upit);
            stmt.close();
            con.close();
        } catch (SQLException ex) {
            System.out.println("GRESKA: kod dodavanja nove grupe u bazu podataka - RadnaDretva - " + ex.getMessage());
        }
    }

    /**
     * metoda zaazuriranje korisnika u bazi podataka na temelju dobivene naredbe
     * i komande AZURIRAJ
     *
     * @param naredba
     */
    public void azurirajKorisnikaUBaziPodataka(String naredba) {
        String[] nizZaParsiranje = naredba.split(";");
        String[] podaciKomandeAuriraj = nizZaParsiranje[2].trim().split(" ");
        String prezime = podaciKomandeAuriraj[1].replace("\"", "");
        String ime = podaciKomandeAuriraj[2].replace("\"", "");

        String[] podaciKorisnika = dohvatiKorisnickoImeILozinkuIzKomande(naredba);

        String upit = "UPDATE korisnici SET ime ='" + ime + "', prezime='" + prezime + "' "
                + "WHERE korisnicko_ime='" + podaciKorisnika[0] + "' AND lozinka='" + podaciKorisnika[1] + "'";

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            stmt.execute(upit);
            stmt.close();
            con.close();
            posaljiPorukuKorisniku("OK 10; Korisnik je azuriran u bazi podataka");

        } catch (SQLException ex) {
            System.out.println("GRESKA: kod azuriranja korisnika u bazi podataka - RadnaDretva - " + ex.getMessage());
        }

    }

    /**
     * metoda za za provjeru i postavljanje parametra pauze u posluzitelju, u
     * suprotnom javlja da je server vec postavljen u stanje pauze
     */
    public void izvrsiPauzuPosluzitelj() {
        if (Posluzitelj.parametarPauza == false) {
            Posluzitelj.parametarPauza = true;
            posaljiPorukuKorisniku("OK 10; Server stavljen u stanje PAUZE");
        } else {
            posaljiPorukuKorisniku("ERR 12; Server je vec u stanju PAUZE");
        }
    }

    /**
     * metoda za pokretanje servera iz stanja pauze pomoću varijable
     * parametarPauza postavljene na serveru sustava ako server nije bio u
     * stanju pauze vraća poruku pogreške
     */
    public void izvrsiPokretanjePosluzitelj() {
        if (Posluzitelj.parametarPauza == true) {
            Posluzitelj.parametarPauza = false;
            posaljiPorukuKorisniku("OK 10; Server je pokrenut iz stanja PAUZE");
        } else {
            posaljiPorukuKorisniku("ERR 13; Server nije bio u stanju PAUZE");
        }
    }

    /**
     * metoda za postavljanje dretve za preuzimanje meteo podataka u pasivno
     * stanje, ako je vec u tom stanju ispisuje poruku pogreske
     */
    public void izvrsiPasivnoPosluzitelj() {
        if (PreuzmiMeteoPodatke.parametarPasivno == false) {
            PreuzmiMeteoPodatke.parametarPasivno = true;
            posaljiPorukuKorisniku("OK 10; Dretva za preuzimanje meteo podataka je bila u AKTIVNOM stanju i sada je postavljena u PASIVNO");
        } else {
            posaljiPorukuKorisniku("ERR 14; Dretva za preuzimanje meteo podataka je vec u PASIVNOM stanju");
        }
    }

    /**
     * metoda za postavljanje dretve za preuzimanje meteo podataka u aktivno
     * stanje, ako je vec u tom stanju ispisuje poruku pogreske preko objekta
     * dretve poziva metodu za postavljanje u aktivno stanje
     */
    public void izvrsiAktivnoPosluzitelj() {
        if (PreuzmiMeteoPodatke.parametarPasivno == true) {
            PreuzmiMeteoPodatke.parametarPasivno = false;
            dretvaMeteo.postaviDretvuUAktivnoStanje();
            posaljiPorukuKorisniku("OK 10; Dretva za preuzimanje meteo podataka je bila u PASIVNOM stanju i sada je postavljena u AKTIVNO");
        } else {
            posaljiPorukuKorisniku("ERR 15; Dretva za preuzimanje meteo podataka je vec u AKTIVNOM stanju");
        }
    }

    /**
     * metoda za izvrsavanje zasutavljanja dretve meteo podataka i posluzitelja
     * (gasi i aplikaciju?)
     */
    public void izvrsiZaustavljanjePosluzitelj() {
        if (Posluzitelj.radi == true) {
            Posluzitelj.ugasiSocketIPrekiniPosluzitelj();
            posaljiPorukuKorisniku("OK 10; Potpuno prekinut rad dretve servera i meteo i ugasen socket");
        } else {
            posaljiPorukuKorisniku("ERR 16; Rad servera je već prekinut");
        }
    }

    /**
     * metoda za provjeru stanja u kojem se nalaze dretve posluzitelja i dretve
     * za meteo podatke
     */
    public void izvrsiStanjePosluzitelj() {
        if (Posluzitelj.parametarPauza == false && PreuzmiMeteoPodatke.parametarPasivno == false) {
            posaljiPorukuKorisniku("OK 11; preuzima sve komande i preuzima meteo podatke");
        } else if (Posluzitelj.parametarPauza == false && PreuzmiMeteoPodatke.parametarPasivno == true) {
            posaljiPorukuKorisniku("OK 12; preuzima sve komande i ne preuzima meteo podatke");
        } else if (Posluzitelj.parametarPauza == true && PreuzmiMeteoPodatke.parametarPasivno == false) {
            posaljiPorukuKorisniku("OK 13; preuzima samo posluziteljske komande i preuzima meteo podatke");
        } else if (Posluzitelj.parametarPauza == true && PreuzmiMeteoPodatke.parametarPasivno == true) {
            posaljiPorukuKorisniku("OK 14; preuzima samo posluziteljske komande i ne preuzima meteo podatke");
        }
    }

    /**
     * metoda za dobivanje liste svih korisnika iz baze podataka u JSON obliku
     */
    public void izvrsiListanjePosluzitelj() {
        JsonArray poljeKorisnika = new JsonArray();
        String upit = "SELECT * FROM korisnici";
        String odgovorKorisniku = "";
        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(upit);
            while (rs.next()) {
                JsonObject objektKorisnika = new JsonObject();

                objektKorisnika.addProperty("id", rs.getInt("id"));
                objektKorisnika.addProperty("ime", rs.getString("ime"));
                objektKorisnika.addProperty("prezime", rs.getString("prezime"));
                objektKorisnika.addProperty("korisnicko_ime", rs.getString("korisnicko_ime"));

                poljeKorisnika.add(objektKorisnika);
            }
            stmt.close();
            con.close();

            Gson gson = new Gson();
            odgovorKorisniku += "OK 10; " + gson.toJson(poljeKorisnika);

            posaljiPorukuKorisniku(odgovorKorisniku);

        } catch (SQLException ex) {
            posaljiPorukuKorisniku("ERR 17;");
        }
    }

    /**
     * metoda sluzi kao izborik za odabir metode na temelju komande za
     * posluzitelja
     *
     * @param komanda - prestavlja tocno DODAJ, AZURIRAJ, PAUZA, KRENI, PASIVNO
     * @param naredba - cijela naredba koju šalje korisnik
     */
    public void izaberiMetoduNaTemeljuKomandePosluzitelj(String komanda, String naredba) {
        if ("DODAJ".equals(komanda)) {
            dodajKorisnikaUBazuPodataka(naredba);
        }
        if (autenticirajKorisnikaUBazi(naredba)) {
            if ("AZURIRAJ".equals(komanda)) {
                azurirajKorisnikaUBaziPodataka(naredba);
            }
            if ("PAUZA".equals(komanda)) {
                izvrsiPauzuPosluzitelj();
            }
            if ("KRENI".equals(komanda)) {
                izvrsiPokretanjePosluzitelj();
            }
            if ("PASIVNO".equals(komanda)) {
                izvrsiPasivnoPosluzitelj();
            }
            zapisiPodatkeUDnevnikRada(naredba);
            posaljiEmailPoruku(naredba);

        } else {
            posaljiPorukuKorisniku("ERR 11; Ne postoji korisnik ili ne odgovara lozinka");
        }
    }

    /**
     * metoda sluzi kao izborik za odabir metode na temelju komande za
     * posluzitelja
     *
     * @param komanda - prestavlja tocno AKTIVNO, STANI, STANJE, LISTAJ
     * @param naredba - cijela naredba koju šalje korisnik
     */
    public void izaberiMetoduNaTemeljuKomandePosluzitelj_v2(String komanda, String naredba) {
        if (autenticirajKorisnikaUBazi(naredba)) {
            if ("AKTIVNO".equals(komanda)) {
                izvrsiAktivnoPosluzitelj();
            }
            if ("STANI".equals(komanda)) {
                izvrsiZaustavljanjePosluzitelj();
            }
            if ("STANJE".equals(komanda)) {
                izvrsiStanjePosluzitelj();
            }
            if ("LISTAJ".equals(komanda)) {
                izvrsiListanjePosluzitelj();
            }
            zapisiPodatkeUDnevnikRada(naredba);
            posaljiEmailPoruku(naredba);

        } else {
            posaljiPorukuKorisniku("ERR 11; Ne postoji korisnik ili ne odgovara lozinka");
        }
    }

    /**
     * metoda sluzi kao izborik za odabir metode na temelju komande za grupe
     *
     * @param komanda - prestavlja tocno DODAJ, PREKID, KRENI, PAUZA, STANJE
     * @param naredba - cijela naredba koju šalje korisnik
     */
    public void izaberiMetoduNaTemeljuKomandeGrupa(String komanda, String naredba) {
        if (autenticirajKorisnikaUBazi(naredba)) {
            Konfiguracija konfiguracija = (Konfiguracija) kontekst.getAttribute("Posluzitelj_Konfig");
            String korisnickoIme = konfiguracija.dajPostavku("korisnik.grupa");
            String lozinka = konfiguracija.dajPostavku("korisnik.lozinka");

            if (Posluzitelj.parametarPauza == false) {
                if (komanda.equals("DODAJ")) {
                    registrirajGrupu(korisnickoIme, lozinka);
                }
                if (komanda.equals("PREKID")) {
                    deregistrirajGrupu(korisnickoIme, lozinka);
                }
                if (komanda.equals("KRENI")) {
                    aktivirajGrupu(korisnickoIme, lozinka);
                }
                if (komanda.equals("PAUZA")) {
                    blokirajGrupu(korisnickoIme, lozinka);
                }
                if (komanda.equals("STANJE")) {
                    dajStanjeGrupe(korisnickoIme, lozinka);
                }
                zapisiPodatkeUDnevnikRada(naredba);
            }else{
                posaljiPorukuKorisniku("ERR 50; Ne izvrsavaj naredbe za grupu");
            }

        } else {
            posaljiPorukuKorisniku("ERR 11; Ne postoji korisnik ili ne odgovara lozinka");
        }
    }

    /**
     * metoda za registriranje grupe i naredbu DODAJ
     *
     * @param korisnickoIme
     * @param lozinka
     */
    public void registrirajGrupu(String korisnickoIme, String lozinka) {
        if (ParkiranjeWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            if (ParkiranjeWS.registrirajGrupu(korisnickoIme, lozinka)) {
                posaljiPorukuKorisniku("OK 20; Grupa je registrirana");
            } else {
                posaljiPorukuKorisniku("ERR 20; Grupa je već registrirana");
            }
        }
    }

    /**
     * metoda za deregistriranje grupe i naredbu PREKID
     *
     * @param korisnickoIme
     * @param lozinka
     */
    public void deregistrirajGrupu(String korisnickoIme, String lozinka) {
        if (ParkiranjeWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            String status = ParkiranjeWS.dajStatusGrupe(korisnickoIme, lozinka).value();
            if (ParkiranjeWS.deregistrirajGrupu(korisnickoIme, lozinka)) {
                posaljiPorukuKorisniku("OK 20; Grupa je deregistrirana");
            } else {
                posaljiPorukuKorisniku("ERR 21; Grupa nije bila registrirana");
            }
        }
    }

    /**
     * metoda za aktiviranje grupe i naredbu KRENI
     *
     * @param korisnickoIme
     * @param lozinka
     */
    public void aktivirajGrupu(String korisnickoIme, String lozinka) {
        if (ParkiranjeWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            String status = ParkiranjeWS.dajStatusGrupe(korisnickoIme, lozinka).value();
            if (status.equals("REGISTRIRAN") || status.equals("BLOKIRAN")) {
                if (ParkiranjeWS.aktivirajGrupu(korisnickoIme, lozinka)) {
                    posaljiPorukuKorisniku("OK 20; Grupa je aktivirana");
                }
            } else if (status.equals("AKTIVAN")) {
                if (!ParkiranjeWS.aktivirajGrupu(korisnickoIme, lozinka)) {
                    posaljiPorukuKorisniku("ERR 22; Grupa je već aktivirana");
                }
            } else if (status.equals("DEREGISTRIRAN")) {
                posaljiPorukuKorisniku("ERR 21; Grupa ne postoji");
            }
        }
    }

    /**
     * metoda za blokiranje grupe i naredbu PAUZA
     *
     * @param korisnickoIme
     * @param lozinka
     */
    public void blokirajGrupu(String korisnickoIme, String lozinka) {
        if (ParkiranjeWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            String status = ParkiranjeWS.dajStatusGrupe(korisnickoIme, lozinka).value();
            if (status.equals("AKTIVAN")) {
                if (ParkiranjeWS.blokirajGrupu(korisnickoIme, lozinka)) {
                    posaljiPorukuKorisniku("OK 20; Grupa je blokirana");
                }
            } else if (status.equals("BLOKIRAN")) {
                if (!ParkiranjeWS.blokirajGrupu(korisnickoIme, lozinka)) {
                    posaljiPorukuKorisniku("ERR 23; Grupa je već blokirana");
                }
            } else if (status.equals("DEREGISTRIRAN")) {
                posaljiPorukuKorisniku("ERR 21; Grupa ne postoji");
            }
        }
    }

    /**
     * metoda za provjeru statusa grupe i naredbu STANJE
     *
     * @param korisnickoIme
     * @param lozinka
     */
    public void dajStanjeGrupe(String korisnickoIme, String lozinka) {
        String statusWS = ParkiranjeWS.dajStatusGrupe(korisnickoIme, lozinka).value();

        // dijagram - 1. AUTENTIKACIJA
        if (ParkiranjeWS.autenticirajGrupu(korisnickoIme, lozinka)) {
            if (statusWS.equals("AKTIVAN")) {
                posaljiPorukuKorisniku("OK 21; Grupa je aktivna");
            }
            if (statusWS.equals("BLOKIRAN")) {
                posaljiPorukuKorisniku("OK 22; Grupa je blokirana");
            }
            if (statusWS.equals("DEREGISTRIRAN") || statusWS.equals("PASIVAN")) {
                // obrisi sva parkiralista servisa i ponovno ucitaj iz baze i stavi na servis
                dohvatiSvaParkiralistaIPostaviNaServis(korisnickoIme, lozinka);
                posaljiPorukuKorisniku("ERR 21; Grupa ne postoji");
            }
            if (statusWS.equals("REGISTRIRAN")) {
                posaljiPorukuKorisniku("OK 20; Grupa je registrirana");
            }
        } else //neispravni podaci korisnika
        {
            posaljiPorukuKorisniku("ERR 11; Ne postoji korisnik ili ne odgovara lozinka");
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
            System.out.println("GRESKA: greska kod dohvacanja korisnika iz baze i postavljanja na servis ParkiranjeWS");
        }

    }

    /**
     * metoda za slanje Email poruke na temelju dobivene naredbe
     *
     * @param naredba
     */
    public void posaljiEmailPoruku(String naredba) {
        SlanjeEmailPoruke poruka = new SlanjeEmailPoruke(kontekst, naredba);
        poruka.saljiPoruku();
    }

    /**
     * metoda izvlaci i vraca naredbu ZA POSLUZITELJA/GRUPU ovisno o parametru
     * (0-posluzitelj, 1-grupa) na temelju komande koju je potrebno odraditi -
     * DODAJ, KRENI, PAUZA...
     *
     * @param naredba
     * @param parametar
     * @return
     */
    public String dohvatiNaredbuIzKomande(String naredba, int parametar) {
        String[] nizZaParsiranje = naredba.split(";");
        String[] podaciKomande = nizZaParsiranje[2].trim().split(" ");

        String naredbaZaIzvrsenje = podaciKomande[parametar];
        return naredbaZaIzvrsenje;
    }

    /**
     * metoda parsira dobivenu naredbu i dohvaca podatke o korisniku koje vraca
     * u obliku niza
     *
     * @param naredba
     * @return
     */
    public String[] dohvatiKorisnickoImeILozinkuIzKomande(String naredba) {
        String[] podaciKorisnika = new String[2];
        String[] nizZaParsiranje = naredba.split(";");

        String[] korisnickoIme = nizZaParsiranje[0].split(" ");
        podaciKorisnika[0] = korisnickoIme[1];
        String[] korisnickaLozinka = nizZaParsiranje[1].trim().split(" ");
        podaciKorisnika[1] = korisnickaLozinka[1];

        return podaciKorisnika;
    }

    /**
     * metoda radi autentikaciju korisnika u bazi podataka na temelju dobivene
     * naredbe
     *
     * @return
     */
    public boolean autenticirajKorisnikaUBazi(String naredba) {
        String[] korisnickiPodaci = dohvatiKorisnickoImeILozinkuIzKomande(naredba);
        String korisnickoIme = korisnickiPodaci[0];
        String lozinka = korisnickiPodaci[1];

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
            System.out.println("GRESKA: kod izvrsavanja provjere korisnika u bazi - RadnaDretva - " + ex.getMessage());
        }
        return false;
    }

    /**
     * metoda za slanje poruke korisniku na output stream koja prima tekst
     * poruke za slanje
     *
     * @param poruka
     */
    public void posaljiPorukuKorisniku(String poruka) {

        OutputStream os = null;
        try {
            os = socket.getOutputStream();
            os.write(poruka.getBytes());
            os.flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            System.out.println("Greska kod kreiranja outputStreama - Radna dretva - " + ex.getMessage());
        } finally {
            try {
                os.close();
            } catch (IOException ex) {
                System.out.println("Greska kod zatvaranja outputStreama - Radna dretva - " + ex.getMessage());
            }
        }
    }

    /**
     * metoda sluzi za zapis podataka u dnevnik rada
     *
     * @param naredba
     */
    private void zapisiPodatkeUDnevnikRada(String naredba) {
        String[] podaciKorisnika = dohvatiKorisnickoImeILozinkuIzKomande(naredba);
        String korisnickoIme = podaciKorisnika[0];

        long trajanje = System.currentTimeMillis() - pocetakObrade;
        String ipAdresa = socket.getInetAddress().toString().replace("/", "");
        String url = socket.getInetAddress().getCanonicalHostName();

        String upit = "INSERT INTO dnevnik (url, ip_adresa, trajanje, korisnicko_ime, sadrzaj, vrsta) VALUES "
                + "('" + url + "','" + ipAdresa + "'," + trajanje + ",'" + korisnickoIme + "','" + naredba + "','SOCKET')";

        try {
            Connection con = postaviVezuNaBazu();
            Statement stmt = con.createStatement();
            stmt.execute(upit);
            stmt.close();
            con.close();
            System.out.println("Podaci zapisani u dnevnik rada");

        } catch (SQLException ex) {
            System.out.println("GRESKA: kod dodavanja novog zapisa u dnevnik - RadnaDretva - " + ex.getMessage());
        }
    }

}
