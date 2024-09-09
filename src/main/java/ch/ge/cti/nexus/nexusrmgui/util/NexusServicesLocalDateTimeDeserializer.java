/*
 * Copyright (C) <Date> Republique et canton de Geneve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

