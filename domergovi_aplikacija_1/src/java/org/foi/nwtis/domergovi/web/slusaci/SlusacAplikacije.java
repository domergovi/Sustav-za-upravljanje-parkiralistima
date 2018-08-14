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
import org.foi.nwtis.domergovi.posluzitelj.Posluzitelj;
import org.foi.nwtis.domergovi.web.dretve.PreuzmiMeteoPodatke;


@WebListener
public class SlusacAplikacije implements ServletContextListener {
    
    /**
     * varijabla za preuzimanje konteksta
     */
    private static ServletContext kontekst;
    /**
     * objekt dretve PreuzmiMeteoPodatke
     */
    PreuzmiMeteoPodatke dretvaMeteo;
    
    /**
     * instanca klase posluzitelj
     */
    private Posluzitelj posluzitelj;

    /**
     * metoda za inicijalizaciju konteksta te pokretanje rada dretve PreuzmiMeteoPodatke
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
        
        
        // postavljanje konfiguracijske datoteke (Posluzitelj) u kontekst
        try {
            String putanjaPosluziteljKonf = kontekst.getRealPath("/WEB-INF/NWTIS_konfiguracijaPosluzitelj.txt");
            Konfiguracija konfiguracijaPosluzitelj = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanjaPosluziteljKonf);
            kontekst.setAttribute("Posluzitelj_Konfig", konfiguracijaPosluzitelj);
            
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            Logger.getLogger(SlusacAplikacije.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        dretvaMeteo = new PreuzmiMeteoPodatke();
        dretvaMeteo.start();
        // instanciranje posluzitelja i prosljeđivanje podataka o kontekstu i dretvi za meteo podatke, pokretanje posluzitelja
        posluzitelj = new Posluzitelj(kontekst,dretvaMeteo);
        posluzitelj.start();
    }
    
    /**
     * metoda za uništavanje konteksta te prekid rada dretve i gašenje socketa
     * @param sce 
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
        sc.removeAttribute("BP_Konfig");
        sc.removeAttribute("Posluzitelj_Konfig");
        posluzitelj.ugasiSocketIPrekiniPosluzitelj();
    }

    /**
     * metoda za vracanje konteksta
     * @return 
     */
    public static ServletContext getKontekst() {
        return kontekst;
    }

}
