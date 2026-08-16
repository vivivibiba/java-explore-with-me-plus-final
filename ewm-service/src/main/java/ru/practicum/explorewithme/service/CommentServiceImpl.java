package ru.practicum.explorewithme.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.comment.*;
import ru.practicum.explorewithme.entity.Comment;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.mapper.CommentMapper;
import ru.practicum.explorewithme.repository.CommentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.common.pagination.OffsetPageRequest;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.repository.specification.AdminCommentSearchSpecification;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceBase implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public List<CommentShortDto> getComments(long eventId, int from, int size) {
        log.trace("Иницировано получение комментариев с параметрами from={} и size={}", from, size);
        List<Comment> result = commentRepository.findByEventIdWithOffset(eventId, from, size);
        log.debug("Найдено {} категорий", result.size());
        return result.stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    @Override
    public List<CommentDto> searchComments(long adminId,
                                           String text,
                                           List<Long> authorsIds,
                                           String rangeStart,
                                           String rangeEnd,
                                           List<Long> eventIds,
                                           List<CommentStatus> states,
                                           int from,
                                           int size) {
        log.trace("Иницирован поиск комментариев");
        Specification<Comment> spec = new AdminCommentSearchSpecification(text, authorsIds, rangeStart, rangeEnd, eventIds, states);
        Pageable pageable = new OffsetPageRequest(from, size);
        Page<Comment> commentPage = commentRepository.findAll(spec, pageable);
        List<Comment> comments = commentPage.getContent();
        log.debug("Получен список комментариев {}", comments);
        if (comments.isEmpty()) {
            return List.of();
        }

        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentDto moderateComment(long adminId, long commentId, ModerationAction action) {
        log.trace("Иницирована модерация комментария с id {} администратором с id {}", commentId, adminId);
        LocalDateTime now = LocalDateTime.now();
        User admin = findEntityIn(userRepository, adminId, Entities.USER);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);

        if (action.equals(ModerationAction.APPROVE)) {
            comment.setText(comment.getTextOnModeration());
            comment.setUpdated(now);
            comment.setStatus(CommentStatus.APPROVED);
        }

        if (action.equals(ModerationAction.REJECT) && comment.getText() == null) {
            comment.setStatus(CommentStatus.REJECTED);
        } else if (action.equals(ModerationAction.REJECT) && comment.getText() != null) {
            comment.setStatus(CommentStatus.APPROVED);
        }

        comment.setModerator(admin);
        comment.setModerated(now);
        comment.setTextOnModeration(null);
        commentRepository.save(comment);
        log.debug("Комментарий после модерации {}", comment);
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(long adminId, long commentId) {
        log.trace("Иницировано удаление комментария с id {} администратором с id {}", commentId, adminId);
        checkUserExistence(adminId);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        commentRepository.delete(comment);
        log.debug("Удален комметарий {}", comment);
    }

    @Override
    @Transactional
    public void deleteCommentByUser(long userId, long commentId) {
        log.trace("Иницировано удаление комментария с id {} пользователем с id {}", commentId, userId);
        checkUserExistence(userId);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        checkCommentAuthorship(comment, userId);
        commentRepository.delete(comment);
        log.debug("Удален комметарий {}", comment);
    }

    @Override
    public List<CommentShortDto> getUsersComments(long userId) {
        log.trace("Иницировано получение комментариев пользователя с id {}", userId);
        checkUserExistence(userId);
        List<Comment> commentsList = commentRepository.findByAuthorId(userId);
        log.debug("Получены кооментарии пользователя {}", commentsList);
        return commentsList.stream()
                .map(CommentMapper::toCommentShortDto)
                .toList();
    }

    @Override
    @Transactional
    public CommentShortDto createComment(long userId, long eventId, NewCommentDto body) {
        log.trace("Иницировано создание комментария к событию с id {} пользователем с id {}", eventId, userId);
        User author = findEntityIn(userRepository, userId, Entities.USER);
        Event event = findEntityIn(eventRepository, eventId, Entities.EVENT);
        Comment comment = CommentMapper.toComment(body);
        setFieldsOnCreation(comment, event, author);
        Comment createdComment = commentRepository.save(comment);
        log.info("Создан комментарий {}", createdComment);
        return CommentMapper.toCommentShortDto(createdComment);
    }

    @Override
    @Transactional
    public CommentShortDto updateComment(long userId, long commentId, UpdateCommentDto body) {
        log.trace("Иницировано обновление комментария с id {} пользователем с id {}", commentId, userId);
        checkUserExistence(userId);
        Comment comment = findEntityIn(commentRepository, commentId, Entities.COMMENT);
        checkCommentAuthorship(comment, userId);
        comment.setTextOnModeration(body.getText());
        comment.setStatus(CommentStatus.PENDING);
        Comment updatedComment = commentRepository.save(comment);
        log.info("Обновлен комментарий {}", updatedComment);
        return CommentMapper.toCommentShortDto(updatedComment);
    }

    private void setFieldsOnCreation(Comment comment, Event event, User author) {
        comment.setCreated(LocalDateTime.now());
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setStatus(CommentStatus.PENDING);
    }

    private void checkUserExistence(long userId) {
        boolean isUserExists = userRepository.existsById(userId);
        if (!isUserExists) {
            throw new NotFoundException(Entities.USER, userId);
        }
    }

    private void checkCommentAuthorship(Comment comment, long userId) {
        if (comment.getAuthor().getId() != userId) {
            throw new UnavailableUpdateException(Entities.COMMENT.name(), comment.getId());
        }
    }
}
