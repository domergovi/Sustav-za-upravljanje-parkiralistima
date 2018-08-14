package org.foi.nwtis.domergovi.web.podaci;

import java.util.Date;


public class Poruka {
    
    private String id;
    private Date vrijemeSlanja;
    private Date vrijemePrijema;
    private String salje;
    private String predmet;
    private String privitak;

    /**
     * konstruktor klase Poruka
     * @param id string
     * @param vrijemeSlanja date
     * @param vrijemePrijema date
     * @param salje string
     * @param predmet string
     * @param privitak string
     * @param vrsta VrstaPoruka
     */
    public Poruka(String id, Date vrijemeSlanja, Date vrijemePrijema, String salje, String predmet, String privitak) {
        this.id = id;
        this.vrijemeSlanja = vrijemeSlanja;
        this.vrijemePrijema = vrijemePrijema;
        this.salje = salje;
        this.predmet = predmet;
        this.privitak = privitak;
    }

    /**
     * dohvacanje id-a
     * @return id tipa string
     */
    public String getId() {
        return id;
    }

    /**
     * dohvacanje vremena slanja
     * @return vrijemeSlanja tipa date
     */
    public Date getVrijemeSlanja() {
        return vrijemeSlanja;
    }

    /**
     * dohvacanje vremena prijema
     * @return parametar tipa date
     */
    public Date getVrijemePrijema() {
        return vrijemePrijema;
    }

    /**
     * dohvaca predmet poruke
     * @return parametar tipa string
     */
    public String getPredmet() {
        return predmet;
    }

    /**
     * dohvaca email adresu korisnika koji salje poruku
     * @return parametar tipa string
     */
    public String getSalje() {
        return salje;
    }


    /**
     * dohvacanje privitka
     * @return parametar tipa string
     */
    public String getPrivitak() {
        return privitak;
    }

}
