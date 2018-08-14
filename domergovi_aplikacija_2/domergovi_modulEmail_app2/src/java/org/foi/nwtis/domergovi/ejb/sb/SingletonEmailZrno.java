/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.foi.nwtis.domergovi.ejb.sb;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.LocalBean;
import javax.ejb.Startup;
import org.foi.nwtis.domergovi.web.dretve.ObradaPoruka;

/**
 *
 * @author Domagoj
 */
@Singleton
@LocalBean
@Startup
public class SingletonEmailZrno {

    /**
     * varijabla za preuzimanje mail imap porta iz ejb-jar.xml-a
     */
    @Resource(name = "mail.imap.port")
    private String imapPortKonfiguracija;

    /**
     * varijabla za preuzimanje naziva usernameThread iz ejb-jar.xml-a
     */
    @Resource(name = "mail.usernameThread")
    private String userNameThreadKonfiguracija;

    /**
     * varijabla za preuzimanje naziva passwordThread iz ejb-jar.xml-a
     */
    @Resource(name = "mail.passwordThread")
    private String passwordThreadKonfiguracija;

    /**
     * varijabla za preuzimanje vrijendosti intervala rada dretve iz
     * ejb-jar.xml-a
     */
    @Resource(name = "mail.timeSecThreadCycle")
    private String timeSecThreadCycleKonfiguracija;

    /**
     * varijabla za preuzimanje naziva attachment-a iz ejb-jar.xml-a
     */
    @Resource(name = "mail.attachmentFilename")
    private String attachmentFileKonfiguracija;

    /**
     * varijabla za preuzimanje naziva username email address iz ejb-jar.xml-a
     */
    @Resource(name = "mail.usernameEmailAddress")
    private String usernameEmailAddressKonfiguracija;

    /**
     * varijabla za preuzimanje naziva predmeta poruke iz ejb-jar.xml-a
     */
    @Resource(name = "mail.subjectEmail")
    private String subjectKonfiguracija;

    /**
     * varijabla za preuzimanje naziva NWTIS foldera iz ejb-jar.xml-a
     */
    @Resource(name = "mail.folderNWTiS")
    private String folderNWTISKonfiguracija;

    /**
     * varijabla za preuzimanje naziva mail servera iz ejb-jar.xml-a
     */
    @Resource(name = "mail.server")
    private String mailServerKonfiguracija;

    ObradaPoruka dretvaObradePoruka;

    public SingletonEmailZrno() {
    }

    @PostConstruct
    public void inicijalizirajParametre() {
        System.out.println("Pokrenut konstruktor Singleton zrna!");

        //TODO pokreni dretvu za provjeru/pregled poruka
        dretvaObradePoruka = new ObradaPoruka(Integer.valueOf(timeSecThreadCycleKonfiguracija),
                mailServerKonfiguracija, userNameThreadKonfiguracija, passwordThreadKonfiguracija, subjectKonfiguracija, folderNWTISKonfiguracija);

        dretvaObradePoruka.start();
    }

    /**
     * metoda za brisanje sengleton SB
     */
    @PreDestroy
    private void obrisiSingletonEmailZrno() {
        // TODO prekini rad dretve za preuzimanje i pregled poruka
        dretvaObradePoruka.interrupt();
        System.out.println("Unistavam Singleton zrno!");
    }
}
