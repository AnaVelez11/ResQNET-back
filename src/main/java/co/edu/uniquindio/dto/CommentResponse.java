package co.edu.uniquindio.dto;

import co.edu.uniquindio.model.Comment;

import java.util.Date;

public record CommentResponse(
        String id,
        String content,
        String reportId,
        String authorId,
        Date timestamp
) {
    public static CommentResponse fromComment(Comment comment) {
        return new CommentResponse(
                comment.getIdComment(),
                comment.getContent(),
                comment.getIdReport(),
                comment.getIdUser(),
                comment.getDate()
        );
    }
}