package ch.ge.cti.nexus.nexusrmgui.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

/**
 * Transforme en LocalDateTime une date reçue de FormServices, comme "2020-11-25T15:42:05.445+0000" ou
 * "2020-11-25T15:42:05.445+00:00".
 */
@Slf4j
public class NexusServicesLocalDateTimeDeserializer extends LocalDateTimeDeserializer {
    private static final long serialVersionUID = 1;

    private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder()
            // date/time
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            // offset (hh:mm - "+00:00" when it's zero)
            .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
            // offset (hhmm - "+0000" when it's zero)
            .optionalStart().appendOffset("+HH:MM", "+0000").optionalEnd()
            // offset (hh - "+00" when it's zero)
            .optionalStart().appendOffset("+HH", "+00").optionalEnd()
            // offset (pattern "X" uses "Z" for zero offset)
            .optionalStart().appendPattern("X").optionalEnd()
            // create formatter
            .toFormatter();

    public NexusServicesLocalDateTimeDeserializer() {
        super(FORMAT);
    }

    @Override
    public LocalDateTime deserialize(JsonParser parser, DeserializationContext ctx) throws IOException {
        return LocalDateTime.parse(parser.getText(), FORMAT);
    }

}

