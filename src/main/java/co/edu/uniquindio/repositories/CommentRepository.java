package co.edu.uniquindio.repositories;

import co.edu.uniquindio.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findAllByIdReport(String idReport);

    List<Comment> findAllByIdUser(String idUser);

}
