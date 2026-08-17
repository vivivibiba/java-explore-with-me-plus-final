package ru.practicum.explorewithme.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentStatus;
import ru.practicum.explorewithme.dto.comment.ModerationAction;
import ru.practicum.explorewithme.service.CommentService;

import java.util.List;

import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@RestController
@RequestMapping(path = ACCESS_ADMIN + URL_COMMENTS)
@RequiredArgsConstructor
public class AdminCommentController {
    private final CommentService commentService;
    private static final String ADMIN_HEADER = "X-EWV-Admin-Id";

    @GetMapping
    public ResponseEntity<List<CommentDto>> searchComments(@RequestHeader("X-EWV-Admin-Id") long adminId,
                                                           @RequestParam(required = false) String text,
                                                           @RequestParam(required = false) List<Long> authorsIds,
                                                           @RequestParam(required = false) String rangeStart,
                                                           @RequestParam(required = false) String rangeEnd,
                                                           @RequestParam(required = false) List<Long> eventIds,
                                                           @RequestParam(required = false) List<CommentStatus> states,
                                                           @RequestParam(name = PARAM_FROM, defaultValue = "0") int from,
                                                           @RequestParam(name = PARAM_SIZE, defaultValue = "10") int size) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.searchComments(adminId, text, authorsIds, rangeStart, rangeEnd, eventIds, states, from, size));
    }

    @PatchMapping("/{" + ID_COMMENT + "}")
    public ResponseEntity<CommentDto> moderateComment(@RequestHeader(ADMIN_HEADER) long adminId,
                                                      @PathVariable long commentId,
                                                      @RequestParam ModerationAction action) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(commentService.moderateComment(adminId, commentId, action));
    }

    @DeleteMapping("/{" + ID_COMMENT + "}")
    public ResponseEntity<Void> deleteComment(@RequestHeader(ADMIN_HEADER) long adminId,
                                              @PathVariable long commentId) {
        commentService.deleteCommentByAdmin(adminId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(null);
    }
}
