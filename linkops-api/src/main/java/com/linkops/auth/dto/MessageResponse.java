package com.linkops.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem de resultado")
public record MessageResponse(String message) {
}
