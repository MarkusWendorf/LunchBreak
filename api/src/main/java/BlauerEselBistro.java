import dto.Dish;
import dto.Menu;
import org.apache.pdfbox.pdmodel.PDDocument;
import technology.tabula.ObjectExtractor;
import technology.tabula.Page;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.extractors.BasicExtractionAlgorithm;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.regex.Pattern;

public class BlauerEselBistro implements MenuProvider {

    Pattern priceRegex = Pattern.compile("\\d+([.,]\\d+)?€");
    DecimalFormat roundDecimal = new DecimalFormat("#.#");

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
            List<Dish> tempDishes = new ArrayList<>();

            for (List<RectangularTextContainer> row : rows) {
                row.removeIf((c) -> c.getText().equals(""));

                if (row.size() == 2) {
                    Dish d = new Dish(row.get(0).getText(), Util.parsePrice(row.get(1).getText()) * 100);
                    tempDishes.add(d);
                }

                if (row.size() == 1) {
                    RectangularTextContainer container = row.get(0);
                    if (!isDetailText(container)) continue;

                    for (Dish d : tempDishes) {
                        String details = container.getText().replaceAll("\\s+\\|\\s+", ", ");
                        d.setName(d.getName() + " - " + details);
                        dishes.add(d);
                    }

                    tempDishes.clear();
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

    private boolean isDishText(RectangularTextContainer text) {
        return isEqual(text.getHeight(), 7.2);
    }

    private boolean isDetailText(RectangularTextContainer text) {
        return isEqual(text.getHeight(), 6.0);
    }
}