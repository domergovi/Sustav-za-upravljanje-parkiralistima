/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.posluzitelj;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.servlet.ServletContext;
import org.foi.nwtis.domergovi.konfiguracije.Konfiguracija;
import org.foi.nwtis.domergovi.web.dretve.PreuzmiMeteoPodatke;
import org.foi.nwtis.domergovi.web.dretve.RadnaDretva;

/**
 *
 * @author Domagoj
 */
public class Posluzitelj extends Thread{
    
    /**
     * varijabla za kreirani kontekst iz slusaca aplikacije
     */
    public ServletContext kontekst;
    /**
     * varijabla za dretvu koja preuzima meteo podatke
     */
    public static PreuzmiMeteoPodatke dretvaMeteo;
    
    /**
     * varijabla namjenjena za preuzimanje socketa
     */
    public static ServerSocket serverSocket;
    
    /**
     * varijabla namjenjena za preuzimanje važećeg porta
     */
    private int port;
    
    /**
     * varijabla namjenjena za upravljanje radom servera
     */
    public static boolean radi = true;
    
    /**
     * statična varijabla koja se regulira iz radne dretve
     * preko nje se provjerava jel server u stanju pauze ili nije (true/false)
     */
    public static boolean parametarPauza = false;
    
    
    /**
     * konstruktor klase koji dobiva kontekst i dretvu za meteo podatke
     * @param kontekst
     * @param dretva 
     */
    public Posluzitelj(ServletContext kontekst, PreuzmiMeteoPodatke dretva) {
        this.kontekst = kontekst;
        this.dretvaMeteo = dretva;
    }

    @Override
    public void run() {
        try {
            Konfiguracija posluziteljKonf = (Konfiguracija) kontekst.getAttribute("Posluzitelj_Konfig");
            port = Integer.valueOf(posluziteljKonf.dajPostavku("port"));
            serverSocket = new ServerSocket(port);
            
            while (radi){
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Korisnik se spojio!");
                    
                    RadnaDretva radnaDretva = new RadnaDretva(socket,kontekst,dretvaMeteo);
                    radnaDretva.start();
       
                } catch (IOException ex) {
                    System.out.println("GRESKA: "+ex.getMessage());
                }
            }
        } catch (IOException ex) {
            System.out.println("GRESKA: "+ex.getMessage());
        }
    }

    @Override
    public synchronized void start() {
        super.start();
    }
    
    
    @Override
    public void interrupt() {
        super.interrupt();
    }
    
    /**
     * metoda koja služi za zaustavlajnje rada dretvi i prekid rada socketa
     */
    public static void ugasiSocketIPrekiniPosluzitelj(){
        dretvaMeteo.prekiniRadDretveMeteo();
        radi = false;
        try {
            serverSocket.close();
            System.out.println("Info: Server socket je zatvoren i prekinuta je drteva!");
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod zatvaranja socketa!");
        }
    }
    
    
    /**
     * metoda dohvaća komandu preko socketa (inputstream-a)
     * @param socket argument dobivenog socketa
     * @return vraćena vrijednost naredbe
     * @throws IOException u slucaju greske
     */
    public static String dohvatiNaredbu(Socket socket) throws IOException {

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
