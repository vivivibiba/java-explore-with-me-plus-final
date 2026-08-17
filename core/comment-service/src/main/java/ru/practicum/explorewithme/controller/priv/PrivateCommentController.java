package ru.practicum.explorewithme.controller.priv;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.dto.comment.UpdateCommentDto;
import ru.practicum.explorewithme.service.CommentService;

import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = ACCESS_PRIVATE + "/{" + ID_USER + "}")
@RequiredArgsConstructor
public class PrivateCommentController {
    private final CommentService commentService;

    @GetMapping(URL_COMMENTS)
    public ResponseEntity<List<CommentShortDto>> getComments(@PathVariable long userId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.getUsersComments(userId));
    }

    @PostMapping(URL_EVENTS + "/{" + ID_EVENT + "}" + URL_COMMENTS)
    public ResponseEntity<CommentShortDto> createComment(@PathVariable long userId,
                                                         @PathVariable long eventId,
                                                         @RequestBody NewCommentDto body) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(userId, eventId, body));
    }

    @PatchMapping(URL_COMMENTS + "/{" + ID_COMMENT + "}")
    public ResponseEntity<CommentShortDto> updateComment(@PathVariable long userId,
                                                         @PathVariable long commentId,
                                                         @RequestBody UpdateCommentDto body) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.updateComment(userId, commentId, body));
    }

    @DeleteMapping(URL_COMMENTS + "/{" + ID_COMMENT + "}")
    public ResponseEntity<Void> deleteComment(@PathVariable long userId, @PathVariable long commentId) {
        commentService.deleteCommentByUser(userId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(null);
    }
}
