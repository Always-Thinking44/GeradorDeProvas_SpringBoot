package com.prova.gerador_provas.controller;

import com.prova.gerador_provas.dto.GerarProvaRequest;
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
@RequestMapping("/api/prova_gerada")
@RequiredArgsConstructor
public class ProvaGeradaController {

    private final ProvaGeradaService provaGeradaService;
    private final PdfService pdfService;

    @GetMapping
    public List<ProvaGerada> getAllGeneratedExams() {
        return provaGeradaService.findAll();
    }

    @GetMapping("/{id}")
    public ProvaGerada getGeneratedExamById(
            @PathVariable Long id) {
        return provaGeradaService.findById(id);
    }

    @PostMapping("/generate")
    public ProvaGerada generateExam(
            @RequestBody GerarProvaRequest request) {

        return provaGeradaService.generateExam(
                request.classeId(),
                request.disciplinaId(),
                request.trimestre(),
                request.modelo()
        );
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadProvaPdf(
            @PathVariable Long id) {
        return pdfResponse(pdfService.renderProvaPdf(id, false), "prova-" + id + ".pdf");
    }

    @GetMapping("/{id}/gabarito/pdf")
    public ResponseEntity<byte[]> downloadGabaritoPdf(
            @PathVariable Long id) {
        return pdfResponse(pdfService.renderProvaPdf(id, true), "gabarito-" + id + ".pdf");
    }

    private ResponseEntity<byte[]> pdfResponse(byte[] bytes, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(bytes);
    }
}
