package com.prova.gerador_provas.dto;

import com.prova.gerador_provas.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterUsuarioRequest(
        @NotBlank(message = "O nome é obrigatório")
        @Size(min = 2, max = 100)
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Email inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres")
        String senha,

        @NotNull(message = "O perfil (role) é obrigatório")
        Role role
) {}
