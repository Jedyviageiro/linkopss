package com.linkops.user.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres.")
        @Pattern(regexp = ".*\\S.*", message = "O nome não pode estar vazio.")
        String firstName,

        @Size(max = 100, message = "O apelido deve ter no máximo 100 caracteres.")
        @Pattern(regexp = ".*\\S.*", message = "O apelido não pode estar vazio.")
        String lastName,

        @Size(max = 50, message = "O telefone deve ter no máximo 50 caracteres.")
        String phone
) {
}
