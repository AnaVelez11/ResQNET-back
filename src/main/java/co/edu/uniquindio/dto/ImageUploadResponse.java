package co.edu.uniquindio.dto;

import java.util.List;

public class ImageUploadResponse {
    private String mensaje;
    private List<String> urls;

    public ImageUploadResponse(String mensaje, List<String> urls) {
        this.mensaje = mensaje;
        this.urls = urls;
    }

}
