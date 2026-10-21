package com.tracker.budget.repository;

import com.tracker.budget.entity.Transaction;
import com.tracker.budget.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:type IS NULL OR t.type = :type) " +
           "AND (:categoryId IS NULL OR t.category.id = :categoryId) " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:search IS NULL OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.transactionDate DESC, t.id DESC")
    Page<Transaction> findFilteredTransactions(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> findAllInDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    List<Transaction> findTop10ByUserIdOrderByTransactionDateDescIdDesc(Long userId);

    @Query("SELECT COALESCE(SUM(t.amount), 0.00) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumAmountByUserIdAndTypeAndDateBetween(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(t.amount), 0.00) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = 'EXPENSE' AND t.category.id = :categoryId " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate")
    BigDecimal sumExpenseByCategoryAndDateBetween(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Category breakdown projection: categoryId, categoryName, color, icon, totalAmount
    @Query("SELECT t.category.id AS categoryId, t.category.name AS categoryName, " +
           "t.category.color AS color, t.category.icon AS icon, SUM(t.amount) AS totalAmount " +
           "FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
           "GROUP BY t.category.id, t.category.name, t.category.color, t.category.icon " +
           "ORDER BY SUM(t.amount) DESC")
    List<CategorySummaryProjection> getCategoryBreakdown(
            @Param("userId") Long userId,
            @Param("type") TransactionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    interface CategorySummaryProjection {
        Long getCategoryId();
        String getCategoryName();
        String getColor();
        String getIcon();
        BigDecimal getTotalAmount();
    }
}
