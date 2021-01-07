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

    public Map<LocalDate, Menu> getMenu() {
        Map<LocalDate, Menu> menus = new HashMap<>();

        try {
            for (int i = 0; i < 3; i++) {
                String week = LocalDate
                        .now()
                        .plusWeeks(i)
                        .with(WeekFields.of(Locale.GERMANY).getFirstDayOfWeek())
                        .toString();

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

    private void getMenu(Map<LocalDate, Menu> menusByDay, String week) throws IOException {
        System.out.println("https://www.michaelshof.de/michaelservice/speiseplan/insa-39-woche-" + week + ".html");
        Document doc = Jsoup.connect("https://www.michaelshof.de/michaelservice/speiseplan/insa-39-woche-" + week + ".html").get();
        Elements days = doc.select(".weekday");

        for (Element dayElement : days) {
            LocalDate date = parseDate(dayElement.attributes().get("id"));
            Menu menu = new Menu("Insa", date);

            for (Element dishElement : dayElement.select("li")) {
                String html = dishElement.html().replaceFirst("\\s<sup>.*?</sup>", "");
                String price = html.substring(html.lastIndexOf('('));
                String name = html.substring(0, html.lastIndexOf('('));

                Dish dish = new Dish(name.trim(), parsePrice(price));
                menu.addDish(dish);
            }

            if (menusByDay.containsKey(date)) {
                menusByDay.get(date).getDishes().addAll(menu.getDishes());
            } else {
                menusByDay.put(date, menu);
            }
        }
    }

    private LocalDate parseDate(String day) {
        return day.equals("daily-offer") ? LocalDate.now() : LocalDate.parse(day.substring(day.indexOf('-') + 1));
    }

    private int parsePrice(String price) {
        return Integer.parseInt(price.substring(price.indexOf("(") + 1, price.indexOf('&')).replaceAll(",", ""));
    }
}
