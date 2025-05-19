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

package ch.ge.cti.nexus.nexusrmgui.business;

import ch.ge.cti.nexus.nexusrmgui.WebClientProvider;
import ch.ge.cti.nexus.nexusrmgui.business.certificate.Certificate;
import ch.ge.cti.nexus.nexusrmgui.business.component.ComponentResponse;
import ch.ge.cti.nexus.nexusrmgui.business.permission.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * REST requests to Nexus RM.
 */
@Service
@Slf4j
public class NexusAccessService {

    private static final String ERROR_MESSAGE_404 =
            "The Nexus server called by the application is unavailable or the URL is wrong";

    @Value("${app.security.token-base64}")
    private String token;

    /**
     *  Provider of a WebClient (non-blocking access client) to the Nexus server.
     */
    @Resource
    private WebClientProvider webClientProvider;

    /**
     * Call to NexusServices.
     */
    public Certificate[] getCertificats() {
        try {
            var uri = "/v1/security/ssl/truststore";
            return webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(Certificate[].class)
                    .block();
        } catch (RuntimeException e) {
            handleInvocationError(e);
            return null;
        }
    }

    /**
     * Call to NexusServices.
     */
    public ComponentResponse getComponents(String continuationToken) {
        try {
            var uri = "/v1/components?repository=project_release";
            if (! isEmpty(continuationToken)) {
                uri += "&continuationToken=" + continuationToken;
            }
            log.info("Request URL: " + uri);
            return webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(ComponentResponse.class)
                    .block();
        } catch (RuntimeException e) {
            log.error("Error during the call to get components: ", e);
            return null;
        }
    }

    public void deleteComponent(String componentId) {
        try {
            var uri = "/v1/components/" + componentId;
            webClientProvider.getWebClient()
                    .delete()
                    .uri(uri)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            log.info("Deleted component with ID {}", componentId);
        } catch (RuntimeException e) {
            log.error("Error during the call to delete component: ", e);
        }
    }

    /**
     * Call to NexusServices.
     */
    public List<User> getUsers() {
        try {
            var uri = "/v1/security/users";
            return Arrays.asList(Objects.requireNonNull(webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(User[].class)
                    .block()));
        } catch (RuntimeException e) {
            handleInvocationError(e);
            return Collections.emptyList();
        }
    }

    /**
     * Call to NexusServices.
     */
    public List<User> getUser(String userId) {
        try {
            var uri = "/v1/security/users?userId=" + userId;
            return Arrays.asList(Objects.requireNonNull(webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(User[].class)
                    .block()));
        } catch (RuntimeException e) {
            handleInvocationError(e);
            return Collections.emptyList();
        }
    }

    /**
     * Error during the call to NexusServices.
     * Handles cases where the error is neither a 4xx nor a 5xx. Examples: NexusServices is unavailable;
     * the URL of NexusServices is incorrect.
     * The best solution found is a try/catch block to handle this case,
     * see <a href="https://stackoverflow.com/questions/73989083/handling-server-unavailability-with-webclient">stackoverflow/handling-server-unavailability-with-webclient</a>.
     */
    private void handleInvocationError(RuntimeException exception) {
        log.error("Error during the call to Nexus :", exception);
    }

    void setWebClientProvider(WebClientProvider webClientProvider) {
        this.webClientProvider = webClientProvider;
    }

}
