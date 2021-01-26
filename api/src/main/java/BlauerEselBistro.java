import dto.Dish;
import dto.Menu;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlauerEselBistro implements MenuProvider {

    // Example: Bolognese 5,5€
    Pattern nameAndPriceRegex = Pattern.compile("^(.*?)\\s+(\\d+[.,]?\\d*€)\\s*$");

    @Override
    public Map<LocalDate, Menu> getMenu() {
        Map<LocalDate, Menu> menus = new HashMap<>();

        try {
            for (int i = 0; i < 3; i++) {
                LocalDate date = LocalDate.now().plusWeeks(i);

                int year = date.getYear();
                String month = String.format("%02d", date.getMonth().getValue());
                int week = date.get(WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear());
                LocalDate firstDayOfWeek = date.with(WeekFields.of(Locale.GERMANY).getFirstDayOfWeek());

                getMenu(menus, year, month, week, firstDayOfWeek);
            }
        } catch (IOException ex) {
            return menus;
        } catch (Exception ex) {
            ex.printStackTrace();
            return menus;
        }

        return menus;
    }

    private void getMenu(Map<LocalDate, Menu> menusByDay, int year, String month, int week, LocalDate firstDayOfWeek) throws IOException {
        String pdf = "https://blauer-esel-bistro.de/wp-content/uploads/" + year + "/" + month + "/BISTRO-Speisekarte-KW" + week + ".pdf";
        System.out.println(pdf);
        PDDocument pdfDocument = Util.downloadPDF(pdf);
        ObjectExtractor oe = new ObjectExtractor(pdfDocument);

        Page page = oe.extract(1);
        BasicExtractionAlgorithm be = new BasicExtractionAlgorithm();
        List<? extends Table> tables = be.extract(page);
        List<Dish> dishes = new ArrayList<>();

        for (Table table : tables) {
            List<List<RectangularTextContainer>> rows = table.getRows();

            for (int i = 0; i < rows.size(); i++) {
                List<RectangularTextContainer> row = rows.get(i);
                boolean isLastRow = i == rows.size() - 1;
                if (row.size() != 1) continue;

                RectangularTextContainer<TextElement> textContainer = row.get(0);

                if (isDishText(textContainer)) {
                    String text = Util.cleanString(textContainer.getText());
                    Matcher matcher = nameAndPriceRegex.matcher(text);
                    if (!matcher.find()) continue;

                    Dish d = new Dish(matcher.group(1), Util.parsePrice(matcher.group(2)) * 100);
                    if (isLastRow) {
                        dishes.add(d);
                        break;
                    }

                    List<RectangularTextContainer> nextRow = rows.get(i + 1);
                    textContainer = nextRow.get(0);

                    if (isDetailText(textContainer)) {
                        text = Util.cleanString(textContainer.getText());
                        String details = text.replaceAll("\\s+\\|\\s+", ", ");
                        d.setName(d.getName() + " - " + details.trim());
                        i += 1;
                    }

                    dishes.add(d);
                }
            }

        }

        // Sort by price (low to high)
        dishes.sort(Comparator.comparingInt(Dish::getPrice));

        LocalDate currentDay = firstDayOfWeek;
        for (int i = 0; i < 5; i++) {
            Menu menu = new Menu("Blauer Esel", currentDay);
            menu.setDishes(dishes);
            menusByDay.put(currentDay, menu);
            currentDay = currentDay.plus(1, ChronoUnit.DAYS);
        }
    }

    private boolean isEqual(double a, double b) {
        return Math.abs(a - b) <= 0.1;
    }

    private boolean isDishText(RectangularTextContainer<TextElement> text) {
        return matchesHeight(text, 7.2) && text.getText().contains("€");
    }

    private boolean isDetailText(RectangularTextContainer<TextElement> text) {
        return matchesHeight(text, 6.0);
    }
    
    private boolean matchesHeight(RectangularTextContainer<TextElement> text, double height) {
        double count = 0;
        for (TextElement textElement : text.getTextElements()) {
            if (isEqual(textElement.getHeight(), height)) {
                count += 1;
            }
        }

        // at least 80% of the text elements match the given height
        return count / text.getTextElements().size() > 0.8;
    }
}