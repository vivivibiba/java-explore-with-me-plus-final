package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

    @Query(value = """
            SELECT *
            FROM comments
            WHERE event_id = :event_id AND status = 'APPROVED'
            ORDER BY id
            OFFSET :from
            ROWS FETCH NEXT :size ROWS ONLY
            """,
            nativeQuery = true)
    List<Comment> findByEventIdWithOffset(@Param("event_id") long eventId, @Param("from") int from, @Param("size") int size);

    List<Comment> findByAuthorId(long authorId);
}
