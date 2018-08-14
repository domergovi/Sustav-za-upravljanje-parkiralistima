/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.rest.serveri;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

/**
 * REST Web Service
 *
 * @author Domagoj
 */
@Path("korisnici")
public class KorisniciREST {

    @Context
    private UriInfo context;

    /**
     * varijabla za preuzimanje vrijednosti socketa
     */
    private Socket socket;

    /**
     * Creates a new instance of KorisniciREST
     */
    public KorisniciREST() {
    }

    /**
     * metoda sluzi za slanje naredbe na temelju dobivenih parametara na socket
     * te se nakon toga iz odgovora preuzima popis korisnika i pirkazuje u
     * konačnom odgovoru, inače status: ERR
     *
     * @param korisnickoIme
     * @param lozinka
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getJson(@HeaderParam("korisnickoIme") String korisnickoIme, @HeaderParam("lozinka") String lozinka) {
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + korisnickoIme + "; LOZINKA " + lozinka + "; LISTAJ;");
        String odgovor = "";
        if (odgovorServera.contains("OK 10;")) {
            String korisnici = odgovorServera.replace("OK 10; ", "");
            odgovor = "{\"odgovor\": " + korisnici + ", \"status\": \"OK\"}";
        } else {
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguće dohvatiti korisnike iz baze podataka!\"}";
        }

        return odgovor;
    }

    /**
     * metoda koja na temelju dobivenih podataka u JSON formatu {ime:...
     * prezime:... korisnicko_ime:... lozinka:...} ažurira korisnika u bazi
     * podataka ukoliko je to moguće i ispravan JSON format, u suprotnom status:
     * ERR
     *
     * @param podaciKorisnika
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public String putJson(String podaciKorisnika) {
        JsonObject objekt = null;
        String odgovor = "";
        try {
            JsonReader reader = Json.createReader(new StringReader(podaciKorisnika));
            objekt = reader.readObject();

            if (objekt != null) {
                String korisnickoIme = objekt.get("korisnicko_ime").toString().replace("\"", "");
                String lozinka = objekt.get("lozinka").toString().replace("\"", "");
                String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + korisnickoIme + "; LOZINKA "
                        + lozinka + "; AZURIRAJ " + objekt.get("prezime") + " " + objekt.get("ime") + ";");

                if (odgovorServera.contains("OK 10;")) {
                    odgovor = "{\"odgovor\": [], \"status\": \"OK\"}";
                } else {
                    odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguće azurirati korisnika u bazi podataka!\"}";
                }
            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nesipravan JSON format poruke!\"}";
            }
        } catch (JsonException ex) {
            System.out.println("GRESKA: Neispravan JSON format zapisa kod putJson");
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nesipravan JSON format poruke!\"}";
        }

        return odgovor;
    }

    /**
     * metoda koja na temelju dobivenih podataka u JSON formatu {ime:...
     * prezime:... korisnicko_ime:... lozinka:...} dodaje korisnika u bazu
     * podataka ukoliko je to moguće i ispravan JSON format, u suprotnom status:
     * ERR
     *
     * @param podaciKorisnika
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String postJson(String podaciKorisnika) {
        JsonObject objekt = null;
        String odgovor = "";
        try {
            JsonReader reader = Json.createReader(new StringReader(podaciKorisnika));
            objekt = reader.readObject();

            if (objekt != null) {
                String korisnickoIme = objekt.get("korisnicko_ime").toString().replace("\"", "");
                String lozinka = objekt.get("lozinka").toString().replace("\"", "");
                String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + korisnickoIme + "; LOZINKA "
                        + lozinka + "; DODAJ " + objekt.get("prezime") + " " + objekt.get("ime") + ";");

                if (odgovorServera.contains("OK 10;")) {
                    odgovor = "{\"odgovor\": [], \"status\": \"OK\"}";
                } else {
                    odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguće dodati korisnika u bazu podataka!\"}";
                }
            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nesipravan JSON format poruke!\"}";
            }
        } catch (JsonException ex) {
            System.out.println("GRESKA: Neispravan JSON format zapisa kod putJson");
            odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nesipravan JSON format poruke!\"}";
        }

        return odgovor;
    }

    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @return
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public String deleteJson() {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }

    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @param korisnickoIme
     * @param autentikacija
     * @param korime
     * @param lozinka
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}/{autentikacija}")
    public String putJson(@PathParam("korisnickoIme") String korisnickoIme, @PathParam("autentikacija") String autentikacija, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }

    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @param korisnickoIme
     * @param autentikacija
     * @param korime
     * @param lozinka
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}/{autentikacija}")
    public String postJson(@PathParam("korisnickoIme") String korisnickoIme, @PathParam("autentikacija") String autentikacija, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }

    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @param korisnickoIme
     * @param autentikacija
     * @param korime
     * @param lozinka
     * @return
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}/{autentikacija}")
    public String deleteJson(@PathParam("korisnickoIme") String korisnickoIme, @PathParam("autentikacija") String autentikacija, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }

    /**
     * metoda sluzi za slanje naredbe na temelju dobivenih parametara i
     * autentikaciju korisnika na sockette se nakon toga na temelju odgovora
     * odlučuje jel korisnik autenticiran ili nije
     *
     * @param korisnickoIme korisničko ime korisnika
     * @param autentikacija zbog problema sa već postojećom metodom dodan je još
     * jedan parametar
     * @param korime
     * @param lozinka postavlja se u headeru
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}/{autentikacija}")
    public String getJson(@PathParam("korisnickoIme") String korisnickoIme, @PathParam("autentikacija") String autentikacija, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + korime + "; LOZINKA " + lozinka + ";");
        String odgovor = "";
        if ("korisnickoIme".equals(korisnickoIme) && "autentikacija".equals(autentikacija)) {
            if (odgovorServera.contains("OK 10;")) {
                odgovor = "{\"odgovor\": [], \"status\": \"OK\"}";
            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Korisnik nije autenticiran u bazi!\"}";
            }
        }

        return odgovor;
    }

    /**
     * metoda koja na temelju dobivenog korisničkog imena vraća podatke o korisniku
     * @param korisnickoIme korisničko ime korisnika
     * @param korime
     * @param lozinka postavlja se u headeru
     * @return an instance of java.lang.String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}")
    public String getJson(@PathParam("korisnickoIme") String korisnickoIme, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        String odgovor = "";

        if ("korisnickoIme".equals(korisnickoIme)) {
            String odgovorServera = uspostaviVezuSaSocketomICekajOdgovor("KORISNIK " + korime + "; LOZINKA " + lozinka + "; LISTAJ;");
            if (odgovorServera.contains("OK 10;")) {
                String korisnici = odgovorServera.replace("OK 10; ", "");
                
                JsonReader reader = Json.createReader(new StringReader(korisnici));
                JsonArray polje = reader.readArray();
                
                for (int i = 0; i < polje.size(); i++){
                    if (korime.equals(polje.getJsonObject(i).get("korisnicko_ime").toString().replace("\"", ""))){
                        String id = polje.getJsonObject(i).get("id").toString();
                        String ime = polje.getJsonObject(i).get("ime").toString();
                        String prezime = polje.getJsonObject(i).get("prezime").toString();
                        String korisnik = polje.getJsonObject(i).get("korisnicko_ime").toString();
                        odgovor = "{\"odgovor\": [{\"id\": "+id+", \"ime\": "+ime+", \"prezime\": "+prezime+", \"korisnicko_ime\": "
                                +korisnik+"}], \"status\": \"OK\"}";
                    }
                }
            } else {
                odgovor = "{\"odgovor\": [], \"status\": \"ERR\",\"poruka\": \"Nije moguće dohvatiti korisnika iz baze podataka!\"}";
            }
        }

        return odgovor;
    }

    
    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @param korisnickoIme
     * @param korime
     * @param lozinka
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}")
    public String putJson(@PathParam("korisnickoIme") String korisnickoIme, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }

    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @param korisnickoIme
     * @param korime
     * @param lozinka
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}")
    public String postJson(@PathParam("korisnickoIme") String korisnickoIme, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }

    /**
     * metoda koja vraća poruku pogreške jer nije dozvoljena
     *
     * @param korisnickoIme
     * @param korime
     * @param lozinka
     * @return
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{korisnickoIme}")
    public String deleteJson(@PathParam("korisnickoIme") String korisnickoIme, @HeaderParam("korisnickoIme") String korime, @HeaderParam("lozinka") String lozinka) {
        return "{\"odgovor\": [], "
                + "\"status\": \"ERR\", "
                + "\"poruka\": \"Nije dozvoljeno\"}";
    }
    
    
    /**
     * metoda za slanje podataka na socket
     *
     * @param poruka prvi argument - tekst poruke
     * @param socket drugi argument - socket
     */
    private void posaljiPoruku(String poruka) {

        try {
            OutputStream os = socket.getOutputStream();

            os.write(poruka.getBytes());
            os.flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod slanja poruke na outputStream - KorisniciREST");
        }
    }

    /**
     * metoda sluzi za uspostavljanje veze sa socketom i prosljeđivanje naredbe
     * na njega te preuzimanje odgovora
     *
     * @param naredba
     * @return
     */
    private String uspostaviVezuSaSocketomICekajOdgovor(String naredba) {
        try {
            //int port = klijent.dohvatiIPostaviPortIzKonfiguracije();
            int port = 4000;
            socket = new Socket("127.0.0.1", port);
            posaljiPoruku(naredba);
            String odgovorServera = dohvatiPorukuServera();
            return odgovorServera;
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod rada sa socketom - KorisniciREST");
        }
        return "";
    }

    /**
     * metoda dohvaća komandu preko socketa (inputstream-a)
     *
     * @return vraćena vrijednost naredbe
     * @throws IOException u slucaju greske
     */
    private String dohvatiPorukuServera() throws IOException {

        InputStream is = socket.getInputStream();

        int znak;
        StringBuffer buffer = new StringBuffer();
        while (true) {
            znak = is.read();
            if (znak == -1) {
                break;
            }
            buffer.append((char) znak);
        }

        String naredba = buffer.toString();

        socket.shutdownInput();
        return naredba;
    }
}
