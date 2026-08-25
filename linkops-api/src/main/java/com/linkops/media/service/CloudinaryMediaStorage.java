package com.linkops.media.service;

import com.linkops.common.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class CloudinaryMediaStorage implements CloudMediaStorage {

    private final RestClient restClient;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryMediaStorage(
            @Value("${linkops.media.cloudinary.cloud-name:}") String cloudName,
            @Value("${linkops.media.cloudinary.api-key:}") String apiKey,
            @Value("${linkops.media.cloudinary.api-secret:}") String apiSecret,
            @Value("${linkops.media.cloudinary.connect-timeout:5s}") Duration connectTimeout,
            @Value("${linkops.media.cloudinary.read-timeout:30s}") Duration readTimeout
    ) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .build();
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);
        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
        this.cloudName = cloudName;
        this.apiKey = apiKey;
        this.apiSecret = apiSecret;
    }

    @Override
    public StoredMedia upload(
            byte[] content,
            String contentType,
            String filename,
            String folder
    ) {
        ensureConfigured();
        long timestamp = Instant.now().getEpochSecond();
        String signature = sign(folder, timestamp);
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        body.part("file", resource).contentType(MediaType.parseMediaType(contentType));
        body.part("folder", folder);
        body.part("api_key", apiKey);
        body.part("timestamp", Long.toString(timestamp));
        body.part("signature", signature);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(
                            "https://api.cloudinary.com/v1_1/{cloudName}/image/upload",
                            cloudName
                    )
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body.build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            String url = response == null ? null : (String) response.get("secure_url");
            if (!StringUtils.hasText(url)) {
                throw new ServiceUnavailableException(
                        "O armazenamento de imagens devolveu uma resposta inválida."
                );
            }
            return new StoredMedia(url, contentType, content.length);
        } catch (RestClientException exception) {
            throw new ServiceUnavailableException(
                    "Não foi possível guardar a imagem. Tente novamente mais tarde.",
                    exception
            );
        }
    }

    private String sign(String folder, long timestamp) {
        String value = "folder=" + folder + "&timestamp=" + timestamp + apiSecret;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-1")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "O algoritmo necessário para assinar o upload não está disponível.",
                    exception
            );
        }
    }

    private void ensureConfigured() {
        if (!StringUtils.hasText(cloudName)
                || !StringUtils.hasText(apiKey)
                || !StringUtils.hasText(apiSecret)) {
            throw new ServiceUnavailableException(
                    "O armazenamento de imagens ainda não está configurado."
            );
        }
    }
}
