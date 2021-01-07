import dto.Dish;
import dto.Menu;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import technology.tabula.*;
import technology.tabula.extractors.BasicExtractionAlgorithm;
import technology.tabula.extractors.SpreadsheetExtractionAlgorithm;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlauerEselBistro implements MenuProvider {

    Pattern priceRegex = Pattern.compile("\\d+([.,]\\d+)?€");

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

        PDDocument pdfDocument = Util.downloadPDF(pdf);
        ObjectExtractor oe = new ObjectExtractor(pdfDocument);

        Page page = oe.extract(1);
        BasicExtractionAlgorithm be = new BasicExtractionAlgorithm();
        List<? extends Table> tables = be.extract(page);
        List<Dish> dishes = new ArrayList<>();

        for (Table table : tables) {
            for (int i = 0; i < table.getRowCount(); i++) {
                String line = table.getCell(i, 0).getText().replaceAll("[\\s+/]", " ");

                if (line.contains("€")) { // Ganzer Knusper-Broiler 17€
                    Matcher matcher = priceRegex.matcher(line);
                    if (!matcher.find()) {
                        continue;
                    }

                    String price = matcher.group();
                    String dishName = line.replace(price, "").trim();

                    int offset = i + 1;
                    String detailLine; // Fritten | Cole Slaw | Orange-Curry Mayo
                    while ((detailLine = table.getCell(offset, 0).getText()).contains("€")) {
                        offset += 1;
                    }

                    String details = detailLine.replaceAll("\\s+\\|\\s+", ", ");

                    Dish dish = new Dish(dishName + " - " + details, Util.parsePrice(price) * 100);
                    System.out.println(dish.getName());
                    dishes.add(dish);
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
}