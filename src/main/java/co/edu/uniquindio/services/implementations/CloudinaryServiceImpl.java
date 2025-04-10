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
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Autowired
    public  CloudinaryServiceImpl(Cloudinary cloudinary){
        this.cloudinary = cloudinary;
    }
    @Override
    public List<String> uploadImages(List<MultipartFile> files) {
        List<String> imageUrls = new ArrayList<>();
        if (files == null || files.isEmpty()) return imageUrls;

        for (MultipartFile file : files) {
            try {
                Map<?, ?> uploadResult = cloudinary.uploader().upload(
                        file.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "resqnet/reports",
                                "resource_type", "auto"
                        )
                );
                imageUrls.add((String) uploadResult.get("secure_url"));
            } catch (IOException e) {
                log.error("Error subiendo imagen a Cloudinary: {}", e.getMessage());
                throw new RuntimeException("Error al subir la imagen", e);
            }
        }
        return imageUrls;    }

    @Override
    public void deleteImage(String publicId) {

    }
}