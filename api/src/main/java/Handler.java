import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.Menu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

public class Handler implements RequestHandler<ScheduledEvent, Void> {
    public static void main(String[] args) throws IOException {
        // Map<Integer,  Map<LocalDate, List<Menu>>> combined = getMenusForCurrentWeek(Arrays.asList(new Kuestenmuehle()));
    }

    private static Map<Integer,  Map<LocalDate, List<Menu>>> getMenusForCurrentWeek(List<MenuProvider> providers) throws IOException {
        // tree map for ordering
        Map<Integer,  Map<LocalDate, List<Menu>>> kw = new TreeMap<>();

        // combine menus from different providers by week and date
        for (MenuProvider provider : providers) {
            Map<LocalDate, Menu> menusByDay = provider.getMenu();
            if (menusByDay == null) continue;

            for (Map.Entry<LocalDate, Menu> menu : menusByDay.entrySet()) {
                int week = menu.getKey().get(WeekFields.of(Locale.GERMANY).weekOfWeekBasedYear());
                Map<LocalDate, List<Menu>> byDay = kw.computeIfAbsent(week, k -> new TreeMap<>());

                List<Menu> menus = byDay.computeIfAbsent(menu.getKey(), k -> new ArrayList<>());
                menus.add(menu.getValue());
            }
        }

        return kw;
    }

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        try {
            List<MenuProvider> menuProviders = Arrays.asList(new Insa(), new Kuestenmuehle(), new BlauerEselBistro());
            Map<Integer,  Map<LocalDate, List<Menu>>> combined = getMenusForCurrentWeek(menuProviders);

            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, combined);
            byte[] data = out.toByteArray();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setCacheControl("max-age=1800");
            InputStream input = new ByteArrayInputStream(data);

            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
            s3.putObject(System.getenv("bucket"), "lunchdata.json", input, metadata);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}