package com.tracker.budget;

import com.tracker.budget.entity.Category;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;

@SpringBootApplication
public class StudentBudgetTrackerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudentBudgetTrackerApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(CategoryRepository categoryRepository) {
        return args -> {
            // Seed system default categories if database is newly initialized
            if (categoryRepository.count() == 0) {
                List<Category> defaultCategories = Arrays.asList(
                        // Expense categories tailored for university students
                        new Category(null, "Groceries & Dining", TransactionType.EXPENSE, "utensils", "#EF4444", true),
                        new Category(null, "Rent & Housing", TransactionType.EXPENSE, "home", "#F59E0B", true),
                        new Category(null, "Textbooks & Supplies", TransactionType.EXPENSE, "book-open", "#10B981", true),
                        new Category(null, "Tuition & Fees", TransactionType.EXPENSE, "academic-cap", "#3B82F6", true),
                        new Category(null, "Transportation & Transit", TransactionType.EXPENSE, "truck", "#6366F1", true),
                        new Category(null, "Utilities & Internet", TransactionType.EXPENSE, "lightning-bolt", "#8B5CF6", true),
                        new Category(null, "Entertainment & Social", TransactionType.EXPENSE, "sparkles", "#EC4899", true),
                        new Category(null, "Health & Personal Care", TransactionType.EXPENSE, "heart", "#14B8A6", true),
                        new Category(null, "Subscription Services", TransactionType.EXPENSE, "film", "#F97316", true),
                        new Category(null, "Miscellaneous Expenses", TransactionType.EXPENSE, "dots-circle-horizontal", "#6B7280", true),

                        // Income categories
                        new Category(null, "Part-Time Job", TransactionType.INCOME, "briefcase", "#10B981", true),
                        new Category(null, "Scholarships & Grants", TransactionType.INCOME, "badge-check", "#3B82F6", true),
                        new Category(null, "Student Loans", TransactionType.INCOME, "cash", "#06B6D4", true),
                        new Category(null, "Family Allowance", TransactionType.INCOME, "user-group", "#8B5CF6", true),
                        new Category(null, "Freelance & Tutoring", TransactionType.INCOME, "laptop", "#14B8A6", true),
                        new Category(null, "Other Income", TransactionType.INCOME, "plus-circle", "#6B7280", true)
                );
                categoryRepository.saveAll(defaultCategories);
            }
        };
    }
}
