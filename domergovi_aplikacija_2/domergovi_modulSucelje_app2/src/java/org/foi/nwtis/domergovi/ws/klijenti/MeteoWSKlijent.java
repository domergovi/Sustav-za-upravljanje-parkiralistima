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
public class MeteoWSKlijent {

    public static MeteoPodaci dajVazeceMeteoPodatke(java.lang.String korisnickoIme, java.lang.String lozinka, int idParkiralista) {
        org.foi.nwtis.domergovi.ws.klijenti.SOAPWS_Service service = new org.foi.nwtis.domergovi.ws.klijenti.SOAPWS_Service();
        org.foi.nwtis.domergovi.ws.klijenti.SOAPWS port = service.getSOAPWSPort();
        return port.dajVazeceMeteoPodatke(korisnickoIme, lozinka, idParkiralista);
    }

    public static MeteoPodaci dajZadnjeMeteoPodatke(java.lang.String korisnickoIme, java.lang.String lozinka, int idParkiralista) {
        org.foi.nwtis.domergovi.ws.klijenti.SOAPWS_Service service = new org.foi.nwtis.domergovi.ws.klijenti.SOAPWS_Service();
        org.foi.nwtis.domergovi.ws.klijenti.SOAPWS port = service.getSOAPWSPort();
        return port.dajZadnjeMeteoPodatke(korisnickoIme, lozinka, idParkiralista);
    }
    
    
}
