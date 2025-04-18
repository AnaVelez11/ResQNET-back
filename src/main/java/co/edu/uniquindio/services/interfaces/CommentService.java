package co.edu.uniquindio.services.interfaces;

import co.edu.uniquindio.dto.CommentDTO;
import co.edu.uniquindio.model.Comment;

public interface CommentService {

    Comment saveComment(CommentDTO commentDTO, String userId);
}
