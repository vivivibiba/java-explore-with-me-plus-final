package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.explorewithme.entity.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);

    @Query(value = """
            SELECT *
            FROM categories
            ORDER BY id
            OFFSET :from
            ROWS FETCH NEXT :size ROWS ONLY
            """,
            nativeQuery = true)
    List<Category> findWithOffset(@Param("from") int from, @Param("size") int size);
}
