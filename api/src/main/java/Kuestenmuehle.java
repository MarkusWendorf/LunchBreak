import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;

import dto.Dish;
import dto.Menu;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.TextChunk;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

public class Kuestenmuehle implements MenuProvider {

    public Map<LocalDate, Menu> getMenu() {
        Map<LocalDate, Menu> menus = new HashMap<>();

        try {
            for (int i = 0; i < 3; i++) {
                int week = LocalDate.now().plusWeeks(i).get(WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear());
                getMenu(menus, week);
            }
        } catch (IOException ex) {
            return menus;
        } catch (Exception ex) {
            ex.printStackTrace();
            return menus;
        }

        return menus;
    }

    private void getMenu(Map<LocalDate, Menu> menusByDay, int week) throws IOException {
        String weekLeadingZero = String.format("%02d", week);
        PDDocument pdfDocument = Util.downloadPDF("https://www.kuestenmuehle.de/files/bilder/pdf/speiseplan-kw" + weekLeadingZero + ".pdf");
        ObjectExtractor oe = new ObjectExtractor(pdfDocument);

        Page page = oe.extract(1);
        SpreadsheetExtractionAlgorithm se = new SpreadsheetExtractionAlgorithm();
        List<? extends Table> tables = se.extract(page);

        for (Table table : tables) {

            for (int i = 1; i < table.getRowCount(); i++) {
                LocalDate date = getDateByShorthand(getText(table.getCell(i, 0)), week);
                if (date == null) continue;

                Menu menu = new Menu("Küstenmühle", date);

                for (int j = 1; j < table.getColCount(); j++) {
                    String dishName = getText(table.getCell(i, j));
                    if (dishName.isEmpty()) continue;

                    int price = Util.parsePrice(getText(table.getCell(0, j)));
                    Dish dish = new Dish(dishName, price);
                    menu.addDish(dish);
                }

                menusByDay.put(date, menu);
            }
        }

        pdfDocument.close();
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
                .replaceAll("-([a-z])", "$1")
                .replaceAll("\\s(.{1,2},)+(.{1,2}),?$", "");
    }

    private LocalDate getDateByShorthand(String shorthand, int week) {
        LocalDate mondayOfWeek = LocalDate.now().with(WeekFields.ISO.weekOfWeekBasedYear(), week)
                .with(WeekFields.of(Locale.GERMANY).getFirstDayOfWeek());

        DayOfWeek dayOfWeek = getDayOfWeekByShorthand(shorthand);
        if (dayOfWeek == null) return null;

        return mondayOfWeek.with(TemporalAdjusters.nextOrSame(dayOfWeek));
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
            case "fr":
                return DayOfWeek.FRIDAY;
            default:
                return null;
        }
    }
}
