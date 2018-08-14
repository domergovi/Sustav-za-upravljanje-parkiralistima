/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.dretve;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.foi.nwtis.domergovi.web.podaci.JMSPoruka;

/**
 *
 * @author Domagoj
 */
public class ObradaPoruka extends Thread {

    /**
     * varijabla koja sluzi za korigiranje rada dretve
     */
    private boolean radi = true;

    /**
     * varijabla koja sluzi za korigiranje rada dretve
     */
    private int intervalDretve;

    /**
     * varijabla koja sluzi za ime posluzitelja iz konfiguracije
     */
    private String posluzitelj;

    /**
     * varijabla koja sluzi za korisnicko ime iz konfiguracije
     */
    private String korisnickoIme;

    /**
     * varijabla koja sluzi za lozinku iz konfiguracije
     */
    private String lozinka;

    /**
     * varijabla koja sluzi za predmet e-mail poruke iz konfiguracije
     */
    private String predmetPoruke;

    /**
     * varijabla koja sluzi za naziv foldera nwtis iz konfiguracije
     */
    private String nazivFolderaIzKonfiguracije;

    /**
     * varijabla koja sluzi za brojanje NWTIS poruka u jednom ciklusu
     */
    private int brojNWTISPoruka;

    /**
     * varijabla koja je namjenjena za detektiranje barem jedne NWTIS poruke
     * tijekom obrade na temelju koj se odlucuje slati/ne slati JMS poruku
     */
    private boolean saljiJMSPoruku;

    /**
     * varijabla za redni broj JMS poruke koja se šalje
     */
    private int redniBrojPoruke;

    /**
     * varijabla za vrijeme slanja prethodne JMS poruke
     */
    private long vrijemeSlanjaPrethodneJMSPoruke = 0;

    /**
     * varijabla za vrijeme slanja trenutne JMS poruke
     */
    private long vrijemeSlanjaTrenutneJMSPoruke = 0;

    private Session session;
    private Store store;
    private Folder folder, NWTISfolder;

    /**
     * konstruktor klase
     *
     * @param intervalDretve
     * @param posluzitelj
     * @param korisnickoIme
     * @param lozinka
     * @param predmet
     * @param nazivFoldera
     */
    public ObradaPoruka(int intervalDretve, String posluzitelj, String korisnickoIme, String lozinka, String predmet, String nazivFoldera) {
        this.intervalDretve = intervalDretve * 1000;
        this.posluzitelj = posluzitelj;
        this.korisnickoIme = korisnickoIme;
        this.lozinka = lozinka;
        this.predmetPoruke = predmet;
        this.nazivFolderaIzKonfiguracije = nazivFoldera;
    }

    @Override
    public void run() {
        redniBrojPoruke = 0;

        while (radi) {
            long pocetak = System.currentTimeMillis();

            saljiJMSPoruku = false;
            brojNWTISPoruka = 0;

            System.out.println("Dretva za OBRADU krenula u: " + new Date());

            spojiSeIPokreniProvjeru();

            long kraj = System.currentTimeMillis() - pocetak;
            long vrijemeSpavanja = (intervalDretve - kraj);

            if (brojNWTISPoruka > 0) {
                saljiJMSPoruku = true;
            }

            System.out.println("VARIJABLA ŠALJI JMS = " + saljiJMSPoruku);
            System.out.println("Broj NWTIS poruka = " + brojNWTISPoruka);

            if (saljiJMSPoruku == true) {
                try {
                    vrijemeSlanjaTrenutneJMSPoruke = System.currentTimeMillis();
                    JMSPoruka jmsPoruka = new JMSPoruka(redniBrojPoruke++, new Date(vrijemeSlanjaPrethodneJMSPoruke),
                            new Date(vrijemeSlanjaTrenutneJMSPoruke), vrijemeSpavanja, brojNWTISPoruka);
                    sendJMSMessageToNWTiS_domergovi_1(jmsPoruka);
                } catch (JMSException | NamingException ex) {
                    Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            vrijemeSlanjaPrethodneJMSPoruke = vrijemeSlanjaTrenutneJMSPoruke;

            try {
                sleep(vrijemeSpavanja);
            } catch (InterruptedException ex) {
                System.out.println("GRESKA: greska kod spavanja dretve - ObradaPoruka");
            }
        }
    }

    @Override
    public synchronized void start() {
        System.out.println("Pokrenuta dretva za EMAIL");
        super.start();
    }

    @Override
    public void interrupt() {
        radi = false;
        System.out.println("Prekinuta dretva za EMAIL");
        super.interrupt();
    }

    /**
     * metoda slizi za spajanje na James-a i provjeru poruka za ciklus pozivom
     * metode provjeriPorukeIzFoldera
     */
    private void spojiSeIPokreniProvjeru() {
        try {
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            session = Session.getInstance(properties, null);

            store = session.getStore("imap");
            store.connect(posluzitelj, korisnickoIme, lozinka);

            folder = store.getFolder("INBOX");
            folder.open(Folder.READ_ONLY);

            NWTISfolder = store.getFolder(nazivFolderaIzKonfiguracije);
            if (!NWTISfolder.exists()) {
                NWTISfolder.create(Folder.HOLDS_MESSAGES);
            }
            NWTISfolder.open(Folder.READ_ONLY);

            System.out.println("Javljam se iz dretve za EMAIL");
            provjeriPorukeIzFoldera(folder);
            
            
            folder.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * metoda na temelju dobivenog foldera provjerava koje su poruke ne
     * pročiitane i na temelju njih poziva metodu za provjeru jel se radi o
     * NWTIS ili neNWTIS poruci
     *
     * @param folder
     */
    public void provjeriPorukeIzFoldera(Folder folder) {
        try {
            Message[] messages = null;
            messages = folder.getMessages();
            System.out.println("INBOX sadrzi: " + messages.length + " poruka");

            for (int i = 0; i < messages.length; i++) {
                System.out.println("Poruka");
                if (!messages[i].isSet(Flags.Flag.SEEN)) {
                    System.out.println("NIJE PROCITANA");
                    if (provjeriIspravnostNWTIS(messages[i])) {
                        System.out.println("NWTIS poruka");
                        dodajPorukuUMapuNWTIS(messages[i]);
                        brojNWTISPoruka++;
                    } else {
                        System.out.println("neNWTIS poruka");
                    }
                }else{
                    System.out.println("Poruka vec procitana SEEN");
                }
            }

        } catch (MessagingException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * metoda koja dodaje NWTIS poruku u mapu NWTIS, ukoliko navedena mapa ne
     * postoji - kreira ju Navedena poruka potom se brise iz mape INBOX
     *
     * @param porukaNWTIS dobivena poruka tipa Message
     * @throws MessagingException u slucaju pogreske
     */
    public void dodajPorukuUMapuNWTIS(Message porukaNWTIS) throws MessagingException {
        porukaNWTIS.setFlag(Flags.Flag.SEEN, true);
        Message[] msg = new Message[]{porukaNWTIS};
        folder.copyMessages(msg, NWTISfolder);
        porukaNWTIS.setFlag(Flags.Flag.DELETED, true);
        folder.expunge();
    }

    /**
     * metoda koja za dobivenu poruku provjerava jel sadrži predmet koji je
     * naveden u konfiguraciji i tip sadržaja koji bi trebao biti text/json ili
     * application/json u slucaju da oba uvjeta sadrzi metoda vraca true, inace
     * false
     *
     * @param poruka
     * @return
     */
    public boolean provjeriIspravnostNWTIS(Message poruka) {
        try {
            Multipart tipSadrzajaPoruke = (Multipart) poruka.getContent();

            String tipSadrzaja = tipSadrzajaPoruke.getBodyPart(0).getHeader("Content-Type")[0];
            System.out.println("Tip sadrzaja: " + tipSadrzaja);
            if (tipSadrzaja.contains("text/json") || tipSadrzaja.contains("TEXT/JSON")
                    || tipSadrzaja.contains("application/json") || tipSadrzaja.contains("APPLICATION/JSON")) {
                if (poruka.getSubject().equals(predmetPoruke)) {
                    return true;
                }
            }
        } catch (MessagingException ex) {
            System.out.println("Greska kod provjere sadrzaja i predmeta NWTIS poruke - ObradaPoruka");
        } catch (IOException ex) {
            Logger.getLogger(ObradaPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * kreiranje JMS poruke
     *
     * @param session
     * @param messageData
     * @return
     * @throws JMSException
     */
    private javax.jms.ObjectMessage createJMSMessageForjmsNWTiS_domergovi_1(javax.jms.Session session, Object messageData) throws JMSException {
        ObjectMessage tm = session.createObjectMessage();
        tm.setObject((JMSPoruka) messageData);
        return tm;
    }

    /**
     * o rednom broju JMS poruke koja se šalje, vremenu slanja prethodne JMS
     * poruke, vremenu slanja trenutne JMS poruke, vremenu rada iteracije
     * dretve, broju NWTiS poruka
     *
     * obliku ObjectMessage poruka naziv reda čekanja NWTiS_{korisnicko_ime}_1)
     *
     */
    private void sendJMSMessageToNWTiS_domergovi_1(Object messageData) throws JMSException, NamingException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("jms/NWTiS_QF_domergovi_1");
        Connection conn = null;
        javax.jms.Session s = null;
        try {
            conn = cf.createConnection();
            s = conn.createSession(false, s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("jms/NWTiS_domergovi_1");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForjmsNWTiS_domergovi_1(s, messageData));
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
}
