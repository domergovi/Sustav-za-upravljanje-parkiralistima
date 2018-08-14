/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.ejb.sb;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import org.foi.nwtis.domergovi.web.podaci.JMSPoruka;

/**
 *
 * @author Domagoj
 */
@Singleton
@LocalBean
public class SpremanjeJMS {
    
    

    /**
     * lista za spremanje JMS poruka iz ObradaEmailDrivenBeana
     */
    public List<JMSPoruka> listaJMSPoruka;

    //TODO metoda za serijalizaciju JMS poruka iz liste u datoteku
    //TODO metoda za deserijalizaciju JMS poruka iz datoteke i učitavanje u listu
    
    public SpremanjeJMS() {
        listaJMSPoruka = new ArrayList();
    }
    
    /**
     * metoda za dohvacanje poruke i spremanje u listu
     * @param poruka 
     */
    public void dohvatiJMSPorukuISpremiUListu(JMSPoruka poruka){
        listaJMSPoruka.add(poruka);
    }
    
    public List<JMSPoruka> getListaJMSPoruka() {
        return listaJMSPoruka;
    }

    public void setListaJMSPoruka(List<JMSPoruka> listaJMSPoruka) {
        this.listaJMSPoruka = listaJMSPoruka;
    }
    
    public void obrisiPodatkeListe(){
        listaJMSPoruka.clear();
    }
}
