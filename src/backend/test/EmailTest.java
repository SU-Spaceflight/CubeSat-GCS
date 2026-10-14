import org.apache.commons.mail.EmailException;
import org.susf.cubesat.gcs.net.email.EmailClient;
import org.susf.cubesat.gcs.util.Env;

public class EmailTest {
    static void main() throws EmailException {
        EmailClient client = new EmailClient(
                Env.get("MAIL_USERNAME"),
                Env.get("MAIL_PASSWORD"),
                Env.get("MAIL_HOST"));

        String recipient = Env.get("MAIL_RECIPIENT");
        String from = System.getenv("MAIL_USERNAME");

        String html = """
            <html>
                <h1 style=\"color:red;\">Test123</h1>
            </html>
        """;

        client.sendHtml(html, recipient, "Test email", from);
    }
}
