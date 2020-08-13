import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.Menu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

public class Handler implements RequestHandler<ScheduledEvent, Void> {
    public static void main(String[] args) throws IOException, InterruptedException {
    }

    private static Collection<List<Menu>> getMenusForCurrentWeek(List<MenuProvider> providers) throws IOException {
        // tree map for ordering
        Map<LocalDate, List<Menu>> combined = new TreeMap<>();

        for (MenuProvider provider : providers) {
            Map<LocalDate, Menu> menusByDay = provider.getMenu();

            for (Map.Entry<LocalDate, Menu> menu : menusByDay.entrySet()) {
                List<Menu> existingMenus = combined.computeIfAbsent(menu.getKey(), k -> new ArrayList<>());
                existingMenus.add(menu.getValue());
            }
        }

        return combined.values();
    }

    @Override
    public Void handleRequest(ScheduledEvent event, Context context) {
        try {
            Collection<List<Menu>> combined = getMenusForCurrentWeek(Arrays.asList(new Insa(), new Kuestenmuehle()));
            ObjectMapper mapper = new ObjectMapper();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mapper.writeValue(out, combined);
            byte[] data = out.toByteArray();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setCacheControl("max-age=3600");
            InputStream input = new ByteArrayInputStream(data);

            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.EU_CENTRAL_1).build();
            s3.putObject(System.getenv("bucket"), "lunchdata.json", input, metadata);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}