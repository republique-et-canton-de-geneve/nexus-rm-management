package ch.ge.cti.nexus.nexusrmgui.util;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    Date now = new Date();

    public final SimpleDateFormat DATEFORMATEUR = new SimpleDateFormat("yyyyMMdd_HHmmss");

    public final String DATEFORMATEE = DATEFORMATEUR.format(now);
}
