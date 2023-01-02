import dto.Menu;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.Map;

public interface MenuProvider {
    Map<LocalDate, Menu> getMenu() throws IOException, ParseException;
}
