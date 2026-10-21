package com.tracker.budget.repository;

import com.tracker.budget.entity.Category;
import com.tracker.budget.entity.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId ORDER BY c.type, c.name")
    List<Category> findAllAccessibleCategories(@Param("userId") Long userId);

    @Query("SELECT c FROM Category c WHERE (c.user IS NULL OR c.user.id = :userId) AND c.type = :type ORDER BY c.name")
    List<Category> findAllAccessibleByType(@Param("userId") Long userId, @Param("type") TransactionType type);

    List<Category> findByUserIdOrderByNameAsc(Long userId);

    Optional<Category> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT c FROM Category c WHERE c.id = :id AND (c.user IS NULL OR c.user.id = :userId)")
    Optional<Category> findAccessibleById(@Param("id") Long id, @Param("userId") Long userId);

    boolean existsByUserIdAndNameIgnoreCaseAndType(Long userId, String name, TransactionType type);
}
