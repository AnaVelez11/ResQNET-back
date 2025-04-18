package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.enums.ReportStatus;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends MongoRepository<Report, String> {
    void deleteById(String id);
    // Busca reportes por estado y categorías (paginados)
    Page<Report> findByStatusAndCategoriesIn(ReportStatus status, List<String> categories, Pageable pageable);

    // Busca reportes por estado (paginados)
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    // Busca reportes por categorías (paginados)
    Page<Report> findByCategoriesIn(List<String> categories, Pageable pageable);

    // Consulta para reportes recientes en un radio (usando coordenadas GeoJSON)
    @Query("{"
            + "'location': { "
            + "  '$nearSphere': { "
            + "    '$geometry': { "
            + "      'type': 'Point', "
            + "      'coordinates': [?0, ?1] "
            + "    }, "
            + "    '$maxDistance': ?2 "
            + "  } "
            + "}, "
            + "'date': { '$gte': ?3 } "
            + "}")
    List<Report> findRecentReportsNearLocation(double longitude, double latitude, double maxDistanceInMeters, LocalDateTime minDate
    );

    // Busca reportes de un usuario
    List<Report> findById(ObjectId userId);

    // Método para verificar si existe algún reporte que use una categoría específica
    @Query(value = "{ 'categories': ?0 }", exists = true)
    boolean existsByCategoriesContaining(String categoryId);

    // Método alternativo que cuenta cuántos reportes usan la categoría
    @Query(value = "{ 'categories': ?0 }", count = true)
    long countReportsUsingCategory(String categoryId);

    // Excluye reportes anónimos en consultas normales
    @Query("{ 'anonymous': false }")
    Page<Report> findAllActive(Pageable pageable);
    @Query("{ 'idUser': ?0 }")
    List<Report> findByIdUser(String idUser);

}
