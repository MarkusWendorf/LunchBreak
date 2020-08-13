import dto.Dish;
import dto.Menu;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.*;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Kuestenmuehle implements MenuProvider {

    public Map<LocalDate, Menu> getMenu() throws IOException {
        int week = LocalDate.now().get(WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear());
        PDDocument pdfDocument = downloadPDF("https://www.kuestenmuehle.de/files/bilder/pdf/speiseplan-kw" + week + ".pdf");
        ObjectExtractor oe = new ObjectExtractor(pdfDocument);

        Page page = oe.extract(1);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);

        Table table = tables.get(0);
        Map<LocalDate, Menu> menus = new HashMap<>();

        for (int i = 0; i < table.getRowCount(); i++) {
            Menu menu = new Menu("Küstenmühle");

            for (int j = 1; j < table.getColCount(); j++) {
                String dishName = getText(table.getCell(i, j));
                int price = parsePrice(getText(table.getCell(0, j)));
                Dish dish = new Dish(dishName, price);
                menu.addDish(dish);
            }

            LocalDate date = getDateByShorthand(getText(table.getCell(i, 0)));
            menus.put(date, menu);
        }

        pdfDocument.close();
        return menus;
    }

    private String getText(RectangularTextContainer textContainer) {
        List<TextChunk> s = textContainer.getTextElements();
        StringBuilder sb = new StringBuilder();
        for (TextChunk te : s) {
            sb.append(te.getText());
            sb.append(' ');
        }

        return sb.toString().trim()
                .replaceAll("- ", "-")
                .replaceAll("\\s(.{1,2},)+(.{1,2}),?$", "");
    }

    private LocalDate getDateByShorthand(String shorthand) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.getDayOfWeek().equals(DayOfWeek.MONDAY) ? today : today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        DayOfWeek dayOfWeek = getDayOfWeekByShorthand(shorthand);
        return dayOfWeek.equals(DayOfWeek.MONDAY) ? monday : monday.with(TemporalAdjusters.next(dayOfWeek));
    }

    private DayOfWeek getDayOfWeekByShorthand(String shorthand) {
        switch (shorthand.toLowerCase()) {
            case "mo":
                return DayOfWeek.MONDAY;
            case "di":
                return DayOfWeek.TUESDAY;
            case "mi":
                return DayOfWeek.WEDNESDAY;
            case "do":
                return DayOfWeek.THURSDAY;
            default:
                return DayOfWeek.FRIDAY;
        }
    }

    private PDDocument downloadPDF(String pdfUrl) throws IOException {
        URL url = new URL(pdfUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.connect();

        return PDDocument.load(con.getInputStream());
    }

    private int parsePrice(String price) {
        return Integer.parseInt(price.replaceAll("[\\s€,]", ""));
    }
}
