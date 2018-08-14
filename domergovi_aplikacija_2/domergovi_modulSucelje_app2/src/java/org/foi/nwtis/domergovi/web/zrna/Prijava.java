/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import java.io.IOException;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.domergovi.ejb.sb.StatefulMQTTZrno;
import org.foi.nwtis.domergovi.rest.klijenti.KorisniciRESTAutentikacija;

/**
 *
 * @author Domagoj
 */
@Named(value = "prijava")
@SessionScoped
public class Prijava implements Serializable {

    @EJB
    private StatefulMQTTZrno statefulMQTTZrno;
    
    /**
     * varijabla za provjeru jel se korisnik prijavio u sustav
     */
    private boolean korisnikPrijavljen = true;
    
    /**
     * varijabla za preuzimanje korisnickog imena sa obrasca prijave
     */
    private String korisnickoIme;
    
    /**
     * varijabla za preuzimanje korisnickog imena sa obrasca prijave
     */
    private String lozinka;
    
    /**
     * varijabla za prikaz poruke
     */
    private String porukaZaIspis;
    
    /**
     * varijabla tipa ResourceBundle za rad s prijevodom
     */
    private ResourceBundle prijevod;
    
    /**
     * varijabla za rad sa sesijom
     */
    private HttpSession sesija;
    /**
     * Creates a new instance of Prijava
     */
    public Prijava() {
    }
    
    /**
     * metoda sluzi za autentikaciju korinsika na temelju REST metode iz aplikacije 3 
     */
    public void prijaviKorisnikaUSustav(){
        dohvatiJezikIVratiObjektPrijevoda();
        
        if (!korisnickoIme.isEmpty() && !lozinka.isEmpty()){
            
            if (statefulMQTTZrno.autenticirajKorisnika(korisnickoIme, lozinka)){
                try {
                    korisnikPrijavljen = false;
                    sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(true);
                    sesija.setAttribute("korisnickoIme", korisnickoIme);
                    sesija.setAttribute("lozinka", lozinka);
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                            prijevod.getString("index.informacija"), prijevod.getString("index.poruka_PrijavljeniSte")));
                    FacesContext.getCurrentInstance().getExternalContext().redirect("index.xhtml");
                } catch (IOException ex) {
                    System.out.println("GRESKA: Problem kod redirektanja - Prijava");
                }
            }
            else{
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaPrijava")));
            }
        }else
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                        prijevod.getString("index.greska"), prijevod.getString("index.poruka_PostavljeniSviElementi")));
    }
    
    /**
     * dohvaća ttrenutni jezik i postavlja varijablu prijevod na putanju
     */
    public void dohvatiJezikIVratiObjektPrijevoda(){
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        prijevod = ResourceBundle.getBundle("org.foi.nwtis.domergovi.prijevod",locale);
    }

    /**
     * metoda za dohvaćanje vrijednosti varijable porukaZaIspis 
     * @return 
     */
    public String getPorukaZaIspis() {
        return porukaZaIspis;
    }

    /**
     * metoda za postavljanje vrijednosti varijable porukaZaIspis
     * @param porukaZaIspis 
     */
    public void setPorukaZaIspis(String porukaZaIspis) {
        this.porukaZaIspis = porukaZaIspis;
    }
    
    
    /**
     * metoda za dohvacanje vrijednosti varijable korisnikPrijavljen
     * @return 
     */
    public boolean isKorisnikPrijavljen() {
        return korisnikPrijavljen;
    }

    /**
     * metoda za postavljanje vrijednosti varijable korisnikPrijavljen
     * @param korisnikPrijavljen 
     */
    public void setKorisnikPrijavljen(boolean korisnikPrijavljen) {
        this.korisnikPrijavljen = korisnikPrijavljen;
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
