/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.domergovi.konfiguracije.Konfiguracija;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@Named(value = "pregledStatusa")
@RequestScoped
public class PregledStatusa {

    /**
     * varijabla za preuzimanje vrijednosti socketa
     */
    private Socket socket;
    /**
     * varijabla tipa ResourceBundle za rad s prijevodom
     */
    private ResourceBundle prijevod;
    /**
     * varijabla za rad sa sesijom
     */
    private HttpSession sesija;
    
    /**
     * parametar za provjeru jel server u stanju pauze, onda ne prima naredbe grupe
     */
    private boolean parametarPauza = false;

    /**
     * varijabla za prikaz stanja posluzitelja
     */
    private String stanjeGrupe;

    /**
     * varijabla za provjeru tipke registriraj grupu na obrascu
     */
    private boolean tipkaRegistriraj;
    /**
     * varijabla za provjeru tipke deregistriraj grupu na obrascu
     */
    private boolean tipkaDeregistriraj;
    /**
     * varijabla za provjeru tipke blokiraj grupu na obrascu
     */
    private boolean tipkaBlokiraj;
    /**
     * varijabla za provjeru tipke aktiviraj grupu na obrascu
     */
    private boolean tipkaAktiviraj;


    /**
     * Creates a new instance of PregledStatusa
     */
    public PregledStatusa() {
        sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        provjeriStanjeGrupe();
    }

    /**
     * metoda na temelju dobivenog odgovora servera vraća stanje odgovora u
     * obliku informacije na obrascu
     */
    public void provjeriStanjePosluzitelja() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; STANJE;");

        if (odgovorServera.contains("OK 11;")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljStanje11")));
        }
        if (odgovorServera.contains("OK 12;")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljStanje12")));
        }
        if (odgovorServera.contains("OK 13;")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljStanje13")));
        }
        if (odgovorServera.contains("OK 14;")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljStanje14")));
        }
        provjeriStanjeGrupe();

    }

    /**
     * metoda preko socketa poziva komandu PAUZA i prikazuje poruku u obrascu
     * PregledStatusa
     */
    public void pozoviKomanduPauzaPosluzitelj() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; PAUZA;");

        if (odgovorServera.contains("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljPauzaOK")));
            provjeriStanjeGrupe();
        }
        if (odgovorServera.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.posluziteljPauzaERR")));
        }
    }

    /**
     * metoda preko socketa poziva komandu KRENI i prikazuje poruku u obrascu
     * PregledStatusa
     */
    public void pozoviKomanduKreniPosluzitelj() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; KRENI;");

        if (odgovorServera.contains("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljKreniOK")));
            provjeriStanjeGrupe();
        }
        if (odgovorServera.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.posluziteljKreniERR")));
        }
    }

    /**
     * metoda preko socketa poziva komandu PASIVNO i prikazuje poruku u obrascu
     * PregledStatusa
     */
    public void pozoviKomanduPasivnoPosluzitelj() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; PASIVNO;");

        if (odgovorServera.contains("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljPasivnoOK")));
        }
        if (odgovorServera.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.posluziteljPasivnoERR")));
        }
    }

    /**
     * metoda preko socketa poziva komandu AKTIVNO i prikazuje poruku u obrascu
     * PregledStatusa
     */
    public void pozoviKomanduAktivnoPosluzitelj() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; AKTIVNO;");

        if (odgovorServera.contains("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljAktivnoOK")));
        }
        if (odgovorServera.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.posluziteljAktivnoERR")));
        }
    }

    /**
     * metoda preko socketa poziva komandu STANI i prikazuje poruku u obrascu
     * PregledStatusa
     */
    public void pozoviKomanduStaniPosluzitelj() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; STANI;");

        if (odgovorServera.contains("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.posluziteljStaniOK")));
        }
        if (odgovorServera.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.posluziteljStaniERR")));
        }
    }

    /**
     * metoda dohvaća trenutni status grupe preko komande GRUPA STANJE i ovisno o tome vraća odgovor i blokira/deblokira tipke
     */
    public void provjeriStanjeGrupe() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; GRUPA STANJE;");

        if (odgovorServera.contains("OK 22;")) {
            stanjeGrupe = prijevod.getString("index.grupaBlokirana");
            tipkaAktiviraj = true;
            tipkaDeregistriraj = true;
            tipkaRegistriraj = false;
            tipkaBlokiraj = false;
        }
        if (odgovorServera.contains("ERR 21;")) {
            stanjeGrupe = prijevod.getString("index.grupaNePostoji");
            tipkaAktiviraj = false;
            tipkaDeregistriraj = false;
            tipkaRegistriraj = true;
            tipkaBlokiraj = false;
        }
        if (odgovorServera.contains("OK 21;")) {
            stanjeGrupe = prijevod.getString("index.grupaAktivna");
            tipkaAktiviraj = false;
            tipkaDeregistriraj = false;
            tipkaRegistriraj = false;
            tipkaBlokiraj = true;
        }
        if (odgovorServera.contains("OK 20;")) {
            stanjeGrupe = prijevod.getString("index.grupaRegistrirana");
            tipkaAktiviraj = true;
            tipkaDeregistriraj = true;
            tipkaRegistriraj = false;
            tipkaBlokiraj = false;
        }
        if (odgovorServera.contains("ERR 50;")) {
            tipkaAktiviraj = false;
            tipkaDeregistriraj = false;
            tipkaRegistriraj = false;
            tipkaBlokiraj = false;
        }
    }
    
    /**
     * metoda za poziv metode REGISTRIRAJ GRUPU preko socketa iz radne dretve
     */
    public void pozoviKomanduRegistrirajGrupu() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; GRUPA DODAJ;");
        
        provjeriStanjeGrupe();
    }
    
    /**
     * metoda za poziv metode DEREGISTRIRAJ GRUPU preko socketa iz radne dretve
     */
    public void pozoviKomanduDeregistrirajGrupu() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; GRUPA PREKID;");

        provjeriStanjeGrupe();
    }
    
    /**
     * metoda za poziv metode AKTIVIRAJ GRUPU preko socketa iz radne dretve
     */
    public void pozoviKomanduAktivirajGrupu() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; GRUPA KRENI;");

        provjeriStanjeGrupe();
    }
    
    /**
     * metoda za poziv metode BLOKIRAJ GRUPU preko socketa iz radne dretve
     */
    public void pozoviKomanduBlokirajGrupu() {
        dohvatiJezikIVratiObjektPrijevoda();
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + String.valueOf(sesija.getAttribute("korisnickoIme"))
                + "; LOZINKA " + String.valueOf(sesija.getAttribute("lozinka")) + "; GRUPA PAUZA;");

        provjeriStanjeGrupe();
    }
    
    

    /**
     * dohvaća trenutni jezik i postavlja varijablu prijevod na putanju
     */
    public void dohvatiJezikIVratiObjektPrijevoda() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        prijevod = ResourceBundle.getBundle("org.foi.nwtis.domergovi.prijevod", locale);
    }

    /**
     * metoda za slanje podataka na socket
     *
     * @param poruka prvi argument - tekst poruke
     * @param socket drugi argument - socket
     */
    private void posaljiPoruku(String poruka) {

        try {
            OutputStream os = socket.getOutputStream();

            os.write(poruka.getBytes());
            os.flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod slanja poruke na outputStream - KorisniciREST");
        }
    }

    /**
     * metoda sluzi za uspostavljanje veze sa socketom i prosljeđivanje naredbe
     * na njega te preuzimanje odgovora
     *
     * @param naredba
     * @return
     */
    private String uspostaviVezuSaSocketomICekajOdgovor(String naredba) {
        try {
            Konfiguracija konf = (Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("Posluzitelj_Konfig");
            int port = Integer.valueOf(konf.dajPostavku("port"));
            socket = new Socket("127.0.0.1", port);
            posaljiPoruku(naredba);
            String odgovorServera = dohvatiPorukuServera();
            return odgovorServera;
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod rada sa socketom - PregledStatusa");
        }
        return "";
    }

    /**
     * metoda dohvaća komandu preko socketa (inputstream-a)
     *
     * @return vraćena vrijednost naredbe
     * @throws IOException u slucaju greske
     */
    private String dohvatiPorukuServera() throws IOException {

        InputStream is = socket.getInputStream();

        int znak;
        StringBuffer buffer = new StringBuffer();
        while (true) {
            znak = is.read();
            if (znak == -1) {
                break;
            }
            buffer.append((char) znak);
        }

        String naredba = buffer.toString();

        socket.shutdownInput();
        return naredba;
    }

    public String getStanjeGrupe() {
        return stanjeGrupe;
    }

    public void setStanjeGrupe(String stanjeGrupe) {
        this.stanjeGrupe = stanjeGrupe;
    }

    public boolean isTipkaRegistriraj() {
        return tipkaRegistriraj;
    }

    public void setTipkaRegistriraj(boolean tipkaRegistriraj) {
        this.tipkaRegistriraj = tipkaRegistriraj;
    }

    public boolean isTipkaDeregistriraj() {
        return tipkaDeregistriraj;
    }

    public void setTipkaDeregistriraj(boolean tipkaDeregistriraj) {
        this.tipkaDeregistriraj = tipkaDeregistriraj;
    }

    public boolean isTipkaBlokiraj() {
        return tipkaBlokiraj;
    }

    public void setTipkaBlokiraj(boolean tipkaBlokiraj) {
        this.tipkaBlokiraj = tipkaBlokiraj;
    }

    public boolean isTipkaAktiviraj() {
        return tipkaAktiviraj;
    }

    public void setTipkaAktiviraj(boolean tipkaAktiviraj) {
        this.tipkaAktiviraj = tipkaAktiviraj;
    }

}
