package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.services.interfaces.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        if (files == null || files.isEmpty()) return imageUrls;

        for (MultipartFile file : files) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", "resqnet/reports", "resource_type", "auto"));
                String url = (String) uploadResult.get("secure_url");
                imageUrls.add(url);
                log.info("Imagen subida exitosamente: {}", url);
            } catch (IOException e) {
                log.error("Error subiendo imagen a Cloudinary: {}", e.getMessage());
                throw new RuntimeException("Error al subir la imagen", e);
            }
        }
        log.info("Todas las imágenes fueron subidas exitosamente.");
        return imageUrls;
    }

    //  Métodos auxiliares
    private void deleteImageByUrl(String imageUrl) {
        try {
            // Extrae el public_id de la URL
            String publicId = extractPublicIdFromUrl(imageUrl);
            if (publicId != null) {
                Map<String, String> options = new HashMap<>();
                cloudinary.uploader().destroy(publicId, options);
            }
        } catch (Exception e) {
            // Loggear el error pero continuar
            log.error("Error al eliminar imagen de Cloudinary: " + imageUrl, e);
        }
    }

    private String extractPublicIdFromUrl(String imageUrl) {
        try {
            String[] parts = imageUrl.split("/upload/");
            if (parts.length > 1) {
                String path = parts[1];
                // Elimina parámetros de versión (v12345) si existen
                path = path.replaceFirst("^v\\d+/", "");
                // Elimina la extensión del archivo
                return path.substring(0, path.lastIndexOf('.'));
            }
        } catch (Exception e) {
            log.error("Error al extraer public_id de URL: " + imageUrl, e);
        }
        return null;
    }
}