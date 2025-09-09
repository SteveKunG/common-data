import java.time.ZoneId;
import java.time.chrono.ThaiBuddhistDate;
import java.util.Random;

import org.slf4j.Logger;
import com.stevekung.common.data.DateConverter;
import com.stevekung.common.data.Locales;
import com.stevekung.common.data.util.LoggerUtils;

public class DateTest {
    private static final Logger LOGGER = LoggerUtils.getLogger();

    public static void main(String[] args) {

        LOGGER.info("Current date: {}", DateConverter.builder()
                .current()
                .to("yyyy-MM-dd HH:mm:ss.SSS"));

        LOGGER.info("Current sql date: {}", DateConverter.builder()
                .current()
                .to("yyyy-MM-dd HH:mm:ss.SSS")
                .toSqlDate());

        LOGGER.info("Date format from {} to {}", "2025-05-08 12:15:12", DateConverter.builder()
                .from("2025-05-08 12:15:12", "yyyy-MM-dd HH:mm:ss")
                .to("yyyy/MM/dd"));

        LOGGER.info("Date format from {} to thai {}", "2025-08-16 12:15:12", DateConverter.builder()
                .from("2025-08-16 12:15:12", "yyyy-MM-dd HH:mm:ss")
                .to("dd MMM yyyy")
                .outputDateTo(ThaiBuddhistDate::from)
                .toLocale(Locales.TH));

        LOGGER.info("Date format from thai {} to {}", "16 ส.ค. 2568 12:15:12", DateConverter.builder()
                .modifyOutput(localDateTime -> localDateTime.minusYears(543))
                .fromLocale(Locales.TH)
                .from("16 ส.ค. 2568 12:15:12", "dd MMM yyyy HH:mm:ss")
                .to("yyyy-MM-dd HH:mm:ss"));

        LOGGER.info("Date format from {} to millisec {}", "2025-08-16 12:15:12.9", DateConverter.builder()
                .from("2025-08-16 12:15:12.9", "yyyy-MM-dd HH:mm:ss.S")
                .to("yyyy-MM-dd HH:mm:ss.SSS"));

        String randomDate = new Random().nextBoolean() ? "2025-08-16 12:15:12.9" : "2025-08-16 12:15:12";
        LOGGER.info("Two inputs date from {} to {}", randomDate, DateConverter.builder()
                .inputCondition((inputDate, defaultInputPattern) -> {
                    if (inputDate.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {
                        return "yyyy-MM-dd HH:mm:ss";
                    }
                    return defaultInputPattern;
                })
                .from(randomDate, "yyyy-MM-dd HH:mm:ss.S")
                .to("yyyy-MM-dd HH:mm:ss.SSS"));
    }
}
