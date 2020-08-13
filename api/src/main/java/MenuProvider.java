import dto.Menu;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Map;

public interface MenuProvider {
    Map<LocalDate, Menu> getMenu() throws IOException;
}
