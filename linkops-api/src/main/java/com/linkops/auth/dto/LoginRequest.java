package com.linkops.auth.dto;

import com.linkops.common.validation.BcryptCompatible;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Email
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @NotBlank
        @Size(max = 72, message = "A palavra-passe deve ter no máximo 72 caracteres.")
        @BcryptCompatible
        String password
) {
}
