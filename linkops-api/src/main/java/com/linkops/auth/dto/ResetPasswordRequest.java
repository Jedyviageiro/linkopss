package com.linkops.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.linkops.common.validation.BcryptCompatible;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Token e nova palavra-passe")
public record ResetPasswordRequest(
        @NotBlank
        @Size(min = 43, max = 43, message = "O token é inválido.")
        @Pattern(regexp = "^[A-Za-z0-9_-]{43}$", message = "O token é inválido.")
        String token,

        @NotBlank
        @Size(min = 8, max = 72, message = "A palavra-passe deve ter entre 8 e 72 caracteres.")
        @BcryptCompatible
        String password,

        @NotBlank
        @Size(min = 8, max = 72, message = "A confirmação da palavra-passe deve ter entre 8 e 72 caracteres.")
        String confirmPassword
) {
    @JsonIgnore
    @AssertTrue(message = "A palavra-passe e a confirmação devem ser iguais.")
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}
