import dto.Dish;
import dto.Menu;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class Insa implements MenuProvider {

    public Map<LocalDate, Menu> getMenu() throws IOException {

        Map<LocalDate, Menu> menus = new HashMap<>();

        String thisWeek = LocalDate.now().with(WeekFields.of(Locale.GERMANY).getFirstDayOfWeek()).toString();
        Document doc = Jsoup.connect("https://www.michaelshof.de/michaelservice/speiseplan/insa-39-woche-" + thisWeek + ".html").get();
        Elements days = doc.select(".weekday");

        for (Element dayElement : days) {
            Menu menu = new Menu("Insa");

            for (Element dishElement : dayElement.select("li")) {
                String[] name0price1 = dishElement.html().split("<.+?>.*?</.+?>");
                Dish dish = new Dish(name0price1[0].trim(), parsePrice(name0price1[1]));
                menu.addDish(dish);
            }

            LocalDate date = parseDate(dayElement.attributes().get("id"));
            if (menus.containsKey(date)) {
                menus.get(date).getDishes().addAll(menu.getDishes());
            } else {
                menus.put(date, menu);
            }
        }

        return menus;
    }

    public String getProviderName() {
        return "Insa";
    }

    private LocalDate parseDate(String day) {
        return day.equals("daily-offer") ? LocalDate.now() : LocalDate.parse(day.substring(day.indexOf('-') + 1));
    }

    private int parsePrice(String price) {
        return Integer.parseInt(price.substring(price.indexOf("(") + 1, price.indexOf('&')).replaceAll(",", ""));
    }
}
