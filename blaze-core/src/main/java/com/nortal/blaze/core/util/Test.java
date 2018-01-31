package com.nortal.blaze.core.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Test {

  public static void main(String[] args) throws ParseException {
    System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSX").parse("2018-01-31 15:00:00.962102+02"));
    //System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX").parse("2018-01-31 15:16:05.962102+02"));
  }
  
}
