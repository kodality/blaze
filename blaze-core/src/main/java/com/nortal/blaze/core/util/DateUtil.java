package com.nortal.blaze.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

public class DateUtil {
  public static final String DATE = "yyyy-MM-dd";
  public static final String ISO_DATETIME = "yyyy-MM-dd'T'HH:mm:ssX";
  public static final String FHIR_DATETIME = "yyyy-MM-dd'T'HH:mm:ssXXX";
  public static final String RFC_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";
  public static final String ISO_DATETIME_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
  public static final String TIMESTAMP_PG = "yyyy-MM-dd HH:mm:ssX";

  public static String reformat(String dateString, String pattern) {
    return new SimpleDateFormat(pattern).format(parse(dateString));
  }

  public static Date parse(String date) {
    return parse(date,
                 TIMESTAMP_PG,
                 ISO_DATETIME_MILLIS,
                 ISO_DATETIME,
                 FHIR_DATETIME,
                 DATE).orElseThrow(() -> new IllegalArgumentException("Cannot parse date: " + date));
  }

  public static Optional<Date> parse(String date, String... formats) {
    if (date == null) {
      return null;
    }
    for (String format : formats) {
      try {
        return Optional.of(new SimpleDateFormat(format).parse(date));
      } catch (ParseException e) {
        // next try
      }
    }
    return Optional.empty();
  }
  

  public static LocalDateTime toLocalDateTime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

}
