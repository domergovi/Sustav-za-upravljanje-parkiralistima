/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.rest.klijenti.KorisniciRESTKlijent;
import org.foi.nwtis.domergovi.web.podaci.Korisnik;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@Named(value = "registracija")
@SessionScoped
public class Registracija implements Serializable {

    /**
     * varijabla za ime iz obrasca
     */
    private String ime;
    /**
     * varijabla za prezime iz obrasca
     */
    private String prezime;
    /**
     * varijabla za korisnicko ime iz obrasca
     */
    private String korisnickoIme;
    /**
     * varijabla za lozinku iz obrasca
     */
    private String lozinka;
    /**
     * varijabla za ponovljenu lozinku iz obrasca
     */
    private String ponovljenaLozinka;
    /**
     * varijabla za email korisnika iz obrasca
     */
    private String email;
    /**
     * varijabla tipa ResourceBundle za rad s prijevodom
     */
    private ResourceBundle prijevod;
    /**
     * varijabla za rad sa sesijom
     */
    private HttpSession sesija;
    /**
     * Creates a new instance of Registracija
     */
    private List<Korisnik> listaKorisnika;
    
    /**
     * varijabla za broj redova za prikaz u tablici učitan iz konfiguracije
     */
    private int brojRedovaZaPrikaz;
    
    public Registracija() {
        sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        brojRedovaZaPrikaz = Integer.valueOf(bpk.getTableNumRowsToShow());
    }

    /**
     * metoda koja ažurira podatke pomoću REST servisa domergovi_aplikacija_3
     */
    public void azurirajPodatkeKorisnika() {
        dohvatiJezikIVratiObjektPrijevoda();
        KorisniciRESTKlijent objektREST = new KorisniciRESTKlijent();

        if (!ime.isEmpty() && !prezime.isEmpty() && !korisnickoIme.isEmpty() && !lozinka.isEmpty()
                && !ponovljenaLozinka.isEmpty() && !email.isEmpty()) {

            if (lozinka.equals(ponovljenaLozinka)) {
                String podaciKorisnika = "{\"ime\":\"" + ime + "\", \"prezime\":\"" + prezime + "\", \"korisnicko_ime\":\""
                        + korisnickoIme + "\", \"lozinka\":\"" + lozinka + "\"}";

                String porukaREST = objektREST.putJson(podaciKorisnika, String.class);

                if (porukaREST.contains("OK")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            prijevod.getString("index.informacija"), prijevod.getString("index.poruka_AzuriraniSte")));
                } else if (porukaREST.contains("ERR")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaAzuriranje")));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaLozinke")));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.poruka_PostavljeniSviElementi")));
        }
    }

    /**
     * dohvaća trenutni jezik i postavlja varijablu prijevod na putanju
     */
    public void dohvatiJezikIVratiObjektPrijevoda() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        prijevod = ResourceBundle.getBundle("org.foi.nwtis.domergovi.prijevod", locale);
    }

    /**
     * metoda koja registrira korisnika pomoću REST servisa
     * domergovi_aplikacija_3
     */
    public void registrirajKorisnika() {
        dohvatiJezikIVratiObjektPrijevoda();
        KorisniciRESTKlijent objektREST = new KorisniciRESTKlijent();

        if (!ime.isEmpty() && !prezime.isEmpty() && !korisnickoIme.isEmpty() && !lozinka.isEmpty()
                && !ponovljenaLozinka.isEmpty() && !email.isEmpty()) {

            if (lozinka.equals(ponovljenaLozinka)) {
                String podaciKorisnika = "{\"ime\":\"" + ime + "\", \"prezime\":\"" + prezime + "\", \"korisnicko_ime\":\""
                        + korisnickoIme + "\", \"lozinka\":\"" + lozinka + "\"}";

                String porukaREST = objektREST.postJson(podaciKorisnika, String.class);

                if (porukaREST.contains("OK")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            prijevod.getString("index.informacija"), prijevod.getString("index.poruka_RegistriraniSte")));
                } else if (porukaREST.contains("ERR")) {
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaRegistracija")));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaLozinke")));
            }
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.poruka_PostavljeniSviElementi")));
        }
    }

    /**
     * metoda dohvaća sve korisnike iz baze podataka preko REST servisa
     * domergovi_aplikacija_3
     */
    public void dohvatiSveKorisnike() {
        dohvatiJezikIVratiObjektPrijevoda();
        KorisniciRESTKlijent objektREST = new KorisniciRESTKlijent();

        String porukaREST = objektREST.getJson(String.class,String.valueOf(sesija.getAttribute("korisnickoIme")), 
                String.valueOf(sesija.getAttribute("lozinka")));
        
        listaKorisnika = new ArrayList<>();
        
        if (porukaREST.contains("OK")) {
                Gson gson = new Gson();
                JsonObject objekt = gson.fromJson(porukaREST, JsonObject.class);
                JsonArray polje = gson.fromJson(objekt.get("odgovor"), JsonArray.class);
                
                for (int i = 0; i < polje.size(); i++){
                    JsonObject objektKorisnik = polje.get(i).getAsJsonObject();
                    Korisnik korisnik = new Korisnik(objektKorisnik.get("id").getAsInt(), objektKorisnik.get("ime").getAsString().replace("\"",""), 
                            objektKorisnik.get("prezime").getAsString().replace("\"",""), 
                            objektKorisnik.get("korisnicko_ime").getAsString().replace("\"",""),"", 0);
                    listaKorisnika.add(korisnik);
                }
        } else if (porukaREST.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaPrikazKorisnika")));
        }
    }

    public int getBrojRedovaZaPrikaz() {
        return brojRedovaZaPrikaz;
    }

    public void setBrojRedovaZaPrikaz(int brojRedovaZaPrikaz) {
        this.brojRedovaZaPrikaz = brojRedovaZaPrikaz;
    }

    public List<Korisnik> getListaKorisnika() {
        return listaKorisnika;
    }

    public void setListaKorisnika(List<Korisnik> listaKorisnika) {
        this.listaKorisnika = listaKorisnika;
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

    public String getKorisnickoIme() {
        return korisnickoIme;
    }

    public void setKorisnickoIme(String korisnickoIme) {
        this.korisnickoIme = korisnickoIme;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getPonovljenaLozinka() {
        return ponovljenaLozinka;
    }

    public void setPonovljenaLozinka(String ponovljenaLozinka) {
        this.ponovljenaLozinka = ponovljenaLozinka;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
