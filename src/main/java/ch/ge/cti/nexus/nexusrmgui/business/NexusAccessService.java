package ch.ge.cti.nexus.nexusrmgui.business;

import ch.ge.cti.nexus.nexusrmgui.WebClientProvider;
import ch.ge.cti.nexus.nexusrmgui.business.certificate.Certificate;
import ch.ge.cti.nexus.nexusrmgui.business.component.ComponentResponse;
import ch.ge.cti.nexus.nexusrmgui.business.permission.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * REST requests to Nexus.
 */
@Service
@Slf4j
public class NexusAccessService {

    private static final String ERROR_MESSAGE_404 =
            "The Nexus server called by the application is unavailable or the URL is wrong";

    @Value("${app.security.token}")
    private String token;

    /**
     *  Provider of a WebClient (non-blocking access client) to the Nexus server.
     */
    @Resource
    private WebClientProvider webClientProvider;

    /**
     * Call to NexusServices
     * @return Certificates
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
     * Call to NexusServices
     * @return Components
     */

    public ComponentResponse getComponents(String continuationToken) {
        try {
            var uri = "/v1/components?repository=project_release";
            if (continuationToken != null && !continuationToken.isEmpty()) {
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
     * Call to NexusServices
     * @return Permissions
     */
    public List<Permission> getPermissions() {
        try {
            var uri = "/v1/security/users";
            return Arrays.asList(Objects.requireNonNull(webClientProvider.getWebClient()
                    .get()
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()
                    .bodyToMono(Permission[].class)
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