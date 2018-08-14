/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.podaci;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Domagoj
 */
public class JMSPoruka implements Serializable{
    
    private int redniBrojJMSPoruke;
    private Date vrijemeSlanjaPrethodneJmsPoruke;
    private Date vrijemeSlanjaTrenutneJMSPoruke;
    private long vrijemeRadaIteracijeDretve;
    private int brojNWTISPoruka;

    public JMSPoruka(int redniBrojJMSPoruke, Date vrijemeSlanjaPrethodneJmsPoruke, Date vrijemeSlanjaTrenutneJMSPoruke, long vrijemeRadaIteracijeDretve, int brojNWTISPoruka) {
        this.redniBrojJMSPoruke = redniBrojJMSPoruke;
        this.vrijemeSlanjaPrethodneJmsPoruke = vrijemeSlanjaPrethodneJmsPoruke;
        this.vrijemeSlanjaTrenutneJMSPoruke = vrijemeSlanjaTrenutneJMSPoruke;
        this.vrijemeRadaIteracijeDretve = vrijemeRadaIteracijeDretve;
        this.brojNWTISPoruka = brojNWTISPoruka;
    }

    public int getRedniBrojJMSPoruke() {
        return redniBrojJMSPoruke;
    }

    public void setRedniBrojJMSPoruke(int redniBrojJMSPoruke) {
        this.redniBrojJMSPoruke = redniBrojJMSPoruke;
    }

    public Date getVrijemeSlanjaPrethodneJmsPoruke() {
        return vrijemeSlanjaPrethodneJmsPoruke;
    }

    public void setVrijemeSlanjaPrethodneJmsPoruke(Date vrijemeSlanjaPrethodneJmsPoruke) {
        this.vrijemeSlanjaPrethodneJmsPoruke = vrijemeSlanjaPrethodneJmsPoruke;
    }

    public Date getVrijemeSlanjaTrenutneJMSPoruke() {
        return vrijemeSlanjaTrenutneJMSPoruke;
    }

    public void setVrijemeSlanjaTrenutneJMSPoruke(Date vrijemeSlanjaTrenutneJMSPoruke) {
        this.vrijemeSlanjaTrenutneJMSPoruke = vrijemeSlanjaTrenutneJMSPoruke;
    }

    public long getVrijemeRadaIteracijeDretve() {
        return vrijemeRadaIteracijeDretve;
    }

    public void setVrijemeRadaIteracijeDretve(long vrijemeRadaIteracijeDretve) {
        this.vrijemeRadaIteracijeDretve = vrijemeRadaIteracijeDretve;
    }

    public int getBrojNWTISPoruka() {
        return brojNWTISPoruka;
    }

    public void setBrojNWTISPoruka(int brojNWTISPoruka) {
        this.brojNWTISPoruka = brojNWTISPoruka;
    }
    
    
}
