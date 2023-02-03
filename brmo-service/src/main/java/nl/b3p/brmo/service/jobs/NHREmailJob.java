/*
 * Copyright (C) 2022 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import nl.b3p.brmo.persistence.staging.NHRInschrijving;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.stripesstuff.stripersist.Stripersist;

@DisallowConcurrentExecution
public class NHREmailJob implements Job {
  private static final Log log = LogFactory.getLog(NHREmailJob.class);

  private EntityManager entityManager;

  private boolean processZip(InputStream stream) {
    try (ZipInputStream zip = new ZipInputStream(stream)) {
      while (true) {
        ZipEntry entry = zip.getNextEntry();
        if (entry == null) {
          break;
        }

        if (entry.getName().endsWith(".csv")) {
          return processCsv(zip);
        }
      }
    } catch (Exception e) {
      return false;
    }

    return false;
  }

  private boolean processCsv(InputStream stream) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
      while (true) {
        String line = reader.readLine();
        if (line == null) {
          break;
        }

        // Parse the KVK numer out of the CSV
        line = line.split(",")[0].replaceAll("\"", "");

        NHRInschrijving proces;
        proces = entityManager.find(NHRInschrijving.class, line);
        if (proces == null) {
          proces = new NHRInschrijving();
        }

        proces.setDatum(new Date());
        proces.setVolgendProberen(new Date());
        proces.setProbeerAantal(0);
        proces.setKvkNummer(line);
        entityManager.merge(proces);
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    Session mailSession = null;
    try {
      InitialContext ctx = new InitialContext();
      mailSession = (Session) ctx.lookup("java:comp/env/brmo/nhr/email");
    } catch (NamingException e) {
      return;
    }

    Stripersist.requestInit();
    entityManager = Stripersist.getEntityManager();
    try {
      Store store = mailSession.getStore("imap");
      store.connect();
      Folder folder = store.getFolder("INBOX");
      folder.open(Folder.READ_WRITE);

      Message[] msgs = folder.getMessages();
      FetchProfile fetchProfile = new FetchProfile();
      fetchProfile.add(FetchProfile.Item.ENVELOPE);
      fetchProfile.add(FetchProfile.Item.FLAGS);
      folder.fetch(msgs, fetchProfile);

      for (Message msg : msgs) {
        if (msg.getFlags().contains(Flags.Flag.SEEN)) {
          continue;
        }
        try {
          Object content = msg.getContent();
          if (!(content instanceof MimeMultipart)) {
            continue;
          }

          boolean success = false;

          MimeMultipart multipart = (MimeMultipart) content;
          for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (part.getFileName() == null) {
              continue;
            } else if (part.getFileName().endsWith(".zip")) {
              success = processZip(part.getInputStream());
            } else if (part.getFileName().endsWith(".csv")) {
              success = processCsv(part.getInputStream());
            }

            if (success) {
              break;
            }
          }

          folder.setFlags(new Message[] {msg}, new Flags(Flags.Flag.SEEN), success);

          if (success) {
            String[] messageIDs = msg.getHeader("Message-ID");
            if (messageIDs == null || messageIDs.length == 0) {
              log.info("Processed email");
            } else {
              log.info(String.format("Processed email <%s>", messageIDs[0]));
            }
          }
        } catch (IOException e) {
          log.error("Reading email failed", e);
        }
      }

      folder.close(false);
      store.close();
    } catch (MessagingException e) {
      log.error("Fetching new email failed", e);
    }

    entityManager.getTransaction().commit();
    Stripersist.requestComplete();
  }
}
