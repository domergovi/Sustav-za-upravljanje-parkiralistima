/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.ejb.mdb;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import org.foi.nwtis.domergovi.ejb.sb.SpremanjeJMS;
import org.foi.nwtis.domergovi.web.podaci.JMSPoruka;

/**
 *
 * @author Domagoj
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/NWTiS_domergovi_1")
    ,
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
})
public class ObradaEmaila implements MessageListener {

    @EJB
    private SpremanjeJMS spremanjeJMS;
    
    public ObradaEmaila() {
    }
    
    /**
     * metoda koja zaprima JMS poruke, ako je instanca klase JMS poruka tada je to poruka koju trebamo
     * i onda dalje ide obrada
     * @param message 
     */
    @Override
    public void onMessage(Message message) {
        try {
            ObjectMessage dolaznaPoruka = (ObjectMessage) message;
            if (dolaznaPoruka.getObject() instanceof JMSPoruka){
                JMSPoruka poruka = (JMSPoruka) dolaznaPoruka.getObject();
                
                System.out.println("APP3 - JMS: "+poruka.getRedniBrojJMSPoruke()+", "+poruka.getVrijemeSlanjaTrenutneJMSPoruke());
                
                //spremi poruku u listu u klasi JMSSpremanje
                spremanjeJMS.dohvatiJMSPorukuISpremiUListu(poruka);
            }
        } catch (JMSException ex) {
            System.out.println("GRESKA: greska kod zaprimanja JMS poruka - ObradaEmailDrivenBean");
        }
    }
    
    
    
}
