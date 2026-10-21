package com.tracker.budget.repository;

import com.tracker.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b LEFT JOIN FETCH b.categoryBudgets cb LEFT JOIN FETCH cb.category WHERE b.user.id = :userId AND b.yearMonth = :yearMonth")
    Optional<Budget> findByUserIdAndYearMonthWithCategories(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    Optional<Budget> findByUserIdAndYearMonth(Long userId, String yearMonth);

    List<Budget> findAllByUserIdOrderByYearMonthDesc(Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndYearMonth(Long userId, String yearMonth);
}
