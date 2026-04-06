package com.github.artsal.ai.assistant.backend.rag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/docs")
@CrossOrigin(origins = "http://localhost:5173")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {

        documentService.clear(); // 🔥 clear previous doc

        String content;

        if (file.getOriginalFilename().endsWith(".pdf")) {
            PDDocument document = PDDocument.load(file.getInputStream());
            PDFTextStripper stripper = new PDFTextStripper();
            content = stripper.getText(document);
            document.close();
        } else {
            content = new String(file.getBytes());
        }

        documentService.addDocument(content);

        return "Uploaded successfully";
    }
}