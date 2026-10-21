-- ===================================================================
-- Student Budget Tracker - Default Categories & Seed Data
-- ===================================================================

USE `student_budget_tracker`;

-- Insert System Default Categories (Applicable to all students)
INSERT INTO `categories` (`user_id`, `name`, `type`, `icon`, `color`, `is_system_default`) VALUES
-- Expense Categories tailored for students
(NULL, 'Groceries & Dining', 'EXPENSE', 'utensils', '#EF4444', TRUE),
(NULL, 'Rent & Housing', 'EXPENSE', 'home', '#F59E0B', TRUE),
(NULL, 'Textbooks & Supplies', 'EXPENSE', 'book-open', '#10B981', TRUE),
(NULL, 'Tuition & Fees', 'EXPENSE', 'academic-cap', '#3B82F6', TRUE),
(NULL, 'Transportation & Transit', 'EXPENSE', 'truck', '#6366F1', TRUE),
(NULL, 'Utilities & Internet', 'EXPENSE', 'lightning-bolt', '#8B5CF6', TRUE),
(NULL, 'Entertainment & Social', 'EXPENSE', 'sparkles', '#EC4899', TRUE),
(NULL, 'Health & Personal Care', 'EXPENSE', 'heart', '#14B8A6', TRUE),
(NULL, 'Subscription Services', 'EXPENSE', 'film', '#F97316', TRUE),
(NULL, 'Miscellaneous Expenses', 'EXPENSE', 'dots-circle-horizontal', '#6B7280', TRUE),

-- Income Categories
(NULL, 'Part-Time Job', 'INCOME', 'briefcase', '#10B981', TRUE),
(NULL, 'Scholarships & Grants', 'INCOME', 'badge-check', '#3B82F6', TRUE),
(NULL, 'Student Loans', 'INCOME', 'cash', '#06B6D4', TRUE),
(NULL, 'Family Allowance', 'INCOME', 'user-group', '#8B5CF6', TRUE),
(NULL, 'Freelance & Tutoring', 'INCOME', 'laptop', '#14B8A6', TRUE),
(NULL, 'Other Income', 'INCOME', 'plus-circle', '#6B7280', TRUE);

-- Demo User (Password is 'Student123!' hashed with BCrypt)
-- Hash: $2a$10$w09u7RrvBv19Mh/hS.p08OncZ5J60jQ7Gg/v5nUj8M3s7mE7RjY4K
INSERT INTO `users` (`id`, `email`, `password`, `full_name`, `university`, `currency_symbol`, `monthly_budget_baseline`)
VALUES (1, 'alex.student@campus.edu', '$2a$10$w09u7RrvBv19Mh/hS.p08OncZ5J60jQ7Gg/v5nUj8M3s7mE7RjY4K', 'Alex Mercer', 'Metropolitan University', '$', 1200.00);

-- Monthly Budget for Current Month (2026-10)
INSERT INTO `budgets` (`id`, `user_id`, `year_month`, `monthly_limit`, `notes`)
VALUES (1, 1, '2026-10', 1200.00, 'Fall Semester October Budget');

-- Category Budgets for Demo
INSERT INTO `category_budgets` (`budget_id`, `category_id`, `allocated_amount`) VALUES
(1, 1, 350.00), -- Groceries & Dining
(1, 2, 550.00), -- Rent & Housing
(1, 3, 100.00), -- Textbooks & Supplies
(1, 5, 80.00),  -- Transportation & Transit
(1, 7, 120.00); -- Entertainment & Social

-- Demo Transactions for October 2026
INSERT INTO `transactions` (`user_id`, `category_id`, `type`, `amount`, `description`, `transaction_date`) VALUES
(1, 11, 'INCOME', 850.00, 'Campus IT Helpdesk Bi-weekly Paycheck', '2026-10-01'),
(1, 14, 'INCOME', 400.00, 'Monthly Parents Contribution', '2026-10-02'),
(1, 15, 'INCOME', 120.00, 'Physics Tutoring 3 Sessions', '2026-10-14'),
(1, 2, 'EXPENSE', 525.00, 'Shared Apartment Rent (Room 3B)', '2026-10-02'),
(1, 1, 'EXPENSE', 84.50, 'Weekly Grocery Run at Trader Joes', '2026-10-04'),
(1, 3, 'EXPENSE', 62.00, 'Database Systems Lab Manual & Notebooks', '2026-10-06'),
(1, 5, 'EXPENSE', 35.00, 'Monthly Student Transit Pass Reload', '2026-10-07'),
(1, 1, 'EXPENSE', 18.75, 'Campus Cafe Lunch & Espresso', '2026-10-10'),
(1, 7, 'EXPENSE', 45.00, 'Campus Cinema Night & Pizza with Roommates', '2026-10-12'),
(1, 6, 'EXPENSE', 42.80, 'High-Speed Apartment Wi-Fi Share', '2026-10-15');
