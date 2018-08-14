/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.ejb.EJB;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.domergovi.ejb.eb.Dnevnik;
import org.foi.nwtis.domergovi.ejb.sb.DnevnikFacade;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@Named(value = "pregledDnevnika")
@SessionScoped
public class PregledDnevnika implements Serializable {

    @EJB
    private DnevnikFacade dnevnikFacade;
    
    /**
     * varijabla tipa ResourceBundle za rad s prijevodom
     */
    private ResourceBundle prijevod;
    /**
     * varijabla za rad sa sesijom
     */
    private HttpSession sesija;
    /**
     * varijabla za broj redova za prikaz u tablici učitan iz konfiguracije
     */
    private int brojRedovaZaPrikaz;
    
    /**
     * lista za preuzimanje podataka iz dnevnika putem Criteria API
     */
    private List<Dnevnik> listaPodatakaDnevnika;
    
    /**
     * Creates a new instance of PregledDnevnika
     */
    public PregledDnevnika() {
        sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        brojRedovaZaPrikaz = Integer.valueOf(bpk.getTableNumRowsToShow());
    }
    
    /**
     * metoda puni listu podacima dnevnika
     */
    public void pozoviMetoduZaPrikazPodatakaDnevnika(){
        dohvatiJezikIVratiObjektPrijevoda();
        listaPodatakaDnevnika = new ArrayList<>();
        listaPodatakaDnevnika = dnevnikFacade.findAll();
    }
    
    /**
     * dohvaća trenutni jezik i postavlja varijablu prijevod na putanju
     */
    public void dohvatiJezikIVratiObjektPrijevoda() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        prijevod = ResourceBundle.getBundle("org.foi.nwtis.domergovi.prijevod", locale);
    }
    
    public List<Dnevnik> getListaPodatakaDnevnika() {
        return listaPodatakaDnevnika;
    }

    public void setListaPodatakaDnevnika(List<Dnevnik> listaPodatakaDnevnika) {
        this.listaPodatakaDnevnika = listaPodatakaDnevnika;
    }

    public int getBrojRedovaZaPrikaz() {
        return brojRedovaZaPrikaz;
    }

    public void setBrojRedovaZaPrikaz(int brojRedovaZaPrikaz) {
        this.brojRedovaZaPrikaz = brojRedovaZaPrikaz;
    }
    
    

}
