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
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthor().getId())
                .build();
    }

    public static CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .created(comment.getCreated())
                .updated(comment.getUpdated())
                .eventId(comment.getEvent().getId())
                .authorId(comment.getAuthor().getId())
                .status(comment.getStatus())
                .moderatorId(comment.getModerator() != null ? comment.getModerator().getId() : null)
                .moderated(comment.getModerated())
                .build();
    }

    public static Comment toComment(NewCommentDto dto) {
        return Comment.builder()
                .textOnModeration(dto.getText())
                .build();
    }
}