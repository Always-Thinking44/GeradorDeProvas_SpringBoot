package com.prova.gerador_provas.config;

import com.prova.gerador_provas.enums.Role;
import com.prova.gerador_provas.model.AtribuicaoProfessor;
import com.prova.gerador_provas.model.Classe;
import com.prova.gerador_provas.model.Disciplina;
import com.prova.gerador_provas.model.Usuario;
import com.prova.gerador_provas.repository.AtribuicaoProfessorRepository;
import com.prova.gerador_provas.repository.ClasseRepository;
import com.prova.gerador_provas.repository.DisciplinaRepository;
import com.prova.gerador_provas.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final AtribuicaoProfessorRepository atribuicaoRepository;
    private final ClasseRepository classeRepository;
    private final DisciplinaRepository disciplinaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        inicializarUsuarios();
    }

    private void inicializarUsuarios() {
        // Criar Administrador padrão se não existir
        if (usuarioRepository.findByEmail("admin@escola.com").isEmpty()) {
            Usuario admin = Usuario.builder()
                    .nome("Administrador do Sistema")
                    .email("admin@escola.com")
                    .senha(passwordEncoder.encode("admin123"))
                    .role(Role.ROLE_ADMIN)
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info(">>> Usuário ADMIN criado com sucesso: admin@escola.com / admin123");
        }

        // Criar Professor de demonstração se não existir
        if (usuarioRepository.findByEmail("professor@escola.com").isEmpty()) {
            Usuario professor = Usuario.builder()
                    .nome("Professor Carlos Alberto")
                    .email("professor@escola.com")
                    .senha(passwordEncoder.encode("prof123"))
                    .role(Role.ROLE_PROFESSOR)
                    .ativo(true)
                    .build();
            usuarioRepository.save(professor);
            log.info(">>> Usuário PROFESSOR criado: professor@escola.com / prof123");

            // Se existirem classes e disciplinas já cadastradas, cria uma atribuição de exemplo
            Optional<Classe> primeiraClasse = classeRepository.findAll().stream().findFirst();
            Optional<Disciplina> primeiraDisciplina = disciplinaRepository.findAll().stream().findFirst();

            if (primeiraClasse.isPresent() && primeiraDisciplina.isPresent()) {
                AtribuicaoProfessor atribuicao = AtribuicaoProfessor.builder()
                        .professor(professor)
                        .classe(primeiraClasse.get())
                        .disciplina(primeiraDisciplina.get())
                        .build();
                atribuicaoRepository.save(atribuicao);
                log.info(">>> Atribuição inicial configurada para o Professor na turma {} e disciplina {}",
                        primeiraClasse.get().getNome(), primeiraDisciplina.get().getNome());
            }
        }
    }
}
