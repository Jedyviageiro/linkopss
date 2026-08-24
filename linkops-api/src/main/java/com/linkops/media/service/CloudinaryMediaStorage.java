package com.linkops.media.service;

import com.linkops.common.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Component
public class CloudinaryMediaStorage implements CloudMediaStorage {

    private final RestClient restClient;
    private final String cloudName;
    private final String apiKey;
    private final String apiSecret;

    public CloudinaryMediaStorage(
            @Value("${linkops.media.cloudinary.cloud-name:}") String cloudName,
            @Value("${linkops.media.cloudinary.api-key:}") String apiKey,
            @Value("${linkops.media.cloudinary.api-secret:}") String apiSecret
    ) {
        this.restClient = RestClient.create();
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
        MultipartBodyBuilder body = new MultipartBodyBuilder();
        ByteArrayResource resource = new ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        body.part("file", resource).contentType(MediaType.parseMediaType(contentType));
        body.part("folder", folder);

        try {
            Map<String, Object> response = restClient.post()
                    .uri(
                            "https://api.cloudinary.com/v1_1/{cloudName}/image/upload",
                            cloudName
                    )
                    .headers(headers -> headers.setBasicAuth(apiKey, apiSecret))
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
