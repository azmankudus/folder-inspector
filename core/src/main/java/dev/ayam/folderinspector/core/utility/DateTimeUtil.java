package dev.ayam.folderinspector.core.utility;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateTimeUtil {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
      .withZone(ZoneId.systemDefault());

  public static String format(TemporalAccessor temporal) {
    if (temporal == null)
      return "N/A";
    return FORMATTER.format(temporal);
  }
}
