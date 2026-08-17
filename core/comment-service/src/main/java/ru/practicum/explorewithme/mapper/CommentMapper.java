package ru.practicum.explorewithme.mapper;

import ru.practicum.explorewithme.dto.comment.CommentDto;
import ru.practicum.explorewithme.dto.comment.CommentShortDto;
import ru.practicum.explorewithme.dto.comment.NewCommentDto;
import ru.practicum.explorewithme.entity.Comment;

public class CommentMapper {
    public static CommentShortDto toCommentShortDto(Comment comment) {
        return CommentShortDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .eventId(comment.getEventId())
                .authorId(comment.getAuthorId())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .eventId(comment.getEventId())
                .authorId(comment.getAuthorId())
                .status(comment.getStatus())
                .moderatorId(comment.getModeratorId())
                .moderated(comment.getModerated())
                .build();
    }

    public static Comment toComment(NewCommentDto dto) {
        return Comment.builder().textOnModeration(dto.getText()).build();
    }
}
