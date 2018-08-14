/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.slusaci;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.foi.nwtis.domergovi.konfiguracije.Konfiguracija;
import org.foi.nwtis.domergovi.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.domergovi.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.domergovi.konfiguracije.NemaKonfiguracije;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;

/**
 *
 * @author Domagoj
 */
@WebListener
public class SlusacAplikacije implements ServletContextListener{

    /**
     * varijabla za kontekst
     */
    private static ServletContext kontekst;
    
    /**
     * metoda za inicijaliziranje konteksta i dohvaćanje konfiguracija
     * @param sce 
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        kontekst = sce.getServletContext();
        
        // postavljanje konfiguracijske datoteke (BP) u kontekst
        String datotekaBP = kontekst.getInitParameter("konfiguracijaBP");
        String putanjaBP = kontekst.getRealPath("/WEB-INF") + java.io.File.separator;
        String puniNazivBP = putanjaBP + datotekaBP;

        BP_Konfiguracija bpk = new BP_Konfiguracija(puniNazivBP);
        kontekst.setAttribute("BP_Konfig", bpk);
        
        try {
            String datotekaPosluzitelj = kontekst.getInitParameter("konfiguracijaPosluzitelj");
            String putanjaPosluziteljKonf = kontekst.getRealPath("/WEB-INF") + java.io.File.separator;
            Konfiguracija konfiguracijaPosluzitelj = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanjaPosluziteljKonf + datotekaPosluzitelj);
            kontekst.setAttribute("Posluzitelj_Konfig", konfiguracijaPosluzitelj);
            
            
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * metoda za uništavanje konteksta i njegovih atributa
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        sc.removeAttribute("Posluzitelj_Konfig");
        sc.removeAttribute("BP_Konfig");
    }

    
    /**
     * metoda za dohvaćanje konteksta
     * @return 
     */
    public static ServletContext getKontekst() {
        return kontekst;
    }
    
    
    
}
