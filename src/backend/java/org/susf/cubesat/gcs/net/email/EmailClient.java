package org.susf.cubesat.gcs.net.email;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.io.File;
import java.util.List;

public class EmailClient {

    private static final int DEFAULT_PORT = 587;

    private final String username;
    private final String password;
    private final String hostname;
    private final int hostPort;

    public EmailClient(String username, String password, String hostname) {
        this(username, password, hostname, DEFAULT_PORT);
    }

    public EmailClient(String username, String password, String hostname, int hostPort) {
        this.username = username;
        this.password = password;
        this.hostname = hostname;
        this.hostPort = hostPort;
    }

    public void sendHtml(String html, String recipient, String subject, String from) throws EmailException {
        sendHtml(html, recipient, subject, from, null);
    }

    public void sendHtml(String html, String recipient, String subject, String from, List<File> attachments) throws EmailException {
        HtmlEmail email = new HtmlEmail();

        // Host name and port of provider (smtp server)
        email.setHostName(hostname);
        email.setSmtpPort(hostPort);

        // Use TLS instead of SSL for better security and encryption
        email.setSSLOnConnect(false);
        email.setStartTLSEnabled(true);
        email.setStartTLSRequired(true);

        // Useing simple auth, wont work with 2fa providers
        email.setAuthentication(username, password);

        email.addTo(recipient, recipient);
        email.setFrom(username, from);
        email.setSubject(subject);

        email.setHtmlMsg(html);
        email.setTextMsg("Your email client does not support HTML messages");

        // Add all atatchments
        if (attachments != null) {
            for (File attachment : attachments) {
                EmailAttachment report = new EmailAttachment();
                report.setPath(attachment.getAbsolutePath());
                report.setDisposition(EmailAttachment.ATTACHMENT);
                report.setName(attachment.getName());
                email.attach(report);
            }
        }

        // May be blocked by antivirus. Might require tempurarily disabling email scanning to send corectly
        // Might also fail if an untrusted certificate is found in the cert chain (again from antivirus). Double check config
        email.send();
    }
}
