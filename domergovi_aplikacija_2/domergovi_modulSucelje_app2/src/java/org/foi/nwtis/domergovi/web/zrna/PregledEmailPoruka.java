/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.web.zrna;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.servlet.http.HttpSession;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;
import org.foi.nwtis.domergovi.web.podaci.Izbornik;
import org.foi.nwtis.domergovi.web.podaci.Poruka;
import org.foi.nwtis.domergovi.web.slusaci.SlusacAplikacije;

/**
 *
 * @author Domagoj
 */
@Named(value = "pregledEmailPoruka")
@SessionScoped
public class PregledEmailPoruka implements Serializable {

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

    private String posluzitelj;
    private String korisnikPosluzitelj;
    private String lozinkaPosluzitelj;
    private List<Izbornik> popisMapa;
    private String odabranaMapa;
    private Store store;
    private Session session;
    private List<Poruka> popisPoruka;

    /**
     * Creates a new instance of PregledEmailPoruka
     */
    public PregledEmailPoruka() {
        sesija = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
        BP_Konfiguracija bpk = (BP_Konfiguracija) SlusacAplikacije.getKontekst().getAttribute("BP_Konfig");
        brojRedovaZaPrikaz = Integer.valueOf(bpk.getTableNumRowsToShow());
        posluzitelj = bpk.getMailServer();
        korisnikPosluzitelj = bpk.getMailUsernameThread();
        lozinkaPosluzitelj = bpk.getMailPasswordThread();

        odabranaMapa = "INBOX";
        preuzmiMape();
        preuzmiPoruke();
    }


    /**
     * metoda za preuzimanje nazva foldera koji sadrže email poruke
     */
    public void preuzmiMape() {
        dohvatiJezikIVratiObjektPrijevoda();
        try {
            popisMapa = new ArrayList<>();
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            session = Session.getInstance(properties, null);
            store = session.getStore("imap");
            store.connect(posluzitelj, korisnikPosluzitelj, lozinkaPosluzitelj);
            Folder[] f = store.getDefaultFolder().list();
            for (Folder fd : f) {
                popisMapa.add(new Izbornik(fd.getFullName(), fd.getName()));
            }
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MessagingException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * metoda za preuzimanje poruka na temelju odabranog foldera i dodavanje istih u listu
     */
    public void preuzmiPoruke() {
        dohvatiJezikIVratiObjektPrijevoda();
        try {
            popisPoruka = new ArrayList<>();

            Folder folder = store.getFolder(odabranaMapa);
            folder.open(Folder.READ_ONLY);

            Message[] messages = null;
            messages = folder.getMessages();

            for (int i = 0; i < messages.length; i++) {
                int j = i;
                messages[i].setFlag(Flags.Flag.SEEN, true);
                String sadrzaj = getMailContent(messages[i]);
                popisPoruka.add(new Poruka(Integer.toString(j++), messages[i].getSentDate(),
                        messages[i].getReceivedDate(), InternetAddress.toString(messages[i].getFrom()), messages[i].getSubject(), sadrzaj));
            }
        } catch (MessagingException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * metoda za brisanje poruka na temelju odabranog foldera
     */
    public void obrisiPoruke() {
        dohvatiJezikIVratiObjektPrijevoda();
        try {
            Folder folder = store.getFolder(odabranaMapa);
            folder.open(Folder.READ_ONLY);
            
            Message[] messages = null;
            messages = folder.getMessages();
            
            for (int i = 0; i < messages.length; i++) {
                messages[i].setFlag(Flags.Flag.DELETED, true);
            }
            folder.expunge();
            
        } catch (MessagingException ex) {
            Logger.getLogger(PregledEmailPoruka.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * metoda koja na temelju dobivene poruke vraća njen sadržaj
     *
     * @param message
     * @return
     */
    public String getMailContent(Message message) {
        String read = "";
        try {
            if (message.getContentType().contains("multipart")) {
                Multipart multiPart = (Multipart) message.getContent();
                if (multiPart.getCount() == 1) {
                    MimeBodyPart attachment = (MimeBodyPart) multiPart.getBodyPart(0);
                    if (Part.ATTACHMENT.equalsIgnoreCase(attachment.getDisposition())) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(attachment.getInputStream(), Charset.defaultCharset()));
                        char cbuf[] = new char[2048];
                        int len;
                        StringBuilder sbuf = new StringBuilder();
                        while ((len = br.read(cbuf, 0, cbuf.length)) != -1) {
                            sbuf.append(cbuf, 0, len);
                        }
                        read = sbuf.toString();
                    }
                }
            }
        } catch (MessagingException | IOException ex) {
            Logger.getLogger(Poruka.class.getName()).log(Level.SEVERE, null, ex);
        }
        return read;
    }

    /**
     * dohvaća trenutni jezik i postavlja varijablu prijevod na putanju
     */
    public void dohvatiJezikIVratiObjektPrijevoda() {
        Locale locale = FacesContext.getCurrentInstance().getViewRoot().getLocale();
        prijevod = ResourceBundle.getBundle("org.foi.nwtis.domergovi.prijevod", locale);
    }

    public List<Poruka> getPopisPoruka() {
        return popisPoruka;
    }

    public void setPopisPoruka(List<Poruka> popisPoruka) {
        this.popisPoruka = popisPoruka;
    }

    public String getPosluzitelj() {
        return posluzitelj;
    }

    public void setPosluzitelj(String posluzitelj) {
        this.posluzitelj = posluzitelj;
    }

    public String getKorisnikPosluzitelj() {
        return korisnikPosluzitelj;
    }

    public void setKorisnikPosluzitelj(String korisnikPosluzitelj) {
        this.korisnikPosluzitelj = korisnikPosluzitelj;
    }

    public String getLozinkaPosluzitelj() {
        return lozinkaPosluzitelj;
    }

    public void setLozinkaPosluzitelj(String lozinkaPosluzitelj) {
        this.lozinkaPosluzitelj = lozinkaPosluzitelj;
    }

    public List<Izbornik> getPopisMapa() {
        return popisMapa;
    }

    public void setPopisMapa(List<Izbornik> popisMapa) {
        this.popisMapa = popisMapa;
    }

    public String getOdabranaMapa() {
        return odabranaMapa;
    }

    public void setOdabranaMapa(String odabranaMapa) {
        this.odabranaMapa = odabranaMapa;
    }

    public int getBrojRedovaZaPrikaz() {
        return brojRedovaZaPrikaz;
    }

    public void setBrojRedovaZaPrikaz(int brojRedovaZaPrikaz) {
        this.brojRedovaZaPrikaz = brojRedovaZaPrikaz;
    }
}
