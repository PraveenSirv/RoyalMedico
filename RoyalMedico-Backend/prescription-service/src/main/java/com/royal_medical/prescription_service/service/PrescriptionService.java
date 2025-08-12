package com.royal_medical.prescription_service.service;

import com.royal_medical.prescription_service.client.FClient;
import com.royal_medical.prescription_service.config.FileStorageProperties;
import com.royal_medical.prescription_service.dto.CustomerDto;
import com.royal_medical.prescription_service.dto.PrescriptionResponse;
import com.royal_medical.prescription_service.exception.CustomerNotFoundException;
import com.royal_medical.prescription_service.exception.NoPrescriptionsFoundException;
import com.royal_medical.prescription_service.model.Prescription;
import com.royal_medical.prescription_service.repository.PrescriptionRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final FileStorageProperties properties;
    private final FClient client;

    public Prescription storeFile(MultipartFile file, Long customerId) throws IOException {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        Path uploadPath = Paths.get(properties.getUploadDir());

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Extract text via OCR (image or PDF)
        String extractedText;
        if (file.getContentType().equalsIgnoreCase("application/pdf")) {
            extractedText = extractTextFromPdf(filePath.toFile(), uploadPath);
        } else {
            extractedText = extractTextFromImage(filePath.toFile());
        }

        Prescription prescription = Prescription.builder()
                .customerId(customerId)
                .fileName(fileName)
                .fileType(file.getContentType())
                .fileUrl(filePath.toString())
                .uploadedAt(LocalDateTime.now())
                .extractedText(extractedText)
                .build();

        return prescriptionRepository.save(prescription);
    }

    private String extractTextFromPdf(File pdfFile, Path uploadPath) {
        StringBuilder sb = new StringBuilder();
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            ITesseract tesseract = new Tesseract();
            tesseract.setLanguage("eng");

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300); // high quality
                File tempImg = new File(uploadPath.toFile(), "page_" + UUID.randomUUID() + ".png");
                ImageIO.write(image, "png", tempImg);

                try {
                    sb.append(tesseract.doOCR(tempImg)).append("\n");
                } catch (TesseractException e) {
                    sb.append("OCR Error on page ").append(page).append(": ").append(e.getMessage()).append("\n");
                } finally {
                    tempImg.delete(); // cleanup temp image
                }
            }

        } catch (IOException e) {
            sb.append("PDF Error: ").append(e.getMessage());
        }

        return sb.toString();
    }

    private String extractTextFromImage(File imageFile) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("/opt/homebrew/share/tessdata"); // adjust if different
        tesseract.setLanguage("eng");

        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            return "OCR Error: " + e.getMessage();
        }
    }

//    public Prescription storeFile(MultipartFile file, Long customerId) throws IOException {
//        String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
//        Path uploadPath = Paths.get(properties.getUploadDir());
//
//        if (!Files.exists(uploadPath)) {
//            Files.createDirectories(uploadPath);
//        }
//
//        Path filePath = uploadPath.resolve(fileName);
//        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//
//        Prescription prescription = Prescription.builder()
//                .customerId(customerId)
//                .fileName(fileName)
//                .fileType(file.getContentType())
//                .fileUrl(filePath.toString())
//                .uploadedAt(LocalDateTime.now())
//                .extractedText(null) // to be filled by OCR
//                .build();
//
//        return prescriptionRepository.save(prescription);
//    }


    public PrescriptionResponse getPrescriptionsByCustomer(Long customerId) {
        try {
            ResponseEntity<?> response = client.getCustomer(customerId);

            if (!response.hasBody()) {
                throw new CustomerNotFoundException("Customer with ID: " + customerId + " does not exist");
            }

        } catch (FeignException.NotFound e) {
            throw new CustomerNotFoundException("Customer with ID: " + customerId + " does not exist");
        } catch (FeignException e) {
            throw new RuntimeException("Customer service is unavailable");
        }

        List<Prescription> prescriptions = prescriptionRepository.findByCustomerId(customerId);

        if (prescriptions == null || prescriptions.isEmpty()) {
            throw new NoPrescriptionsFoundException("No prescriptions found for customer ID: " + customerId);
        }

        return new PrescriptionResponse("Prescriptions retrieved successfully", prescriptions);
    }

    public Object getById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id).orElse(null);
        if (prescription != null){
            return prescription;
        }
        return "No Prescription is available for the request.";
    }

    public String deletePrescription(Long id) throws IOException {
        Prescription prescription = prescriptionRepository.findById(id).orElse(null);
        if (prescription != null) {
            Files.deleteIfExists(Paths.get(prescription.getFileUrl()));
            prescriptionRepository.deleteById(id);
            return "Prescription with PrescriptionID : " + id + "  Deleted Successfully." ;
        }
        return "Prescription with PrescriptionID : " + id + " not found.";
    }

}