package com.tracker.budget.service;

import com.tracker.budget.dto.CategoryRequest;
import com.tracker.budget.dto.CategoryResponse;
import com.tracker.budget.entity.Category;
import com.tracker.budget.entity.TransactionType;
import com.tracker.budget.entity.User;
import com.tracker.budget.exception.BadRequestException;
import com.tracker.budget.exception.ResourceNotFoundException;
import com.tracker.budget.exception.UnauthorizedException;
import com.tracker.budget.repository.CategoryRepository;
import com.tracker.budget.security.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuthService authService;
    private final SecurityUtils securityUtils;

    public CategoryService(CategoryRepository categoryRepository, AuthService authService, SecurityUtils securityUtils) {
        this.categoryRepository = categoryRepository;
        this.authService = authService;
        this.securityUtils = securityUtils;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllAccessibleCategories() {
        Long userId = securityUtils.getCurrentUserId();
        return categoryRepository.findAllAccessibleCategories(userId).stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAccessibleCategoriesByType(TransactionType type) {
        Long userId = securityUtils.getCurrentUserId();
        return categoryRepository.findAllAccessibleByType(userId, type).stream()
                .map(CategoryResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntityForUser(Long categoryId) {
        Long userId = securityUtils.getCurrentUserId();
        return categoryRepository.findAccessibleById(categoryId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));
    }

    @Transactional
    public CategoryResponse createCustomCategory(CategoryRequest request) {
        User currentUser = authService.getCurrentUserEntity();

        if (categoryRepository.existsByUserIdAndNameIgnoreCaseAndType(currentUser.getId(), request.getName().trim(), request.getType())) {
            throw new BadRequestException("A category with name '" + request.getName() + "' already exists for your account.");
        }

        Category category = new Category();
        category.setUser(currentUser);
        category.setName(request.getName().trim());
        category.setType(request.getType());
        category.setIcon(request.getIcon() != null && !request.getIcon().isBlank() ? request.getIcon().trim() : "tag");
        category.setColor(request.getColor() != null && !request.getColor().isBlank() ? request.getColor().trim() : "#6366F1");
        category.setSystemDefault(false);

        Category saved = categoryRepository.save(category);
        return new CategoryResponse(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (category.isSystemDefault()) {
            throw new UnauthorizedException("System default categories cannot be modified.");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to modify this category.");
        }

        category.setName(request.getName().trim());
        category.setType(request.getType());
        category.setIcon(request.getIcon().trim());
        category.setColor(request.getColor().trim());

        Category updated = categoryRepository.save(category);
        return new CategoryResponse(updated);
    }

    @Transactional
    public void deleteCategory(Long id) {
        Long userId = securityUtils.getCurrentUserId();
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        if (category.isSystemDefault()) {
            throw new UnauthorizedException("System default categories cannot be deleted.");
        }

        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You do not have permission to delete this category.");
        }

        categoryRepository.delete(category);
    }
}
