-- ===================================================================
-- Student Budget Tracker - MySQL 8 Database Schema
-- Production Relational Schema Definition
-- ===================================================================

CREATE DATABASE IF NOT EXISTS `student_budget_tracker`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE `student_budget_tracker`;

-- Table: users
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `university` VARCHAR(150) NULL,
    `currency_symbol` VARCHAR(5) NOT NULL DEFAULT '$',
    `monthly_budget_baseline` DECIMAL(12, 2) NOT NULL DEFAULT 0.00,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: categories
-- Supports system default categories (user_id IS NULL) and custom user categories
CREATE TABLE IF NOT EXISTS `categories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NULL,
    `name` VARCHAR(60) NOT NULL,
    `type` VARCHAR(10) NOT NULL, -- 'INCOME' or 'EXPENSE'
    `icon` VARCHAR(50) NOT NULL DEFAULT 'tag',
    `color` VARCHAR(20) NOT NULL DEFAULT '#6366F1',
    `is_system_default` BOOLEAN NOT NULL DEFAULT FALSE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_category_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_category_user` (`user_id`),
    INDEX `idx_category_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: budgets (Monthly overarching budget)
CREATE TABLE IF NOT EXISTS `budgets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `year_month` VARCHAR(7) NOT NULL, -- Format: YYYY-MM
    `monthly_limit` DECIMAL(12, 2) NOT NULL,
    `notes` VARCHAR(255) NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_budget_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `uk_budget_user_month` UNIQUE (`user_id`, `year_month`),
    INDEX `idx_budget_user_month` (`user_id`, `year_month`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: category_budgets (Category-specific limits for a budget)
CREATE TABLE IF NOT EXISTS `category_budgets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `budget_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `allocated_amount` DECIMAL(12, 2) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cat_budget_parent` FOREIGN KEY (`budget_id`) REFERENCES `budgets` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cat_budget_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE,
    CONSTRAINT `uk_cat_budget_unique` UNIQUE (`budget_id`, `category_id`),
    INDEX `idx_cat_budget_parent` (`budget_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table: transactions (Income and Expense entries)
CREATE TABLE IF NOT EXISTS `transactions` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `category_id` BIGINT NOT NULL,
    `type` VARCHAR(10) NOT NULL, -- 'INCOME' or 'EXPENSE'
    `amount` DECIMAL(12, 2) NOT NULL,
    `description` VARCHAR(255) NOT NULL,
    `transaction_date` DATE NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_tx_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_tx_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT,
    INDEX `idx_tx_user_date` (`user_id`, `transaction_date`),
    INDEX `idx_tx_category` (`category_id`),
    INDEX `idx_tx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
