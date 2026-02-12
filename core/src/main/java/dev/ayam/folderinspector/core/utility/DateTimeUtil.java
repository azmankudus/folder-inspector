package dev.ayam.folderinspector.core.utility;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * Utility class for date and time formatting.
 */
public class DateTimeUtil {
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME
      .withZone(ZoneId.systemDefault());

  /**
   * Formats a temporal object into an ISO-8601 string representation.
   *
   * @param temporal The temporal object to format.
   * @return The formatted string, or "N/A" if the input is null.
   */
  public static String format(TemporalAccessor temporal) {
    if (temporal == null)
      return "N/A";
    return FORMATTER.format(temporal);
  }
}
