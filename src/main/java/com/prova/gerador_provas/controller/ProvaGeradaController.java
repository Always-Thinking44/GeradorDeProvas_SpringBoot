package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.dto.GerarProvaRequest;
import com.prova.gerador_provas.dto.ProvaGeradaResumo;
import com.prova.gerador_provas.model.ProvaGerada;
import com.prova.gerador_provas.service.PdfService;
import com.prova.gerador_provas.service.ProvaGeradaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/provas-geradas", "/api/prova_gerada"})
@RequiredArgsConstructor
public class ProvaGeradaController {

    private final ProvaGeradaService provaGeradaService;
    private final PdfService pdfService;

    @GetMapping
    public ResponseEntity<List<ProvaGerada>> getAllGeneratedExams() {
        return ResponseEntity.ok(provaGeradaService.findAll());
    }

    @GetMapping("/resumos")
    public ResponseEntity<List<ProvaGeradaResumo>> getAllResumos() {
        return ResponseEntity.ok(provaGeradaService.findAllResumo());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProvaGerada> getGeneratedExamById(@PathVariable Long id) {
        return ResponseEntity.ok(provaGeradaService.findById(id));
    }

    @PostMapping("/generate")
    public ResponseEntity<ProvaGerada> generateExam(@RequestBody GerarProvaRequest request) {
        ProvaGerada prova = provaGeradaService.generateExam(
                request.classeId(),
                request.disciplinaId(),
                request.trimestre(),
                request.modelo(),
                request.modeloProvaId(),
                request.anoLectivo()
        );
        return ResponseEntity.ok(prova);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGeneratedExam(@PathVariable Long id) {
        provaGeradaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadProvaPdf(@PathVariable Long id) {
        return pdfResponse(pdfService.renderProvaPdf(id, false), "prova-" + id + ".pdf");
    }

    @GetMapping("/{id}/gabarito/pdf")
    public ResponseEntity<byte[]> downloadGabaritoPdf(@PathVariable Long id) {
        return pdfResponse(pdfService.renderProvaPdf(id, true), "gabarito-" + id + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}
