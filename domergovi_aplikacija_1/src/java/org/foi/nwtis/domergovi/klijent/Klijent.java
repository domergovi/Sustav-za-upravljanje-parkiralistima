/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.klijent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;
import org.foi.nwtis.domergovi.konfiguracije.Konfiguracija;
import org.foi.nwtis.domergovi.konfiguracije.KonfiguracijaApstraktna;
import org.foi.nwtis.domergovi.konfiguracije.NeispravnaKonfiguracija;
import org.foi.nwtis.domergovi.konfiguracije.NemaKonfiguracije;

/**
 *
 * @author Domagoj
 */
public class Klijent {
    
    
    public static void main(String[] args) {
        
        System.out.println("Osnovna naredba: KORISNIK korisnik; LOZINKA lozinka; {PAUZA; | KRENI; | PASIVNO; | AKTIVNO; | STANI; | STANJE; | LISTAJ; }");
        System.out.println("Moji podaci: KORISNIK domergovi; LOZINKA nwtis123;");
        System.out.println("____________________________________________________");
        System.out.println("Upište komandu: ");
        
        // kreiranje scannera za hvatanje inputStream-a
        Scanner skener = new Scanner(System.in);
        Klijent klijent = new Klijent();
 
        if (klijent.dohvatiIPostaviPortIzKonfiguracije() != -1){
            try {
                int port = klijent.dohvatiIPostaviPortIzKonfiguracije();
                Socket socket = new Socket("127.0.0.1", port);
                klijent.posaljiPoruku(skener.nextLine(), socket);
                System.out.println("Odgovor: "+klijent.dohvatiPorukuServera(socket));
            } catch (IOException ex) {
                System.out.println("GRESKA: greska kod rada sa socketom - Klijent");
            }
        }
        else{
            System.out.println("GRESKA: greska kod ucitavanja porta iz konfiguracije posluzitelja - Klijent");
        }
    }
    
    /**
     * metoda za slanje podataka na socket
     *
     * @param poruka prvi argument - tekst poruke
     * @param socket drugi argument - socket
     */
    public static void posaljiPoruku(String poruka, Socket socket) {

        try {
            OutputStream os = socket.getOutputStream();
            
            os.write(poruka.getBytes());
            os.flush();
            socket.shutdownOutput();
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod slanja poruke na outputStream - Klijent");
        }
    }
    
    /**
     * metoda koja služi za dohvaćanje i vraćanje vrijednosti porta učitanog iz konfiguracije Poslužitelja
     * @return 
     */
    private int dohvatiIPostaviPortIzKonfiguracije(){
        int dohvaceniPort = -1;
        try {
            File datoteka = new File("web/WEB-INF/NWTIS_konfiguracijaPosluzitelj.txt");
            String putanjaPosluziteljKonf = datoteka.getAbsolutePath();
            Konfiguracija konfiguracijaPosluzitelj = KonfiguracijaApstraktna.preuzmiKonfiguraciju(putanjaPosluziteljKonf);
            dohvaceniPort = Integer.valueOf(konfiguracijaPosluzitelj.dajPostavku("port"));
            
        } catch (NemaKonfiguracije | NeispravnaKonfiguracija ex) {
            System.out.println("GRESKA: greska kod ucitavanje konfiguracije za port - Klijent");
        }
        return dohvaceniPort;
    }
    
    
    /**
     * metoda dohvaća komandu preko socketa (inputstream-a)
     * @param socket argument dobivenog socketa
     * @return vraćena vrijednost naredbe
     * @throws IOException u slucaju greske
     */
    public String dohvatiPorukuServera(Socket socket) throws IOException {

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
