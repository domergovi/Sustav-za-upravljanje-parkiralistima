/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.ws.klijenti;

/**
 *
 * @author Domagoj
 */
public class ParkiranjeWS {

    public static Boolean autenticirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.autenticirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean registrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.registrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean dodajNovoParkiralisteGrupi(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idParkiraliste, java.lang.String nazivParkiraliste, java.lang.String adresaParkiraliste, int kapacitetParkiraliste) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.dodajNovoParkiralisteGrupi(korisnickoIme, korisnickaLozinka, idParkiraliste, nazivParkiraliste, adresaParkiraliste, kapacitetParkiraliste);
    }

    public static StatusKorisnika dajStatusGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.dajStatusGrupe(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean aktivirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.aktivirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean deregistrirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.deregistrirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean blokirajGrupu(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.blokirajGrupu(korisnickoIme, korisnickaLozinka);
    }

    public static Boolean obrisiSvaParkiralistaGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.obrisiSvaParkiralistaGrupe(korisnickoIme, korisnickaLozinka);
    }

    public static boolean aktivirajParkiralisteGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idParkiraliste) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.aktivirajParkiralisteGrupe(korisnickoIme, korisnickaLozinka, idParkiraliste);
    }

    public static boolean blokirajParkiralisteGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idParkiraliste) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.blokirajParkiralisteGrupe(korisnickoIme, korisnickaLozinka, idParkiraliste);
    }

    public static StatusParkiralista dajStatusParkiralistaGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idParkiraliste) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.dajStatusParkiralistaGrupe(korisnickoIme, korisnickaLozinka, idParkiraliste);
    }

    public static boolean obrisiParkiralisteGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idParkiraliste) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.obrisiParkiralisteGrupe(korisnickoIme, korisnickaLozinka, idParkiraliste);
    }

    public static java.util.List<org.foi.nwtis.domergovi.ws.klijenti.Vozilo> dajSvaVozilaParkiralistaGrupe(java.lang.String korisnickoIme, java.lang.String korisnickaLozinka, int idParkiraliste) {
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service service = new org.foi.nwtis.domergovi.ws.klijenti.Parkiranje_Service();
        org.foi.nwtis.domergovi.ws.klijenti.Parkiranje port = service.getParkiranjePort();
        return port.dajSvaVozilaParkiralistaGrupe(korisnickoIme, korisnickaLozinka, idParkiraliste);
    }
    
    
    
}
