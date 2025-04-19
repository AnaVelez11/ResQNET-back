package co.edu.uniquindio.services.implementations;

import co.edu.uniquindio.dto.CommentDTO;
import co.edu.uniquindio.exceptions.ResourceNotFoundException;
import co.edu.uniquindio.model.Comment;
import co.edu.uniquindio.model.Report;
import co.edu.uniquindio.model.User;
import co.edu.uniquindio.repositories.CommentRepository;
import co.edu.uniquindio.repositories.ReportRepository;
import co.edu.uniquindio.repositories.UserRepository;
import co.edu.uniquindio.services.interfaces.CommentService;
import co.edu.uniquindio.services.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private static final Logger log = Logger.getLogger(CommentServiceImpl.class.getName());

    private final CommentRepository commentRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final SimpMessagingTemplate messagingTemplate;


    @Transactional
    public Comment saveComment(CommentDTO commentDTO, String userId) {
        // 1. Validaciones
        Report report = reportRepository.findById(commentDTO.getIdReport()).orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado"));

        User author = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Crear comentario
        Comment comment = Comment.builder().idComment(UUID.randomUUID().toString()).content(commentDTO.getContent()).date(new Date()).idReport(report.getId()).idUser(userId).build();

        Comment savedComment = commentRepository.save(comment);

        // 3. Notificaciones (maneja errores específicos)
        notifyAuthorByEmail(report.getIdUser(), author.getName(), commentDTO.getContent());

        return savedComment;
    }

    private void notifyCommentViaWebSocket(Comment comment, String reportOwnerId) {
        messagingTemplate.convertAndSend("/topic/reports/" + comment.getIdReport() + "/comments", Map.of("type", "NEW_COMMENT", "commentId", comment.getIdComment(), "content", comment.getContent(), "authorId", comment.getIdUser(), "timestamp", comment.getDate()));

        // Notificar al dueño del reporte
        messagingTemplate.convertAndSendToUser(reportOwnerId, "/queue/notifications", Map.of("type", "NEW_COMMENT_ON_YOUR_REPORT", "reportId", comment.getIdReport(), "message", "Nuevo comentario en tu reporte"));
    }

    private void notifyAuthorByEmail(String reportOwnerId, String authorName, String commentContent) {
        try {
            User reportOwner = userRepository.findById(reportOwnerId).orElseThrow(() -> new ResourceNotFoundException("Dueño del reporte no encontrado"));

            String subject = "Nuevo comentario en tu reporte";
            String body = String.format("Hola %s,\n\n%s ha comentado en tu reporte: \"%s\"", reportOwner.getName(), authorName, commentContent.substring(0, Math.min(commentContent.length(), 50)) + "...");

            emailService.sendEmail(reportOwner.getEmail(), subject, body);

        } catch (Exception e) {
            log.warning("Error enviando email al autor del reporte: " + e.getMessage());
        }
    }

}
