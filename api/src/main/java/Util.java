import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Util {

    public static PDDocument downloadPDF(String pdfUrl) throws IOException {
        URL url = new URL(pdfUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();

        return PDDocument.load(con.getInputStream());
    }

    public static int parsePrice(String price) {
        return Integer.parseInt(price.replaceAll("[\\s€,.]", ""));
    }

    public static String cleanString(String str) {
        return str.replaceAll("\\R", "");
    }
}
