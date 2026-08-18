package ru.practicum.explorewithme.service;

import ru.practicum.explorewithme.dto.comment.*;

import java.util.List;

public interface CommentService {
    List<CommentShortDto> getComments(long userId, int from, int size);

    List<CommentDto> searchComments(long adminId,
                                    String text,
                                    List<Long> authorsIds,
                                    String rangeStart,
                                    String rangeEnd,
                                    List<Long> eventId,
                                    List<CommentStatus> states,
                                    int from,
                                    int size);

    CommentDto moderateComment(long adminId, long commentId, ModerationAction action);

    void deleteCommentByAdmin(long adminId, long commentId);

    void deleteCommentByUser(long userId, long commentId);

    List<CommentShortDto> getUsersComments(long userId);

    CommentShortDto createComment(long userId, long eventId, NewCommentDto newCommentDto);

    CommentShortDto updateComment(long userId, long commentId, UpdateCommentDto updateCommentDto);
}
