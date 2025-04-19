package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.model.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para conversión de Reportes y sus DTOs
 * Uso: Transforma entre la entidad Report y sus representaciones API
 */
@Mapper(componentModel = "spring")
public interface ReportMapper {

    // Convierte Report -> ReportResponse (para respuestas API)
    // - Renombra idUser -> userId
    // - Omite el campo 'message' en la respuesta
    @Mapping(source = "idUser", target = "userId")
    @Mapping(target = "message", ignore = true)


    ReportResponse toResponse(Report report);
}
