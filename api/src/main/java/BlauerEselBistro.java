import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import dto.Dish;
import dto.Menu;

public class BlauerEselBistro implements MenuProvider {

    @Override
    public Map<LocalDate, Menu> getMenu() throws IOException, ParseException {
        LocalDate today = LocalDate.now();
        List<Dish> dishes = new ArrayList<>();

        Document doc = Jsoup.connect("https://blauer-esel.de/bistro-speisekarte/").get();
        Elements dishList = doc.select(".kartenkopf ~ ul > li");

        for (Element dishElement : dishList) {
            String text = dishElement.text();
            if (text.isEmpty()) continue;

            List<String> parts = Arrays.asList(text.split("\\s"));
            List<String> textParts = parts.subList(0, parts.size() - 1);
            String price = parts.get(parts.size() - 1);
            
            Dish dish = new Dish(String.join(" ", textParts), Util.parsePriceEuro(price));
            dishes.add(dish);
        }

        Map<LocalDate, Menu> menusByDate = new HashMap<>();
        LocalDate firstDayOfWeek = today.with(WeekFields.of(Locale.GERMANY).getFirstDayOfWeek());

        LocalDate currentDay = firstDayOfWeek;
        for (int i = 0; i < 5; i++) {
            Menu menu = new Menu("Blauer Esel", currentDay);
            menu.setDishes(dishes);
            menusByDate.put(currentDay, menu);
            currentDay = currentDay.plus(1, ChronoUnit.DAYS);
        }

        return menusByDate;
    }

}