package com.prova.gerador_provas.service;

import com.lowagie.text.DocumentException;
import com.prova.gerador_provas.enums.ModeloTemplate;
import com.prova.gerador_provas.model.ProvaGerada;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class PdfService {

    private final ProvaGeradaService provaGeradaService;
    private final SpringTemplateEngine pdfTemplateEngine;

    public PdfService(ProvaGeradaService provaGeradaService) {
        this.provaGeradaService = provaGeradaService;

        // Engine dedicada aos templates de prova (exames/modelo1-3.html).
        // Não é um @Bean de Spring: um bean de SpringTemplateEngine faria o
        // Thymeleaf auto-configurado "recuar", quebrando todas as páginas web.
        // O TemplateMode.XML garante HTML bem-formado para o Flying Saucer (PDF).
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/exames/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.XML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        this.pdfTemplateEngine = new SpringTemplateEngine();
        this.pdfTemplateEngine.setTemplateResolver(resolver);
    }

    public byte[] renderProvaPdf(Long provaId, boolean gabarito) {
        ProvaGerada prova = provaGeradaService.findById(provaId);

        Map<String, Object> contexto = provaGeradaService.buildContext(prova, gabarito, true);

        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(contexto);

        String template = templatePath(prova.getTemplate());
        String html = pdfTemplateEngine.process(template, thymeleafContext);

        return htmlToPdf(html);
    }

    private String templatePath(ModeloTemplate template) {
        return template == null ? "modelo1" : template.fileName();
    }

    private byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.getSharedContext().setPrint(true);
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("Erro ao gerar o PDF da prova.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Erro ao gerar o PDF da prova: " + e.getMessage(), e);
        }
    }
}
