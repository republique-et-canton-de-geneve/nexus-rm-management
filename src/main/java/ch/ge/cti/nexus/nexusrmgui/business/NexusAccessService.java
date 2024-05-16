package ch.ge.cti.nexus.nexusrmgui.business;


import ch.ge.cti.nexus.nexusrmgui.WebClientProvider;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.BodyInserters;
//import org.springframework.web.reactive.function.client.ClientResponse;
//import org.springframework.web.reactive.function.client.WebClientRequestException;
//import reactor.core.publisher.Mono;

import javax.annotation.Resource;

//import ch.ge.ael.integration.marshalling.formservices.File;
//import ch.ge.ael.integration.marshalling.formservices.FileForWorkflowStatusUpdate;
//import ch.ge.ael.integration.marshalling.formservices.UserContext;
//import ch.ge.ael.integration.marshalling.formservices.WorkflowStatus;
//import ch.ge.ael.integration.v1.business.exception.ClientException;
//import ch.ge.ael.integration.v1.business.exception.FormServicesException;
//import ch.ge.ael.integration.v1.business.exception.MediationException;
//import ch.ge.ael.integration.v1.business.exception.NoSuchDemandeException;
//import ch.ge.ael.integration.v1.business.exception.NoSuchPrestationException;
//import ch.ge.ael.integration.v1.business.http.WebClientProvider;
//import static ch.ge.ael.integration.marshalling.formservices.WorkflowStatus.SOUMIS;
//import static ch.ge.ael.integration.marshalling.formservices.WorkflowStatus.TRANSMIS_BO;
//import static ch.ge.ael.integration.v1.business.http.Header.GINA_FULLNAME;
//import static ch.ge.ael.integration.v1.business.http.Header.REMOTE_USER;
//import static ch.ge.ael.integration.v1.business.http.Header.GINA_ROLES;
//import static ch.ge.ael.integration.v1.business.http.Header.GINAUSER;
//import static ch.ge.ael.integration.v1.business.util.Utils.NB_DEMANDES_PAR_PAGE;
import static org.springframework.http.HttpMethod.GET;
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
     * Appel a FormServices : rend les metadonnees d'une demande.
     */
//    public File getFile(String uuidDemande, String username, String roles) {
//        try {
//            val uri = "/rest/file/" + uuidDemande;
//            val method = GET;
//            logCallToFormServices(uri, method, username,roles);
//            return webClientProvider.getWebClient()
////                    .method(method)    // fait planter en NPE un test unitaire...
//                    .get()
//                    .uri(uri)
//                    .accept(APPLICATION_JSON)
//                    .header(REMOTE_USER, username)
//                    .header(GINAUSER, username)
//                    .header(GINA_FULLNAME, username)
//                    .header(GINA_ROLES, roles)
//                    .retrieve()      // appel a FormServices
//                    .onStatus(HttpStatus::is4xxClientError, demandeErrorHandler)
//                    .onStatus(HttpStatus::is5xxServerError, serveurErrorHandler)
//                    .bodyToMono(File.class)
//                    .block();
//        } catch (WebClientRequestException e) {
//            handleInvocationError(e, false);
//            return null;
//        }
//    }

    /**
     * Appel a FormServices : rend les donnees metier XML d'une demande.
     * Note : dans FormServices, il faut que le role utilise' ait le droit "Archiver" et pas seulement le droit "Voir".
     * @return une chaine de caracteres contentant un &lt;dataStore&gt; et ses sous-elements
     */
    public String getCertificats() {
        try {
            var uri = "/rest/v1/security/ssl/truststore"; // "/rest/file/" + uuidDemande + "/data";
            val method = GET;
            return webClientProvider.getWebClient()
                    .method(method)
                    .uri(uri)
                    .accept(APPLICATION_JSON)
                    .header("Authorization", "Basic " + token)
                    .retrieve()      // appel a FormServices
            //        .onStatus(HttpStatus::is4xxClientError, demandeErrorHandler)
            //        .onStatus(HttpStatus::is5xxServerError, serveurErrorHandler)
                    .bodyToMono(String.class)
                    .block();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Appel a FormServices : rend un fichier d'une piece jointe.
     * A la difference des autres methodes de cette classe, cette methode-ci est non bloquante.
     * @param uuidDemande UUID de la demande
     * @param uuidPieceJointe UUID de la piece jointe (attachment)
     * @param index numero du fichier (order) : 1 pour le premier fichier de la piece jointe, 2 pour le deuxieme, etc.
     * @return un flux contenant le fichier
     */
//    public ResponseEntity<byte[]> getAttachmentFile(
//            String uuidDemande,
//            String uuidPieceJointe,
//            int index,
//            String username,
//            String roles) {
//        try {
//            val uri = "/rest/document/ds/" + uuidDemande + "/attachment/" + uuidPieceJointe + "/file/" + index;
//            val method = GET;
//            logCallToFormServices(uri, method, username, roles);
//            return webClientProvider.getWebClient()
//                    .method(method)
//                    .uri(uri)
//                    .accept(APPLICATION_OCTET_STREAM)
//                    .header(REMOTE_USER, username)
//                    .header(GINAUSER, username)
//                    .header(GINA_FULLNAME, username)
//                    .header(GINA_ROLES, roles)
//                    .retrieve()      // appel a FormServices
//                    .onStatus(HttpStatus::is4xxClientError, demandeErrorHandler)
//                    .onStatus(HttpStatus::is5xxServerError, serveurErrorHandler)
//                    .toEntity(byte[].class)
//                    .block();
//        } catch (RuntimeException e) {
//            handleInvocationError(e,  false);
//            return null;
//        }
//    }

    /**
     * Appel a FormServices : rend les demandes a l'etat "soumis" ou "transmis_bo".
     * @param prestation identifiant de la prestation
     * @param page numero du fragment demande' (1 = les 150 demandes les plus recentes)
     * @return une liste de demandes (demande completes, pas juste les identifiants).
     *         Vide si la prestation n'existe pas ou n'est pas accessible pour l'utilisateur.
     *         Vide si la page demandee ne contient aucune demande
     */
//    public File[] getFiles(String prestation, int page, String username, String roles) {
//        try {
//            int first = (page - 1) * NB_DEMANDES_PAR_PAGE + 1;
//            val uri = "/rest/file/managed"
//                    + "?application.name=" + prestation
//                    + "&first=" + first
//                    + "&max=" + NB_DEMANDES_PAR_PAGE
//                    + "&order=stepDate"
//                    + "&workflowStatus=" + SOUMIS + "," + TRANSMIS_BO;
//            val method = GET;
//            logCallToFormServices(uri, method, username, roles);
//            return webClientProvider.getWebClient()
//                    .method(method)
//                    .uri(uri)
//                    .accept(APPLICATION_JSON)
//                    .header(REMOTE_USER, username)
//                    .header(GINAUSER, username)
//                    .header(GINA_ROLES, roles)
//                    .header(GINA_FULLNAME, username)
//                    .retrieve()      // appel a FormServices
//                    .onStatus(HttpStatus::is4xxClientError, prestationErrorHandler)
//                    .onStatus(HttpStatus::is5xxServerError, serveurErrorHandler)
//                    .bodyToMono(File[].class)
//                    .block();
//        } catch (RuntimeException e) {
//            handleInvocationError(e, false);
//            return new File[]{};
//        }
//    }

    /**
     * Appel a FormServices : fait passer une demande dans un autre etat.
     * @param uuidDemande UUID de la demande
     * @param status nouvel etat de la demande
     */
//    public void performTransition(String uuidDemande, WorkflowStatus status, String idCourt, String username, String roles) {
//        try {
//            FileForWorkflowStatusUpdate body = FileForWorkflowStatusUpdate.builder()
//                    .workflowStatus(status)
//                    .name(idCourt)   // FRMSRV-203 : il faut renvoyer le name, sinon le PUT va l'effacer en base
//                    .build();
//            val uri = "/rest/alpha/file/" + uuidDemande;
//            val method = PUT;
//            logCallToFormServices(uri + " avec contenu [" + body + "]", method, username, roles);
//            webClientProvider.getWebClient()
//                    .method(method)
//                    .uri(uri)
//                    .accept(APPLICATION_JSON)
//                    .contentType(APPLICATION_JSON)
//                    .header(REMOTE_USER, username)
//                    .header(GINAUSER, username)
//                    .header(GINA_FULLNAME, username)
//                    .header(GINA_ROLES, roles)
//                    .body(BodyInserters.fromValue(body))
//                    .retrieve()      // appel a FormServices
//                    .onStatus(HttpStatus::is4xxClientError, prestationErrorHandler)
//                    .onStatus(HttpStatus::is5xxServerError, serveurErrorHandler)
//                    .bodyToMono(String.class)
//                    .block();
//        } catch (RuntimeException e) {
//            handleInvocationError(e, false);
//        }
//    }

    /**
     * Appel a FormServices : rend les donnees d'un utilisateur.
     * Cette methode est destinee a etre utilisee par une sonde de disponibilite de l'application.
     */
//    public UserContext authenticate(String username, String roles) {
//        try {
//            val uri = "/rest/auth/me";
//            val method = GET;
//            logCallToFormServices(uri, method, username, roles);
//            return webClientProvider.getWebClient()
//                    .method(method)
//                    .uri(uri)
//                    .accept(APPLICATION_JSON)
//                    .header(REMOTE_USER, username)
//                    .header(GINAUSER, username)
//                    .header(GINA_FULLNAME, username)
//                    .header(GINA_ROLES, roles)
//                    .header("ginauser", username)
//                    .retrieve()      // appel a FormServices
//                    .onStatus(HttpStatus::is4xxClientError, authenticateErrorHandler)
//                    .onStatus(HttpStatus::is5xxServerError, serveurErrorHandler)
//                    .bodyToMono(UserContext.class)
//                    .block();
//        } catch (RuntimeException e) {
//            handleInvocationError(e, false);
//            return null;
//        }
//    }

    /**
     * Erreur lors de l'appel a FormServices pour une demande.
     * Cas ou l'erreur est due a l'appelant (mediation ou client de la mediation).
     */
//    private final Function<ClientResponse, Mono<? extends Throwable>> demandeErrorHandler = response -> {
//        RuntimeException e;
//
//        if (response.statusCode() == FORBIDDEN) {
//            // 403
//            e = new NoSuchDemandeException();
//        } else if (response.statusCode() == NOT_FOUND) {
//            // 404
//            String msg = MESSAGE_ERREUR_404;
//            e = new MediationException(msg);
//        } else {
//            // autre erreur 4xx
//            String msg = "Erreur " + response.statusCode() + " due a la mediation lors de l'appel a FormServices";
//            e = new MediationException(msg);
//        }
//
//        response.toEntity(String.class)
//                .subscribe(entity -> log.error("Erreur due a la mediation (ou au client) lors de l'appel a FormServices : {}", entity)
//        );
//
//        return Mono.error(e);
//    };

    /**
     * Erreur lors de l'appel a FormServices pour une authentification (/auth/me).
     * Cas ou l'erreur est due a l'appelant (mediation ou client de la mediation).
     */
//    private final Function<ClientResponse, Mono<? extends Throwable>> authenticateErrorHandler = response -> {
//        RuntimeException e;
//
//        if (response.statusCode() == FORBIDDEN) {
//            // 403
//            e = new MediationException("Acces interdit au serveur FormServices");
//        } else if (response.statusCode() == NOT_FOUND) {
//            // 404
//            String msg = MESSAGE_ERREUR_404;
//            e = new MediationException(msg);
//        } else {
//            // autre erreur 4xx
//            String msg = "Erreur " + response.statusCode() + " lors de l'appel a FormServices";
//            e = new MediationException(msg);
//        }
//
//        response.toEntity(String.class)
//                .subscribe(entity -> log.error("Erreur due a la mediation (ou au client) lors de l'appel a FormServices : {}", entity)
//        );
//
//        return Mono.error(e);
//    };

    /**
     * Erreur lors de l'appel a FormServices pour une prestation.
     * Cas ou l'erreur est due a l'appelant (mediation ou client de la mediation).
     */
//    private final Function<ClientResponse, Mono<? extends Throwable>> prestationErrorHandler = response -> {
//        RuntimeException e;
//
//        if (response.statusCode() == FORBIDDEN) {
//            // 403
//            e = new NoSuchPrestationException();
//        } else if (response.statusCode() == NOT_FOUND) {
//            // 404
//            String msg = MESSAGE_ERREUR_404;
//            e = new MediationException(msg);
//        } else {
//            // autre erreur 4xx
//            String msg = "Erreur " + response.statusCode() + " due a la mediation lors de l'appel a FormServices";
//            e = new MediationException(msg);
//        }
//
//        response.toEntity(String.class)
//                .subscribe(entity -> log.error(
//                        "Erreur due a la mediation ou au client lors de l'appel a FormServices : {}", entity)
//        );
//
//        return Mono.error(e);
//    };

    /**
     * Erreur lors de l'appel a FormServices. Cas ou l'erreur est due a FormServices.
     */
//    private final Function<ClientResponse, Mono<? extends Throwable>> serveurErrorHandler = response -> {
//        response.toEntity(String.class)
//                .subscribe(entity -> log.info("Erreur interne de FormServices lors de l'appel a FormServices : {}", entity)
//        );
//
//        return Mono.error(new FormServicesException("" + response.statusCode()));
//    };

    /**
     * Erreur lors de l'appel a FormServices.
     * Permet de traiter les cas ou l'erreur n'est ni une 4xx ni un 5xxx. Exemples : FormServices est indisponible ;
     * l'URL de FormServices est erronee.
     * Pas trouvé mieux qu'un bloc try/catch pour traiter ce cas,
     * cf. https://stackoverflow.com/questions/73989083/handling-server-unavailability-with-webclient.
     */
//    private void handleInvocationError(RuntimeException exception, boolean skipTrace) {
//        if (!skipTrace) {
//            log.error("Erreur lors de l'appel a FormServices :", exception);
//        }
//
//        if (exception instanceof MediationException
//                || exception instanceof ClientException
//                || exception instanceof FormServicesException) {
//            // cas d'une erreur 4xx ou 5xx deja traitee par un des handlers
//            throw exception;
//        } else if (exception instanceof WebClientRequestException
//                && exception.getCause() != null
//                && exception.getCause() instanceof UnknownHostException) {
//            // cas d'un appel qui n'arrive meme pas au serveur FormServices
//            throw new MediationException("Connexion impossible au serveur FormServices");
//        } else if (exception instanceof WebClientRequestException
//                && exception.getCause() != null) {
//            // autre cas (avec cause dans une WebClientRequestException)
//            throw new MediationException("Erreur non repertoriee lors de l'acces au serveur FormServices ("
//                    + exception.getCause() + ")");
//        } else {
//            // autre cas
//            throw new MediationException("Erreur non repertoriee lors de l'acces au serveur FormServices ("
//                    + exception + ")");
//        }
//    }
//
    private void logCallToFormServices(String url, HttpMethod method, String username, String roles) {
        log.info("Appel sortant a FormServices a l'URL {} '{}' ({}) [{}]", method, url, username, roles);
    }

    void setWebClientProvider(WebClientProvider webClientProvider) {
        this.webClientProvider = webClientProvider;
    }

}
