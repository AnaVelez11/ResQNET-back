package co.edu.uniquindio.controllers;

import co.edu.uniquindio.services.interfaces.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImageUploadController {

    private final CloudinaryService cloudinaryService;

    //// Subir múltiples imágenes a Cloudinary
    ///
    //// Retorna lista de URLs públicas de las imágenes subidas
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        List<String> urls = cloudinaryService.uploadImages(files);
        return ResponseEntity.ok(urls);
    }
}
