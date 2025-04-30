package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.config.CategorySpecifications;
import it.mahmoud.advmanagement.dto.category.CategoryCreateDTO;
import it.mahmoud.advmanagement.dto.category.CategoryDTO;
import it.mahmoud.advmanagement.dto.category.CategorySelectDTO;
import it.mahmoud.advmanagement.exception.DuplicateResourceException;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Category;
import it.mahmoud.advmanagement.repo.CategoryRepository;
import it.mahmoud.advmanagement.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryCreateDTO categoryCreateDTO) {
        // Check if category with same name already exists
        if (categoryRepository.existsByNameIgnoreCase(categoryCreateDTO.getName())) {
            throw DuplicateResourceException.category(categoryCreateDTO.getName());
        }

        // Create category
        Category category = Category.builder()
                .name(categoryCreateDTO.getName())
                .description(categoryCreateDTO.getDescription())
                .build();

        // Set parent category if provided
        if (categoryCreateDTO.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(categoryCreateDTO.getParentCategoryId())
                    .orElseThrow(() -> ResourceNotFoundException.category(categoryCreateDTO.getParentCategoryId().toString()));

            category.setParentCategory(parentCategory);
        }

        category = categoryRepository.save(category);

        return mapToDTO(category);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryCreateDTO categoryCreateDTO) {
        // Find category
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.category(id.toString()));

        // Check for duplicate name
        categoryRepository.findByNameIgnoreCase(categoryCreateDTO.getName())
                .ifPresent(existingCategory -> {
                    // If another category with the same name exists
                    if (!existingCategory.getId().equals(id)) {
                        throw DuplicateResourceException.category(categoryCreateDTO.getName());
                    }
                });

        // Update fields
        category.setName(categoryCreateDTO.getName());
        category.setDescription(categoryCreateDTO.getDescription());

        // Update parent if changed
        Long newParentId = categoryCreateDTO.getParentCategoryId();

        // If current parent is null
        if (category.getParentCategory() == null) {
            if (newParentId != null) {
                // Add parent
                Category parentCategory = categoryRepository.findById(newParentId)
                        .orElseThrow(() -> ResourceNotFoundException.category(newParentId.toString()));

                // Prevent circular hierarchy
                if (isCircularDependency(id, newParentId)) {
                    throw InvalidOperationException.categoryCircularReference(id.toString(), newParentId.toString());
                }

                category.setParentCategory(parentCategory);
            }
        } else {
            // Current parent is not null
            Long currentParentId = category.getParentCategory().getId();

            if (newParentId == null) {
                // Remove parent
                category.setParentCategory(null);
            } else if (!currentParentId.equals(newParentId)) {
                // Change parent
                Category parentCategory = categoryRepository.findById(newParentId)
                        .orElseThrow(() -> ResourceNotFoundException.category(newParentId.toString()));

                // Prevent circular hierarchy
                if (isCircularDependency(id, newParentId)) {
                    throw InvalidOperationException.categoryCircularReference(id.toString(), newParentId.toString());
                }

                category.setParentCategory(parentCategory);
            }
        }

        category = categoryRepository.save(category);

        return mapToDTO(category);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.category(id.toString()));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryByName(String name) {
        return categoryRepository.findByNameIgnoreCase(name)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.category(name));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean categoryExists(String name) {
        return categoryRepository.existsByNameIgnoreCase(name);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.category(id.toString()));

        List<Category> subcategories = categoryRepository.findByParentCategoryId(id, Sort.unsorted());
        if (!subcategories.isEmpty()) {
            throw InvalidOperationException.categoryHasSubcategories(id.toString());
        }

        if (!category.getAds().isEmpty()) {
            throw InvalidOperationException.categoryHasAds(id.toString());
        }

        categoryRepository.delete(category);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getTopLevelCategories() {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return categoryRepository.findByParentCategoryIsNull(sort)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getSubcategoriesByParentId(Long parentId) {
        Sort sort = Sort.by(Sort.Direction.ASC, "name");
        return categoryRepository.findByParentCategoryId(parentId, sort)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> searchCategories(String searchTerm, Pageable pageable) {
        Specification<Category> spec = CategorySpecifications.nameContains(searchTerm)
                .or(CategorySpecifications.descriptionContains(searchTerm));

        return categoryRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryPath(Long categoryId) {
        List<Object[]> path = categoryRepository.findCategoryPath(categoryId);

        if (path.isEmpty()) {
            throw ResourceNotFoundException.category(categoryId.toString());
        }

        // Convert to DTOs
        return path.stream()
                .map(row -> {
                    Long id = (Long) row[0];
                    String name = (String) row[1];
                    Long parentId = (Long) row[2];

                    return CategoryDTO.builder()
                            .id(id)
                            .name(name)
                            .parentCategoryId(parentId)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategorySelectDTO> getCategorySelectList() {
        List<Category> allCategories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        return allCategories.stream()
                .map(category -> {
                    boolean hasSubcategories = !category.getSubcategories().isEmpty();

                    if (!hasSubcategories && category.getId() != null) {
                        hasSubcategories = !categoryRepository.findByParentCategoryId(category.getId(), Sort.unsorted()).isEmpty();
                    }

                    return CategorySelectDTO.builder()
                            .id(category.getId())
                            .name(category.getName())
                            .parentId(category.getParentCategory() != null ? category.getParentCategory().getId() : null)
                            .hasSubcategories(hasSubcategories)
                            .build();
                })
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public long countAdsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.category(categoryId.toString()));

        return category.getAds().size();
    }

    @Override
    @Transactional(readOnly = true)
    public long countAdsByCategoryHierarchy(Long categoryId) {
        // Ensure category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw ResourceNotFoundException.category(categoryId.toString());
        }

        return categoryRepository.countAdsByCategoryHierarchy(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getCategoriesWithMostAds(Pageable pageable) {
        return categoryRepository.findCategoriesWithMostAds(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryDTO> getUnusedCategories(Pageable pageable) {
        return categoryRepository.findUnusedCategories(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getCategoryTree() {
        // Get all categories
        List<Category> allCategories = categoryRepository.findAll();

        // Group by parent ID for faster access
        Map<Long, List<Category>> childrenMap = new HashMap<>();
        for (Category category : allCategories) {
            Long parentId = category.getParentCategory() != null ? category.getParentCategory().getId() : null;
            childrenMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(category);
        }

        // Start with top-level categories
        List<Category> rootCategories = childrenMap.getOrDefault(null, Collections.emptyList());

        // Build tree recursively
        return buildCategoryTree(rootCategories, childrenMap);
    }

    @Override
    @Transactional
    public CategoryDTO moveCategory(Long categoryId, Long newParentId) {
        // Find category to move
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> ResourceNotFoundException.category(categoryId.toString()));

        // Check for circular dependency
        if (newParentId != null && isCircularDependency(categoryId, newParentId)) {
            throw InvalidOperationException.categoryCircularReference(categoryId.toString(), newParentId.toString());
        }

        // Handle parent change
        if (newParentId == null) {
            // Move to top level
            category.setParentCategory(null);
        } else {
            // Move under new parent
            Category newParent = categoryRepository.findById(newParentId)
                    .orElseThrow(() -> ResourceNotFoundException.category(newParentId.toString()));

            category.setParentCategory(newParent);
        }

        category = categoryRepository.save(category);

        return mapToDTO(category);
    }

    /**
     * Recursively build a category tree
     */
    private List<CategoryDTO> buildCategoryTree(List<Category> categories, Map<Long, List<Category>> childrenMap) {
        List<CategoryDTO> result = new ArrayList<>();

        for (Category category : categories) {
            CategoryDTO dto = mapToDTO(category);

            // Find children
            List<Category> children = childrenMap.getOrDefault(category.getId(), Collections.emptyList());

            // Recursively build subtree
            if (!children.isEmpty()) {
                dto.setSubcategories(new HashSet<>(buildCategoryTree(children, childrenMap)));
            }

            result.add(dto);
        }

        return result;
    }

    /**
     * Check if adding a parent would create a circular dependency
     */
    private boolean isCircularDependency(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }

        // Check parent's ancestry
        Long currentParentId = parentId;
        Set<Long> visitedIds = new HashSet<>();

        while (currentParentId != null) {
            // Avoid infinite loop
            if (visitedIds.contains(currentParentId)) {
                return true;
            }
            visitedIds.add(currentParentId);

            // Check if the current parent is our original category
            if (currentParentId.equals(categoryId)) {
                return true;
            }

            // Move up the hierarchy
            Optional<Category> parent = categoryRepository.findById(currentParentId);
            if (parent.isPresent() && parent.get().getParentCategory() != null) {
                currentParentId = parent.get().getParentCategory().getId();
            } else {
                currentParentId = null;
            }
        }

        return false;
    }

    /**
     * Map Category entity to CategoryDTO
     */
    private CategoryDTO mapToDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();

        // Add parent category info if exists
        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
            dto.setParentCategoryName(category.getParentCategory().getName());
        }

        // Calculate ads count
        dto.setAdsCount(category.getAds() != null ? category.getAds().size() : 0);

        return dto;
    }
}