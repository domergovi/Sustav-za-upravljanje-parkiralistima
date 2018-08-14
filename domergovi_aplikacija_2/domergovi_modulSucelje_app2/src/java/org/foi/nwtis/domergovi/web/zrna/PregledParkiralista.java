/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.rest.klijenti.ParkiralistaRESTKlijent;
import org.foi.nwtis.domergovi.rest.klijenti.ParkiralistaRESTKlijentId;
import org.foi.nwtis.domergovi.web.podaci.Lokacija;
import org.foi.nwtis.domergovi.web.podaci.Parkiraliste;
import org.foi.nwtis.domergovi.ws.klijenti.MeteoPodaci;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;
import org.foi.nwtis.domergovi.ws.klijenti.MeteoWSKlijent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 *
 * @author Domagoj
 */
@Named(value = "pregledParkiralista")
@SessionScoped
public class PregledParkiralista implements Serializable {

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
     * varijabla za id parkirališta
     */
    private int id;
    /**
     * varijabla za naziv parkirališta
     */
    private String naziv;
    /**
     * varijabla za adresu parkirališta
     */
    private String adresa;
    /**
     * varijabla za latitudu parkirališta
     */
    private double latitude;
    /**
     * varijabla za longitudu parkirališta
     */
    private double longitude;
    /**
     * varijabla za broj parkirnih mjesta parkirališta
     */
    private int brojParkirnihMjesta;
    /**
     * varijabla za broj ulazno izlaznih mjesta parkirališta
     */
    private int brojUlaznoIzlaznihMjesta;
    /**
     * lista za preuzimanje važećih meteo podataka
     */
    private List<MeteoPodaci> listaMeteoPodataka;
    /**
     * lista za preuzimanje zadnjih meteo podataka
     */
    private List<MeteoPodaci> listaMeteoPodatakaZadnje;
    /**
     * lista za preuzimanje popisa parkiralista
     */
    private List<Parkiraliste> listaParkiralista;

    /**
     * varijabla za preuzimanje odabranog parkiralista iz baze
     */
    private Parkiraliste odabranoParkiraliste = null;

    /**
     * konstruktor klase u kojem se dohvaća postojeća sesija i broj stranica za
     * prikaz u tablici iz konfiguracije
     */
    public PregledParkiralista() {
        sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        brojRedovaZaPrikaz = Integer.valueOf(bpk.getTableNumRowsToShow());
    }

    /**
     * metoda dohvaća popis parkirališta sa REST web servisa
     * domergovi_aplikacija_1
     */
    public void dohvatiSvaParkiralista() {
        dohvatiJezikIVratiObjektPrijevoda();
        ParkiralistaRESTKlijent prk = new ParkiralistaRESTKlijent();
        String porukaREST = prk.getJson(String.class, sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString());

        listaParkiralista = new ArrayList<>();

        if (porukaREST.contains("OK")) {
            Gson gson = new Gson();
            JsonObject objekt = gson.fromJson(porukaREST, JsonObject.class);
            JsonArray polje = gson.fromJson(objekt.get("odgovor"), JsonArray.class);

            for (int i = 0; i < polje.size(); i++) {
                JsonObject objektParkiraliste = polje.get(i).getAsJsonObject();

                JsonObject lokacijaParkiralista = objektParkiraliste.get("geoloc").getAsJsonObject();
                Lokacija lokacija = new Lokacija(lokacijaParkiralista.get("latitude").getAsString(), lokacijaParkiralista.get("longitude").getAsString());
                Parkiraliste parkiraliste = new Parkiraliste(objektParkiraliste.get("id").getAsInt(), objektParkiraliste.get("naziv").getAsString().replace("\"", ""),
                        objektParkiraliste.get("adresa").getAsString().replace("\"", ""), lokacija,
                        objektParkiraliste.get("brojParkirnihMjesta").getAsInt(), objektParkiraliste.get("brojUlaznoIzlaznihMjesta").getAsInt());

                listaParkiralista.add(parkiraliste);
            }
        } else if (porukaREST.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.poruka_GreskaPrikazKorisnika")));
        }
    }

    /**
     * metoda za dodavanje novog parkirališta u bazu podataka za aktivnog
     * korisnika metoda poziva metodu iz REST servisa aplikacije 1 -
     * ParkiralistaREST
     */
    public void dodajNovoParkiraliste() {
        dohvatiJezikIVratiObjektPrijevoda();
        ParkiralistaRESTKlijent prk = new ParkiralistaRESTKlijent();
        String podaciParkiralista = "{\"naziv\": \"" + naziv + "\", \"adresa\": \"" + adresa + "\", \"brojParkirnihMjesta\": " + brojParkirnihMjesta + ", \"brojUlaznoIzlaznihMjesta\":" + brojUlaznoIzlaznihMjesta + "}";
        String porukaZaREST = prk.postJson(podaciParkiralista, String.class, sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString());

        if (porukaZaREST.contains("OK")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    prijevod.getString("index.informacija"), prijevod.getString("index.dodanoNovoParkiraliste")));
        } else if (porukaZaREST.contains("ERR")) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    prijevod.getString("index.greska"), prijevod.getString("index.parkiralisteGreskaDodavanje")));
        }
    }

    /**
     * metoda za ažuriranje podataka odabranog parkirališta
     */
    public void azurirajParkiraliste() {
        dohvatiJezikIVratiObjektPrijevoda();
        if (id > 0) {
            ParkiralistaRESTKlijentId prkid = new ParkiralistaRESTKlijentId(String.valueOf(id));
            String podaciParkiralista = "{\"naziv\": \"" + naziv + "\", \"adresa\": \"" + adresa + "\", \"brojParkirnihMjesta\": " + brojParkirnihMjesta + ", \"brojUlaznoIzlaznihMjesta\":" + brojUlaznoIzlaznihMjesta + "}";
            String porukaRESTa = prkid.putJson(podaciParkiralista, String.class, sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString());

            if (porukaRESTa.contains("OK")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        prijevod.getString("index.informacija"), prijevod.getString("index.dodanoAzuriranoParkiraliste")));

                dohvatiSvaParkiralista();
            } else if (porukaRESTa.contains("ERR")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        prijevod.getString("index.greska"), prijevod.getString("index.parkiralisteGreskaAzuriranje")));
            }
        }
    }

    /**
     * metoda za brisanje odabranog parkirališta
     */
    public void obrisiParkiraliste() {
        if (odabranoParkiraliste.getId() > 0) {
            ParkiralistaRESTKlijentId prkid = new ParkiralistaRESTKlijentId(String.valueOf(odabranoParkiraliste.getId()));
            String porukaRESTa = prkid.deleteJson(String.class, sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString());

            if (porukaRESTa.contains("OK")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        prijevod.getString("index.informacija"), prijevod.getString("index.obrisanoParkiraliste")));

                dohvatiSvaParkiralista();
            } else if (porukaRESTa.contains("ERR")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        prijevod.getString("index.greska"), prijevod.getString("index.greskaBrisanjeParkiralista")));
            }
        }
    }

    /**
     * TODO metoda za aktiviranje parkirališta 
     */
    public void aktivirajParkiraliste() {
        if (id > 0) {

        }
    }

    /**
     * TODO metoda za blokiranje parkirališta
     */
    public void blokirajParkiraliste() {
        if (id > 0) {

        }
    }

    /**
     * metoda koja vraća postojeći status parkirališta
     */
    public void dajStatusParkiralista() {
        dohvatiJezikIVratiObjektPrijevoda();
        if (odabranoParkiraliste.getId() > 0) {
            ParkiralistaRESTKlijentId prkid = new ParkiralistaRESTKlijentId(String.valueOf(odabranoParkiraliste.getId()));
            String porukaREST = prkid.getJson(String.class, sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString());

            if (porukaREST.contains("OK")) {
                Gson gson = new Gson();
                JsonObject objekt = gson.fromJson(porukaREST, JsonObject.class);
                JsonArray polje = gson.fromJson(objekt.get("odgovor"), JsonArray.class);
                
                String status = "";
                
                for (int i = 0; i < polje.size(); i++) {
                    JsonObject objektParkiraliste = polje.get(i).getAsJsonObject();
                    status = objektParkiraliste.get("statusParkiralista").getAsString();
                    System.out.println("STANJE: "+status);
                }
                
                if (status.equals("AKTIVAN")){
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        prijevod.getString("index.informacija"), prijevod.getString("index.aktivnoParkiraliste")));
                }else if (status.equals("PASIVAN")){
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        prijevod.getString("index.informacija"), prijevod.getString("index.pasivnoParkiraliste")));
                }else if(status.equals("BLOKIRAN")){
                    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        prijevod.getString("index.informacija"), prijevod.getString("index.blokiranoParkiraliste")));
                }
                
            } else if (porukaREST.contains("ERR")) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        prijevod.getString("index.greska"), prijevod.getString("index.greskaPrikazStatusaParkiralista")));
            }

        }
    }

    /**
     * metoda dohvaca objekt meteoPodaaka i isti sprema u listu za prikaz u
     * tablici na obrascu
     *
     * @return
     */
    public String dajVazeceMeteoPodatke() {
        dohvatiJezikIVratiObjektPrijevoda();
        listaMeteoPodataka = new ArrayList<>();

        MeteoPodaci mp = MeteoWSKlijent.dajVazeceMeteoPodatke(sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString(), id);

        listaMeteoPodataka.add(mp);

        return "OK";
    }

    /**
     * metoda za dohvaćanje zadnjih meteo podataka za odabrano parkiralište preko MeeteoWSKlijenta
     * @return 
     */
    public String dajZadnjeMeteoPodatke() {
        dohvatiJezikIVratiObjektPrijevoda();
        listaMeteoPodatakaZadnje = new ArrayList<>();

        MeteoPodaci mp = MeteoWSKlijent.dajZadnjeMeteoPodatke(sesija.getAttribute("korisnickoIme").toString(), sesija.getAttribute("lozinka").toString(), id);

        listaMeteoPodatakaZadnje.add(mp);

        return "OK";
    }

    /**
     * metoda za preuzimanje označenog id-a
     *
     * @param event
     */
    public void naOdabranoParkiraliste(SelectEvent event) {
        id = ((Parkiraliste) event.getObject()).getId();
        dajZadnjeMeteoPodatke();
        dajVazeceMeteoPodatke();
    }

    /**
     * metoda za preuzimanje označenog id-a
     *
     * @param event
     */
    public void naOdznacenoParkiraliste(UnselectEvent event) {
        id = ((Parkiraliste) event.getObject()).getId();
        dajZadnjeMeteoPodatke();
        dajVazeceMeteoPodatke();
    }

    /**
     * metoda za preuzimanje podataka iz odabranog objekta parkirališta iz
     * tablice na obrascu
     */
    public void preuzmiPodatkeParkiralista() {
        if (odabranoParkiraliste != null) {
            id = odabranoParkiraliste.getId();
            naziv = odabranoParkiraliste.getNaziv();
            adresa = odabranoParkiraliste.getAdresa();
            brojParkirnihMjesta = odabranoParkiraliste.getBrojParkirnihMjesta();
            brojUlaznoIzlaznihMjesta = odabranoParkiraliste.getBrojUlaznoIzlaznihMjesta();
        }
    }

    /**
     * dohvaća trenutni jezik i postavlja varijablu prijevod na putanju
     */
    public void dohvatiJezikIVratiObjektPrijevoda() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        prijevod = ResourceBundle.getBundle("org.foi.nwtis.domergovi.prijevod", locale);
    }

    public List<MeteoPodaci> getListaMeteoPodatakaZadnje() {
        return listaMeteoPodatakaZadnje;
    }

    public void setListaMeteoPodatakaZadnje(List<MeteoPodaci> listaMeteoPodatakaZadnje) {
        this.listaMeteoPodatakaZadnje = listaMeteoPodatakaZadnje;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getAdresa() {
        return adresa;
    }

    public void setAdresa(String adresa) {
        this.adresa = adresa;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getBrojParkirnihMjesta() {
        return brojParkirnihMjesta;
    }

    public void setBrojParkirnihMjesta(int brojParkirnihMjesta) {
        this.brojParkirnihMjesta = brojParkirnihMjesta;
    }

    public int getBrojUlaznoIzlaznihMjesta() {
        return brojUlaznoIzlaznihMjesta;
    }

    public void setBrojUlaznoIzlaznihMjesta(int brojUlaznoIzlaznihMjesta) {
        this.brojUlaznoIzlaznihMjesta = brojUlaznoIzlaznihMjesta;
    }

    public List<MeteoPodaci> getListaMeteoPodataka() {
        return listaMeteoPodataka;
    }

    public void setListaMeteoPodataka(List<MeteoPodaci> listaMeteoPodataka) {
        this.listaMeteoPodataka = listaMeteoPodataka;
    }

    public int getBrojRedovaZaPrikaz() {
        return brojRedovaZaPrikaz;
    }

    public void setBrojRedovaZaPrikaz(int brojRedovaZaPrikaz) {
        this.brojRedovaZaPrikaz = brojRedovaZaPrikaz;
    }

    public List<Parkiraliste> getListaParkiralista() {
        return listaParkiralista;
    }

    public void setListaParkiralista(List<Parkiraliste> listaParkiralista) {
        this.listaParkiralista = listaParkiralista;
    }

    public Parkiraliste getOdabranoParkiraliste() {
        return odabranoParkiraliste;
    }

    public void setOdabranoParkiraliste(Parkiraliste odabranoParkiraliste) {
        this.odabranoParkiraliste = odabranoParkiraliste;
    }

}
