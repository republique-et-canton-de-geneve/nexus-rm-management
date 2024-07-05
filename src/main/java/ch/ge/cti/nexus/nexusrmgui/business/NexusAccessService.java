package ch.ge.cti.nexus.nexusrmgui.business;

import ch.ge.cti.nexus.nexusrmgui.WebClientProvider;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON;

/**
 * Classe d'appel aux services REST exposes par FormServices.
 */
@Service
@Slf4j
public class NexusAccessService {

    private static final String MESSAGE_ERREUR_404 =
            "Le serveur FormServices appelé par l'application est indisponible ou son URL est incorrecte";

    @Value("${app.security.token}")
    private String token;

    /**
     * Fournisseur d'un WebClient (client d'acces non bloquant) vers le serveur FormServices.
     */
    @Resource
    private WebClientProvider webClientProvider;

    /**
     * Appel a FormServices : rend les donnees metier XML d'une demande.
     * Note : dans FormServices, il faut que le role utilise' ait le droit "Archiver" et pas seulement le droit "Voir".
     * @return une chaine de caracteres contentant un &lt;dataStore&gt; et ses sous-elements
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
/******************************************************************************************************************************
*           !!! Temporaire !!! Classe a supprimer (utiliser pour le moment afin d'essayer le switch dans Application.java).
******************************************************************************************************************************/


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

    /**
     * Erreur lors de l'appel a FormServices.
     * Permet de traiter les cas ou l'erreur n'est ni une 4xx ni un 5xxx. Exemples : FormServices est indisponible ;
     * l'URL de FormServices est erronee.
     * Pas trouvé mieux qu'un bloc try/catch pour traiter ce cas,
     * cf. https://stackoverflow.com/questions/73989083/handling-server-unavailability-with-webclient.
     */
    private void handleInvocationError(RuntimeException exception) {
        log.error("Erreur lors de l'appel a Nexus :", exception);
    }

    void setWebClientProvider(WebClientProvider webClientProvider) {
        this.webClientProvider = webClientProvider;
    }

}
