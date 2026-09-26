package com.astra.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception {

        ClassPathResource resource =
                new ClassPathResource("certs/http_ca.crt");

        // Read the PEM/CRT certificate.
        CertificateFactory certificateFactory =
                CertificateFactory.getInstance("X.509");

        Certificate certificate;

        try (InputStream inputStream = resource.getInputStream()) {
            certificate = certificateFactory.generateCertificate(inputStream);
        }

        // Create an in-memory trust store and add the CA certificate.
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

        trustStore.load(null, null);

        trustStore.setCertificateEntry(
                "elasticsearch-ca",
                certificate
        );

        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(trustStore, null)
                .build();

        BasicCredentialsProvider credentialsProvider =
                new BasicCredentialsProvider();

        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "https")
        );

        builder.setHttpClientConfigCallback(httpClientBuilder ->
                httpClientBuilder
                        .setDefaultCredentialsProvider(credentialsProvider)
                        .setSSLContext(sslContext)
        );

        RestClient restClient = builder.build();

        ElasticsearchTransport transport =
                new RestClientTransport(
                        restClient,
                        new JacksonJsonpMapper()
                );

        return new ElasticsearchClient(transport);
    }
}
