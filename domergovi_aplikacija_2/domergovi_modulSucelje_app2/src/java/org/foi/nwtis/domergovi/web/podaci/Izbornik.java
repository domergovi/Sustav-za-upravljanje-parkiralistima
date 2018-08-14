package org.foi.nwtis.domergovi.web.podaci;


public class Izbornik {
    private String labela;
    private String vrijednost;

    /**
     * konstruktor klase Izbornik
     * @param labela parametar tipa string
     * @param vrijednost parametar tipa string
     */
    public Izbornik(String labela, String vrijednost) {
        this.labela = labela;
        this.vrijednost = vrijednost;
    }

    /**
     * dohvacanje labele
     * @return
     */
    public String getLabela() {
        return labela;
    }

    /**
     * postavljanje labele
     * @param labela
     */
    public void setLabela(String labela) {
        this.labela = labela;
    }

    /**
     * dohvacanje vrijednosti
     * @return
     */
    public String getVrijednost() {
        return vrijednost;
    }

    /**
     * postavljanje vrijednosti
     * @param vrijednost
     */
    public void setVrijednost(String vrijednost) {
        this.vrijednost = vrijednost;
    }        
}
