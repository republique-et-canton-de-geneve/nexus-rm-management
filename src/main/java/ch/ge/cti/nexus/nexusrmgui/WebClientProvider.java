package ch.ge.cti.nexus.nexusrmgui;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;

/**
 * Un object en charge de creer un {@link WebClient}.
 * Permet, dans le cas des tests JUnit, d'injecter l'URL (formServiceUrl) d'un mock server.
 */
@Component
@Slf4j
public class WebClientProvider {

    private WebClient webClient;

    @Value("${app.formservices.url}")
    private String formServicesUrl;

    @Value("${app.formservices.response-timeout-milliseconds}")
    private int responseTimeout;

    @Value("${app.formservices.connection-timeout-milliseconds}")
    private int connectionTimeout;

    @Value("${javax.net.ssl.trustStore}")
    private String trustStorePath;

    @Value("${javax.net.ssl.trustStorePassword}")
    private String trustStorePassword;


    public WebClientProvider(
            String formServicesUrl,
            int responseTimeout,
            int connectionTimeout,
            String trustStorePath,       // inutile si sslEnabled = false
            String trustStorePassword,   // inutile si sslEnabled = false
            boolean sslEnabled
    )
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException
    {
        log.info("URL FormServices = {}", formServicesUrl);

        HttpClient httpClient;
        if (sslEnabled) {
            // cas general, cad hors tests JUnit
            var trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            try (FileInputStream inputStream = new FileInputStream(ResourceUtils.getFile(trustStorePath))){
                trustStore.load(inputStream, trustStorePassword.toCharArray());
            }
            var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            SslContext sslContext = SslContextBuilder
                    .forClient()
                    .trustManager(trustManagerFactory)
                    .build();
            httpClient = HttpClient.create()
                    .responseTimeout(Duration.ofMillis(responseTimeout))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
                    .secure(t -> t.sslContext(sslContext));
        } else {
            // cas d'un test JUnit
            // en court-circuitant SSL, on evite une erreur "IllegalArgumentException: Resource location must not be null"
            httpClient = HttpClient.create();
        }

        webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(formServicesUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10000000))  // FRMSRV-212
                .build();
    }


    public WebClient getWebClient() {
        return webClient;
    }

}
