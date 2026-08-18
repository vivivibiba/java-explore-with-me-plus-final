package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.practicum.explorewithme.client.EventClient;
import ru.practicum.explorewithme.client.UserClient;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.dto.comment.*;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.repository.CommentRepository;
import ru.practicum.explorewithme.repository.specification.AdminCommentSearchSpecification;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceBase implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    private final EventClient eventClient;
    private final TransactionTemplate transactionTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<CommentShortDto> getComments(long eventId, int from, int size) {
        return commentRepository.findByEventIdWithOffset(eventId, from, size).stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    @Override
    public List<CommentDto> searchComments(long adminId, String text, List<Long> authorsIds,
                                           String rangeStart, String rangeEnd, List<Long> eventIds,
                                           List<CommentStatus> states, int from, int size) {
        userClient.getUser(adminId);
        Specification<Comment> spec = new AdminCommentSearchSpecification(text, authorsIds, rangeStart, rangeEnd, eventIds, states);
        Pageable pageable = new OffsetPageRequest(from, size);
        Page<Comment> page = commentRepository.findAll(spec, pageable);
        return page.getContent().stream().map(CommentMapper::toCommentDto).toList();
    }

    @Override
    public CommentDto moderateComment(long adminId, long commentId, ModerationAction action) {
        userClient.getUser(adminId);

        return transactionTemplate.execute(transactionStatus -> {
            LocalDateTime now = LocalDateTime.now();
            Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
            if (action == ModerationAction.APPROVE) {
                comment.setText(comment.getTextOnModeration());
                comment.setUpdated(now);
                comment.setStatus(CommentStatus.APPROVED);
            }
            if (action == ModerationAction.REJECT && comment.getText() == null) {
                comment.setStatus(CommentStatus.REJECTED);
            } else if (action == ModerationAction.REJECT) {
                comment.setStatus(CommentStatus.APPROVED);
            }
            comment.setModeratorId(adminId);
            comment.setModerated(now);
            comment.setTextOnModeration(null);
            return CommentMapper.toCommentDto(commentRepository.save(comment));
        });
    }

    @Override
    public void deleteCommentByAdmin(long adminId, long commentId) {
        userClient.getUser(adminId);

        transactionTemplate.executeWithoutResult(transactionStatus ->
                commentRepository.delete(
                        findEntityIn(commentRepository, commentId, Entities.COMMENT)
                )
        );
    }

    @Override
    public void deleteCommentByUser(long userId, long commentId) {
        userClient.getUser(userId);

        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
            checkCommentAuthorship(comment, userId);
            commentRepository.delete(comment);
        });
    }

    @Override
    public List<CommentShortDto> getUsersComments(long userId) {
        userClient.getUser(userId);
        return commentRepository.findByAuthorId(userId).stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    @Override
    public CommentShortDto createComment(long userId, long eventId, NewCommentDto body) {
        userClient.getUser(userId);
        eventClient.getEvent(eventId);

        return transactionTemplate.execute(transactionStatus -> {
            Comment comment = CommentMapper.toComment(body);
            comment.setCreated(LocalDateTime.now());
            comment.setEventId(eventId);
            comment.setAuthorId(userId);
            comment.setStatus(CommentStatus.PENDING);
            return CommentMapper.toCommentShortDto(commentRepository.save(comment));
        });
    }

    @Override
    public CommentShortDto updateComment(long userId, long commentId, UpdateCommentDto body) {
        userClient.getUser(userId);

        return transactionTemplate.execute(transactionStatus -> {
            Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
            checkCommentAuthorship(comment, userId);
            comment.setTextOnModeration(body.getText());
            comment.setStatus(CommentStatus.PENDING);
            return CommentMapper.toCommentShortDto(commentRepository.save(comment));
        });
    }

    private void checkCommentAuthorship(Comment comment, long userId) {
        if (comment.getAuthorId() != userId) {
            throw new UnavailableUpdateException(Entities.COMMENT.name(), comment.getId());
        }
    }
}
