package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.dto.LoginRequest;
import com.prova.gerador_provas.dto.LoginResponse;
import com.prova.gerador_provas.dto.RegisterUsuarioRequest;
import com.prova.gerador_provas.dto.UsuarioResponse;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.service.JwtService;
import com.prova.gerador_provas.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        Usuario usuario = usuarioService.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuário ou senha incorretos."));

        String token = jwtService.generateToken(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNome(),
                usuario.getRole().name()
        );

        UsuarioResponse userResp = usuarioService.toResponse(usuario);

        return ResponseEntity.ok(new LoginResponse(
                token,
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getRole(),
                userResp.atribuicoes()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> getMe() {
        Usuario usuario = usuarioService.getUsuarioAutenticado();
        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(usuarioService.toResponse(usuario));
    }
}
