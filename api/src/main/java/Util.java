import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.apache.pdfbox.pdmodel.PDDocument;

public class Util {

    public static PDDocument downloadPDF(String pdfUrl) throws IOException {
        URL url = new URL(pdfUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();

        return PDDocument.load(con.getInputStream());
    }

    public static int parsePrice(String price) {
        return Integer.parseInt(price.replaceAll("[\\s€,.+]", ""));
    }

    public static int parsePriceEuro(String price) throws ParseException {
        DecimalFormat df = new DecimalFormat();
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setCurrencySymbol("€");
        dfs.setDecimalSeparator(',');
        df.setDecimalFormatSymbols(dfs);

        double value = df.parse(price.replaceAll("[+]", "")).doubleValue() * 100;
        return (int) value;
    }

    public static String cleanString(String str) {
        return str.replaceAll("\\R", "");
    }
}
