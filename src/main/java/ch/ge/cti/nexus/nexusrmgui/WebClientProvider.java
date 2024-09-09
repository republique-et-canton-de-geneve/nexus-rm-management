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

package ch.ge.cti.nexus.nexusrmgui;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;

import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.IOException;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import java.time.Duration;

/**
 * An object responsible for creating a {@link WebClient}.
 * Allows, in the case of JUnit tests, to inject the URL (nexusServiceUrl) of a mock server.
 */
@Component
@Slf4j
public class WebClientProvider {

    private WebClient webClient;


    public WebClientProvider(

            @Value("${app.nexusservices.url}")
            String nexusServicesUrl,

            @Value("${app.nexusservices.response-timeout-milliseconds}")
            int responseTimeout,

            @Value("${app.nexusservices.connection-timeout-milliseconds}")
            int connectionTimeout,

            @Value("${javax.net.ssl.trustStore}")
            String trustStorePath,       // useless if sslEnabled = false

            @Value("${javax.net.ssl.trustStorePassword}")
            String trustStorePassword,   // useless if sslEnabled = false

            @Value("${app.nexusservices.ssl.enabled}")
            boolean sslEnabled
    )
            throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException
    {
        log.info("URL NexusServices = {}", nexusServicesUrl);

        HttpClient httpClient;
        if (sslEnabled) {
            // in general, i.e., outside of JUnit tests
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
            // in a JUnit test
            // by bypassing SSL, we avoid an "IllegalArgumentException: Resource location must not be null" error
            httpClient = HttpClient.create();
        }

        webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(nexusServicesUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10000000))
                .build();
    }


    public WebClient getWebClient() {
        return webClient;
    }

}
