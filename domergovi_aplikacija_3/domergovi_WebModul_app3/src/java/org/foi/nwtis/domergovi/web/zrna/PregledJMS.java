/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import org.foi.nwtis.domergovi.ejb.sb.SpremanjeJMS;
import org.foi.nwtis.domergovi.web.podaci.JMSPoruka;

/**
 *
 * @author Domagoj
 */
@Named(value = "pregledJMS")
@RequestScoped
public class PregledJMS {

    /**
     * EJB za pristup listi JMS poruka
     */
    @EJB
    private SpremanjeJMS spremanjeJMS;

    private List<JMSPoruka> listaJMSPoruka;

    /**
     * Creates a new instance of PregledJMS
     */
    public PregledJMS() {
    }

    /**
     * metoda za dohvacanje liste poruka iz klase SpremanjeJMS
     */
    public void dohvatiJMSPoruke() {
        listaJMSPoruka = spremanjeJMS.getListaJMSPoruka();
    }

    /**
     * metoda za brisanje liste JMS poruka
     */
    public void obrisiSveJMSPoruke() {
        spremanjeJMS.obrisiPodatkeListe();
    }

    /**
     * metoda za dohvacanje liste listaJMSPoruka
     * @return 
     */
    public List<JMSPoruka> getListaJMSPoruka() {
        return listaJMSPoruka;
    }

    /**
     * metoda za postavljanje liste listaJMSPoruka
     * @param listaJMSPoruka 
     */
    public void setListaJMSPoruka(List<JMSPoruka> listaJMSPoruka) {
        this.listaJMSPoruka = listaJMSPoruka;
    }
}
