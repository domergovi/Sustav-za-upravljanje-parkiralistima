/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Locale;
import javax.faces.context.FacesContext;

/**
 *
 * @author Domagoj
 */
@Named(value = "lokalizacija")
@SessionScoped
public class Lokalizacija implements Serializable {
    
    private String odabraniJezik;
    private Locale locale;

    /**
     * Creates a new instance of Lokalizacija
     */
    public Lokalizacija() {
         locale = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
    }

    public String getOdabraniJezik() {
        odabraniJezik = FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage();
        return odabraniJezik;
    }

    public void setOdabraniJezik(String odabraniJezik) {
        this.odabraniJezik = odabraniJezik;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    
    public Object odaberiJezik(String jezik){
        locale = new Locale(jezik);
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
        return "";
    }
    
    public String prijava(){
        return "prijava";
    }
    
    public String registracija(){
        return "registracija";
    }
    
}
