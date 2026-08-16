package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.entity.Compilation;

import java.util.List;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @Query(value = """
            SELECT *
            FROM compilations
            WHERE (:pinned IS NULL OR pinned = :pinned)
            ORDER BY id
            OFFSET :from
            ROWS FETCH NEXT :size ROWS ONLY
            """,
            nativeQuery = true)
    List<Compilation> findWithOffset(@Param("pinned") Boolean pinned, @Param("from") int from, @Param("size") int size);
}
