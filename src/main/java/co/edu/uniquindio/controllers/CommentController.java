package co.edu.uniquindio.controllers;

import co.edu.uniquindio.dto.CommentDTO;
import co.edu.uniquindio.dto.CommentResponse;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.model.Comment;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.services.interfaces.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(
            @RequestBody @Valid CommentDTO commentDTO,
            @AuthenticationPrincipal String userId) {  // userId viene del token JWT

        Comment comment = commentService.saveComment(commentDTO, userId);
        return ResponseEntity.ok(CommentResponse.fromComment(comment));
    }

}