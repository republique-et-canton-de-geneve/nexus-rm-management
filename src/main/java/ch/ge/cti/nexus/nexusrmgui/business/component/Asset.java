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

package ch.ge.cti.nexus.nexusrmgui.business.component;

import ch.ge.cti.nexus.nexusrmgui.util.NexusServicesLocalDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Asset {

    private long fileSize;

    //@JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonDeserialize(using = NexusServicesLocalDateTimeDeserializer.class)
    private LocalDateTime lastModified;

    private String path;
}