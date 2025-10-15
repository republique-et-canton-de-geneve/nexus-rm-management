package ch.ge.cti.nexus.nexusrmgui.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    private DateUtils() {
    }

    public static final String FORMATED_DATE = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

}
