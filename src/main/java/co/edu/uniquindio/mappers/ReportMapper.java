package co.edu.uniquindio.mappers;

import co.edu.uniquindio.dto.ReportResponse;
import co.edu.uniquindio.model.Report;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReportMapper {
    @Mapping(source = "idUser", target = "userId")
    @Mapping(target = "message", ignore = true)
    ReportResponse toResponse(Report report);
}
