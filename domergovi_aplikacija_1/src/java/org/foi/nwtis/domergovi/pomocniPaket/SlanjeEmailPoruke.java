package org.foi.nwtis.domergovi.pomocniPaket;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletContext;
import org.foi.nwtis.domergovi.konfiguracije.bp.BP_Konfiguracija;


/**
 *
 * @author Domagoj
 */
public class SlanjeEmailPoruke {
    
    /**
     * varijabla za preuzimanje posluzitalja za slanje email poruke
     */
    private String posluzitelj;
    /**
     * varijabla za preuzimanje korisnika koji prima email poruke
     */
    private String prima;
    /**
     * varijabla za preuzimanje korisnika koji salje email poruke
     */
    private String salje;
    /**
     * varijabla za predmet email poruke
     */
    private String predmet;
    /**
     * varijabla za primljenu naredbu koja se salje email porukom
     */
    private String primljenaNaredba;
    /**
     * varijabla za redni broj email poruke
     */
    public static int redniBrojPoruke = 0;
    /**
     * varijabla za sadrzaj email poruke
     */
    private String sadrzajPoruke;
    /**
     * varijabla za privitak email poruke
     */
    private String privitak;
    /**
     * varijabla za naziv datoteke koja se salje preko email poruke
     */
    private String datotekaNaziv;
    /**
     * varijabla za lozinku korinsika preko email poruke
     */
    private String lozinka;
    
    /**
     * konstruktor klase u kojem se inicijaliziraju parametri ucitani iz datoteke konfiguracije
     * @param kontekst
     * @param naredba 
     */
    public SlanjeEmailPoruke(ServletContext kontekst, String naredba) {
        BP_Konfiguracija bpk = (BP_Konfiguracija) kontekst.getAttribute("BP_Konfig");
        this.posluzitelj = bpk.getMailServer();
        this.prima = bpk.getMailUsernameThread();
        this.salje = bpk.getMailUsernameEmailAddress();
        this.predmet = bpk.getMailSubjectEmail();
        this.datotekaNaziv = bpk.getMailAttachmentFilename();
        this.privitak = "{}";
        this.primljenaNaredba = naredba;
    }
    
    
    /**
     * metoda koja sluzi za slanje email poruke
     */
    public void saljiPoruku(){
        try {
            //postavljanje parametara
            java.util.Properties properties = System.getProperties();
            properties.put("mail.smtp.host", posluzitelj);
            
            Session session = Session.getInstance(properties, null);
            MimeMessage message = new MimeMessage(session);
            
            Address fromAddress = new InternetAddress(salje);
            message.setFrom(fromAddress);
            Address[] toAddresses = InternetAddress.parse(prima);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(predmet); 
            
            // konstrukcija poruke
            File objektDatoteke = radiSaPomocnomDatotekom();
            if (objektDatoteke != null){
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                MimeMultipart multiPart = new MimeMultipart();
                DataSource source = new FileDataSource(objektDatoteke.getAbsolutePath());
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(datotekaNaziv);
                messageBodyPart.setHeader("Content-Type", "text/json");
                multiPart.addBodyPart(messageBodyPart);
                message.setContent(multiPart);
                message.setSentDate(new Date());
                
                //posalji poruku
                Transport.send(message);
            }
            
        } catch (MessagingException e) {
            System.out.println("GRESKA: greska kod slanja poruke - SlanjeEmailPoruke - " + e.getMessage());
        }
    }
    
    
    /**
     * metoda u kojoj se sastavlja i vraca konacna poruka za slanje putem e-maila
     * @return 
     */
    private String sastaviSadrzajEmailPoruke(){
        redniBrojPoruke++;
        
        String[] naredbaUNizu = primljenaNaredba.split(";");
        String novaNaredba = naredbaUNizu[0]+";"+naredbaUNizu[2]+";";
        String porukaZaSlanje = "{\"id\": "+redniBrojPoruke+", \"komanda\": "+novaNaredba+", \"vrijeme\": \""+vratiTrenutniDatum()+"\"}";
        
        return porukaZaSlanje;
    }
    
    /**
     * metoda za konvertiranje trenutnog datuma i vremena u trazeni format
     * @return
     */
    public String vratiTrenutniDatum() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS");
        String trenutniDatum = sdf.format(new Date());
        
        return trenutniDatum;

    }
    
    /**
     * metoda koja vraca objekt pomocne privremeno kreirane datoteke sa sadrzajem privitka
     * @return 
     */
    public File radiSaPomocnomDatotekom() {
        File pomocna = null;
        try {
            pomocna = File.createTempFile("pomocna", ".json");
            BufferedWriter bw = new BufferedWriter(new FileWriter(pomocna));
            privitak = sastaviSadrzajEmailPoruke();
            bw.write(privitak);
            bw.close();
            
        } catch (IOException ex) {
            System.out.println("GRESKA: greska kod rada s pomocnom datotekom - SlanjeEmailPoruke");
        }
        return pomocna;
    }
}
