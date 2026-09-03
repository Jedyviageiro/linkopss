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

        @Size(max = 9, message = "O telefone deve ter 9 dígitos.")
        @Pattern(
                regexp = "^$|^(?:82|83|84|85|86|87)\\d{7}$",
                message = "Informe um número móvel moçambicano válido (mCel/Tmcel, Vodacom ou Movitel)."
        )
        String phone
) {
}
