package com.prova.gerador_provas.service;

import com.prova.gerador_provas.dto.AtribuicaoRequest;
import com.prova.gerador_provas.dto.AtribuicaoResponse;
import com.prova.gerador_provas.dto.RegisterUsuarioRequest;
import com.prova.gerador_provas.dto.UsuarioResponse;
import com.prova.gerador_provas.enums.Role;
import com.prova.gerador_provas.model.AtribuicaoProfessor;
import com.prova.gerador_provas.model.Classe;
import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.repository.AtribuicaoProfessorRepository;
import com.prova.gerador_provas.repository.ClasseRepository;
import com.prova.gerador_provas.repository.DisciplinaRepository;
import com.prova.gerador_provas.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AtribuicaoProfessorRepository atribuicaoRepository;
    private final ClasseRepository classeRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> findAllProfessores() {
        return usuarioRepository.findByRole(Role.ROLE_PROFESSOR).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado."));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse findResponseById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public UsuarioResponse create(RegisterUsuarioRequest req) {
        if (usuarioRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Já existe um usuário com este e-mail.");
        }

        Usuario usuario = Usuario.builder()
                .nome(req.nome())
                .email(req.email())
                .senha(passwordEncoder.encode(req.senha()))
                .role(req.role() != null ? req.role() : Role.ROLE_PROFESSOR)
                .ativo(true)
                .build();

        return toResponse(usuarioRepository.save(usuario));
    }

    public UsuarioResponse update(Long id, RegisterUsuarioRequest req) {
        Usuario usuario = findById(id);

        if (!usuario.getEmail().equalsIgnoreCase(req.email()) && usuarioRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Já existe outro usuário cadastrado com este e-mail.");
        }

        usuario.setNome(req.nome());
        usuario.setEmail(req.email());
        if (req.senha() != null && !req.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(req.senha()));
        }
        if (req.role() != null) {
            usuario.setRole(req.role());
        }

        return toResponse(usuarioRepository.save(usuario));
    }

    public void setAtivo(Long id, boolean ativo) {
        Usuario usuario = findById(id);
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
    }

    public void delete(Long id) {
        // Soft delete para proteger dados
        setAtivo(id, false);
    }

    /* ── Atribuições do Professor (Classes e Disciplinas permitidas) ── */

    public AtribuicaoResponse atribuirProfessor(AtribuicaoRequest req) {
        Usuario professor = findById(req.professorId());
        if (professor.getRole() != Role.ROLE_PROFESSOR) {
            throw new IllegalArgumentException("Apenas usuários com perfil de Professor podem receber atribuições de turmas.");
        }

        Classe classe = classeRepository.findById(req.classeId())
                .orElseThrow(() -> new EntityNotFoundException("Classe não encontrada."));

        Disciplina disciplina = disciplinaRepository.findById(req.disciplinaId())
                .orElseThrow(() -> new EntityNotFoundException("Disciplina não encontrada."));

        if (atribuicaoRepository.existsByProfessorIdAndClasseIdAndDisciplinaId(
                professor.getId(), classe.getId(), disciplina.getId())) {
            throw new IllegalArgumentException("Esta disciplina já está atribuída a este professor para esta classe.");
        }

        AtribuicaoProfessor at = AtribuicaoProfessor.builder()
                .professor(professor)
                .classe(classe)
                .disciplina(disciplina)
                .build();

        AtribuicaoProfessor salvo = atribuicaoRepository.save(at);
        return toAtribuicaoResponse(salvo);
    }

    public void removerAtribuicao(Long atribuicaoId) {
        if (!atribuicaoRepository.existsById(atribuicaoId)) {
            throw new EntityNotFoundException("Atribuição não encontrada.");
        }
        atribuicaoRepository.deleteById(atribuicaoId);
    }

    @Transactional(readOnly = true)
    public List<AtribuicaoResponse> findAtribuicoesByProfessor(Long professorId) {
        return atribuicaoRepository.findByProfessorId(professorId).stream()
                .map(this::toAtribuicaoResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public boolean professorTemAcesso(Usuario usuario, Long classeId, Long disciplinaId) {
        if (usuario.getRole() == Role.ROLE_ADMIN) {
            return true; // Admin tem acesso a tudo
        }
        return atribuicaoRepository.existsByProfessorIdAndClasseIdAndDisciplinaId(
                usuario.getId(), classeId, disciplinaId);
    }

    @Transactional(readOnly = true)
    public Usuario getUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return usuarioRepository.findByEmail(auth.getName()).orElse(null);
    }

    /* ── Mappers ── */

    public UsuarioResponse toResponse(Usuario u) {
        List<AtribuicaoResponse> atribuicoes;
        if (u.getId() != null) {
            // Busca segura via repository que não dispara LazyInitializationException
            atribuicoes = atribuicaoRepository.findByProfessorId(u.getId()).stream()
                    .map(this::toAtribuicaoResponse)
                    .toList();
        } else {
            atribuicoes = List.of();
        }

        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getRole(),
                u.getAtivo(),
                u.getCriadoEm(),
                atribuicoes
        );
    }

    public AtribuicaoResponse toAtribuicaoResponse(AtribuicaoProfessor at) {
        return new AtribuicaoResponse(
                at.getId(),
                at.getProfessor().getId(),
                at.getProfessor().getNome(),
                at.getClasse().getId(),
                at.getClasse().getNome(),
                at.getDisciplina().getId(),
                at.getDisciplina().getNome()
        );
    }
}
