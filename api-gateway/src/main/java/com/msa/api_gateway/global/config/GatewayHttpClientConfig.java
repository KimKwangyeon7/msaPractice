//package com.msa.api_gateway.global.config;
//
//import io.netty.handler.ssl.SslContextBuilder;
//import org.springframework.boot.web.reactive.function.client.WebClientCustomizer;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import reactor.netty.http.client.HttpClient;
//
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLException;
//import javax.net.ssl.TrustManagerFactory;
//import java.io.FileInputStream;
//import java.security.KeyStore;
//
//@Configuration
//public class GatewayHttpClientConfig {
//
//    @Bean
//    public WebClientCustomizer webClientCustomizer() {
//        return webClientBuilder -> {
//            try {
//                // Load truststore
//                KeyStore trustStore = KeyStore.getInstance("JKS");
//                try (FileInputStream trustStoreStream = new FileInputStream("src/main/resources/gateway_truststore.jks")) {
//                    trustStore.load(trustStoreStream, "8llow8llowme".toCharArray());
//                }
//
//                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
//                trustManagerFactory.init(trustStore);
//
//                // Configure SSL context
//                HttpClient httpClient = HttpClient.create()
//                        .secure(sslContextSpec -> {
//                            try {
//                                sslContextSpec.sslContext(SslContextBuilder.forClient()
//                                        .trustManager(trustManagerFactory)
//                                        .build());
//                            } catch (SSLException e) {
//                                throw new RuntimeException(e);
//                            }
//                        });
//
//                // Apply to WebClient
//                webClientBuilder.clientConnector(new ReactorClientHttpConnector(httpClient));
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to configure SSL for WebClient", e);
//            }
//        };
//    }
//}
