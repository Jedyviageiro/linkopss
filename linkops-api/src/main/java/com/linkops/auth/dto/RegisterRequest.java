package com.linkops.auth.dto;

import com.linkops.common.validation.BcryptCompatible;
import com.linkops.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para criação de uma conta de cliente ou prestador")
public record RegisterRequest(
        @NotBlank
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        String firstName,

        @NotBlank
        @Size(max = 100, message = "O apelido deve ter no máximo 100 caracteres.")
        String lastName,

        @NotBlank
        @Email
        @Size(max = 255, message = "O e-mail deve ter no máximo 255 caracteres.")
        String email,

        @Size(max = 50, message = "O telefone deve ter no máximo 50 caracteres.")
        String phone,

        @NotBlank
        @Size(min = 8, max = 72, message = "A palavra-passe deve ter entre 8 e 72 caracteres.")
        @BcryptCompatible
        String password,

        @NotNull
        UserRole role
) {
}
