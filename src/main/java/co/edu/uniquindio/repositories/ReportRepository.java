package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.enums.ReportStatus;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReadPreference;
import org.springframework.stereotype.Repository;
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

    // Búsqueda por cercanía geográfica (lat, lon, radio en metros)
    @Query(value = """
         {
             "location": {
                 "$near": {
                     "$geometry": {
                         "type": "Point",
                         "coordinates": [?1, ?0]
                     },
                     "$maxDistance": ?2
                 }
             }
         }
     """)
    List<Report> findNearLocation(double latitude, double longitude, double maxDistance);

    // Busca reportes de un usuario
    List<Report> findById(ObjectId userId);
}
