package ch.ge.cti.nexus.nexusrmgui.util;

import org.apache.xmlbeans.GDate;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public final String FORMATED_DATE = DATE_FORMAT.format(new Date());
}
