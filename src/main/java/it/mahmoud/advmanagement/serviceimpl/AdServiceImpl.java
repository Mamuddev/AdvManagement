package it.mahmoud.advmanagement.serviceimpl;

import it.mahmoud.advmanagement.config.AdSpecifications;
import it.mahmoud.advmanagement.dto.ad.*;
import it.mahmoud.advmanagement.exception.ApiErrorCode;
import it.mahmoud.advmanagement.exception.InvalidOperationException;
import it.mahmoud.advmanagement.exception.ResourceNotFoundException;
import it.mahmoud.advmanagement.model.Ad;
import it.mahmoud.advmanagement.model.Category;
import it.mahmoud.advmanagement.model.Tag;
import it.mahmoud.advmanagement.model.User;
import it.mahmoud.advmanagement.repo.AdRepository;
import it.mahmoud.advmanagement.repo.CategoryRepository;
import it.mahmoud.advmanagement.repo.TagRepository;
import it.mahmoud.advmanagement.repo.UserRepository;
import it.mahmoud.advmanagement.service.AdService;
import it.mahmoud.advmanagement.util.AdStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Implementation of AdService
 */
@Service
public class AdServiceImpl implements AdService {

    private final AdRepository adRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Autowired
    public AdServiceImpl(
            AdRepository adRepository,
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository) {
        this.adRepository = adRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    @Transactional
    public AdDTO createAd(AdCreateDTO adCreateDTO) {
        // Find creator
        User creator = userRepository.findById(adCreateDTO.getCreatorId())
                .orElseThrow(() -> ResourceNotFoundException.user(adCreateDTO.getCreatorId().toString()));

        // Create new ad
        Ad ad = Ad.builder()
                .title(adCreateDTO.getTitle())
                .description(adCreateDTO.getDescription())
                .price(adCreateDTO.getPrice())
                .expirationDate(adCreateDTO.getExpirationDate())
                .featured(adCreateDTO.getFeatured() != null ? adCreateDTO.getFeatured() : false)
                .status(adCreateDTO.getStatus() != null ? adCreateDTO.getStatus() : AdStatus.DRAFT)
                .creator(creator)
                .creationDate(LocalDateTime.now())
                .views(0)
                .build();

        // Add categories
        if (adCreateDTO.getCategoryIds() != null && !adCreateDTO.getCategoryIds().isEmpty()) {
            Set<Category> categories = adCreateDTO.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> ResourceNotFoundException.category(categoryId.toString())))
                    .collect(Collectors.toSet());

            for (Category category : categories) {
                ad.addCategory(category);
            }
        }

        // Add tags
        if (adCreateDTO.getTagIds() != null && !adCreateDTO.getTagIds().isEmpty()) {
            Set<Tag> tags = adCreateDTO.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> ResourceNotFoundException.tag(tagId.toString())))
                    .collect(Collectors.toSet());

            for (Tag tag : tags) {
                ad.addTag(tag);
            }
        }

        ad = adRepository.save(ad);

        return mapToDTO(ad);
    }

    @Override
    @Transactional
    public AdDTO updateAd(Long id, AdUpdateDTO adUpdateDTO) {
        // Validate ID match
        if (!id.equals(adUpdateDTO.getId())) {
            throw new InvalidOperationException(
                    ApiErrorCode.VALIDATION_ERROR,
                    "ID in path does not match ID in request body");
        }

        // Find ad
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.ad(id.toString()));

        // Update fields
        ad.setTitle(adUpdateDTO.getTitle());
        ad.setDescription(adUpdateDTO.getDescription());
        ad.setPrice(adUpdateDTO.getPrice());
        ad.setExpirationDate(adUpdateDTO.getExpirationDate());
        ad.setFeatured(adUpdateDTO.getFeatured());

        if (adUpdateDTO.getStatus() != null) {
            ad.setStatus(adUpdateDTO.getStatus());

            // If publishing for the first time, set publication date
            if (adUpdateDTO.getStatus() == AdStatus.PUBLISHED && ad.getPublicationDate() == null) {
                ad.setPublicationDate(LocalDateTime.now());
            }
        }

        ad.setModificationDate(LocalDateTime.now());

        // Update categories
        if (adUpdateDTO.getCategoryIds() != null) {
            // Clear current categories
            ad.getCategories().clear();

            // Add new categories
            adUpdateDTO.getCategoryIds().stream()
                    .map(categoryId -> categoryRepository.findById(categoryId)
                            .orElseThrow(() -> ResourceNotFoundException.category(categoryId.toString())))
                    .forEach(ad::addCategory);
        }

        // Update tags
        if (adUpdateDTO.getTagIds() != null) {
            // Clear current tags
            ad.getTags().clear();

            // Add new tags
            adUpdateDTO.getTagIds().stream()
                    .map(tagId -> tagRepository.findById(tagId)
                            .orElseThrow(() -> ResourceNotFoundException.tag(tagId.toString())))
                    .forEach(ad::addTag);
        }

        ad = adRepository.save(ad);

        return mapToDTO(ad);
    }

    @Override
    @Transactional(readOnly = true)
    public AdDTO getAdById(Long id) {
        return adRepository.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(() -> ResourceNotFoundException.ad(id.toString()));
    }

    @Override
    @Transactional
    public void deleteAd(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.ad(id.toString()));

        // Soft delete - change status to DELETED
        ad.setStatus(AdStatus.DELETED);
        adRepository.save(ad);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAllAds(Pageable pageable) {
        return adRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAdsByStatus(AdStatus status, Pageable pageable) {
        return adRepository.findByStatus(status, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAdsByCreator(Long creatorId, Pageable pageable) {
        return adRepository.findByCreatorId(creatorId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAdsByCategory(Long categoryId, Pageable pageable) {
        return adRepository.findByCategoryId(categoryId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAdsByTag(Long tagId, Pageable pageable) {
        return adRepository.findByTagId(tagId, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> searchAds(String query, Pageable pageable) {
        return adRepository.fullTextSearch(query, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> advancedSearch(AdSearchDTO searchDTO, Pageable pageable) {
        // Build specification
        Specification<Ad> spec = Specification.where(null);

        // Add criteria
        if (searchDTO.getStatus() != null) {
            spec = spec.and(AdSpecifications.hasStatus(searchDTO.getStatus()));
        } else {
            // Default to published ads only
            spec = spec.and(AdSpecifications.hasStatus(AdStatus.PUBLISHED));
        }

        if (searchDTO.getCategoryId() != null) {
            spec = spec.and(AdSpecifications.inCategory(searchDTO.getCategoryId()));
        }

        if (searchDTO.getTagIds() != null && !searchDTO.getTagIds().isEmpty()) {
            spec = spec.and(AdSpecifications.hasAnyTag(new ArrayList<>(searchDTO.getTagIds())));
        }

        if (searchDTO.getMinPrice() != null || searchDTO.getMaxPrice() != null) {
            spec = spec.and(AdSpecifications.priceInRange(searchDTO.getMinPrice(), searchDTO.getMaxPrice()));
        }

        if (searchDTO.getQuery() != null && !searchDTO.getQuery().trim().isEmpty()) {
            spec = spec.and(AdSpecifications.containsText(searchDTO.getQuery()));
        }

        if (searchDTO.getFeatured() != null) {
            spec = spec.and(AdSpecifications.isFeatured(searchDTO.getFeatured()));
        }

        if (searchDTO.getCreatorId() != null) {
            spec = spec.and(AdSpecifications.createdBy(searchDTO.getCreatorId()));
        }

        return adRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional
    public AdDTO updateAdStatus(AdStatusUpdateDTO statusUpdateDTO) {
        Ad ad = adRepository.findById(statusUpdateDTO.getId())
                .orElseThrow(() -> ResourceNotFoundException.ad(statusUpdateDTO.getId().toString()));

        AdStatus oldStatus = ad.getStatus();
        AdStatus newStatus = statusUpdateDTO.getNewStatus();

        // Validate status transition
        validateStatusTransition(oldStatus, newStatus, ad.getId().toString());

        ad.setStatus(newStatus);

        // Handle specific status transitions
        if (oldStatus != AdStatus.PUBLISHED && newStatus == AdStatus.PUBLISHED) {
            // Publishing for the first time
            ad.setPublicationDate(LocalDateTime.now());

            // Set expiration date if provided
            if (statusUpdateDTO.getExpirationDate() != null) {
                ad.setExpirationDate(statusUpdateDTO.getExpirationDate());
            }
        }

        ad = adRepository.save(ad);

        return mapToDTO(ad);
    }

    @Override
    @Transactional
    public void incrementViews(Long adId) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> ResourceNotFoundException.ad(adId.toString()));

        ad.setViews(ad.getViews() + 1);
        adRepository.save(ad);

        adRepository.flush();
    }

    @Override
    @Transactional
    public AdDTO publishAd(Long adId, LocalDateTime expirationDate) {
        Ad ad = adRepository.findById(adId)
                .orElseThrow(() -> ResourceNotFoundException.ad(adId.toString()));

        // Validate status transition
        validateStatusTransition(ad.getStatus(), AdStatus.PUBLISHED, adId.toString());

        ad.setStatus(AdStatus.PUBLISHED);
        ad.setPublicationDate(LocalDateTime.now());

        if (expirationDate != null) {
            ad.setExpirationDate(expirationDate);
        }

        ad = adRepository.save(ad);

        return mapToDTO(ad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdDTO> findAdsExpiringSoon(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);

        return adRepository.findByStatusAndExpirationDateBetween(AdStatus.PUBLISHED, now, future)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getFeaturedAds(Pageable pageable) {
        return adRepository.findByFeaturedTrueAndStatus(AdStatus.PUBLISHED, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getRecentlyPublishedAds(int days, Pageable pageable) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        return adRepository.findRecentlyPublished(startDate, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getAdsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        Specification<Ad> spec = Specification.where(AdSpecifications.hasStatus(AdStatus.PUBLISHED))
                .and(AdSpecifications.priceInRange(minPrice, maxPrice));

        return adRepository.findAll(spec, pageable).map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdDTO> getMostViewedAds(Pageable pageable) {
        return adRepository.findByStatusOrderByViewsDesc(AdStatus.PUBLISHED, pageable)
                .map(this::mapToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdSummaryDTO> getAdsSummary(Pageable pageable) {
        return adRepository.findByStatus(AdStatus.PUBLISHED, pageable)
                .map(this::mapToSummaryDTO);
    }

    @Override
    @Transactional
    public int markExpiredAds() {
        return adRepository.updateExpiredAds(LocalDateTime.now());
    }

    /**
     * Validate that the status transition is allowed
     */
    private void validateStatusTransition(AdStatus currentStatus, AdStatus newStatus, String adId) {
        // Rules for status transitions
        switch (currentStatus) {
            case DRAFT:
                // From DRAFT can go to PUBLISHED, DELETED
                if (newStatus != AdStatus.PUBLISHED && newStatus != AdStatus.DELETED) {
                    throw InvalidOperationException.adInvalidStatus(adId, currentStatus.toString(), newStatus.toString());
                }
                break;
            case PUBLISHED:
                // From PUBLISHED can go to EXPIRED, SUSPENDED, DELETED
                if (newStatus != AdStatus.EXPIRED && newStatus != AdStatus.SUSPENDED && newStatus != AdStatus.DELETED) {
                    throw InvalidOperationException.adInvalidStatus(adId, currentStatus.toString(), newStatus.toString());
                }
                break;
            case EXPIRED:
                // From EXPIRED can go to PUBLISHED (renew), DELETED
                if (newStatus != AdStatus.PUBLISHED && newStatus != AdStatus.DELETED) {
                    throw InvalidOperationException.adInvalidStatus(adId, currentStatus.toString(), newStatus.toString());
                }
                break;
            case SUSPENDED:
                // From SUSPENDED can go to PUBLISHED (reinstate), DELETED
                if (newStatus != AdStatus.PUBLISHED && newStatus != AdStatus.DELETED) {
                    throw InvalidOperationException.adInvalidStatus(adId, currentStatus.toString(), newStatus.toString());
                }
                break;
            case DELETED:
                // From DELETED cannot go anywhere else
                throw InvalidOperationException.adInvalidStatus(adId, currentStatus.toString(), newStatus.toString());
        }
    }

    /**
     * Map Ad entity to AdDTO
     */
    private AdDTO mapToDTO(Ad ad) {
        AdDTO dto = AdDTO.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .description(ad.getDescription())
                .creationDate(ad.getCreationDate())
                .modificationDate(ad.getModificationDate())
                .expirationDate(ad.getExpirationDate())
                .publicationDate(ad.getPublicationDate())
                .status(ad.getStatus())
                .price(ad.getPrice())
                .views(ad.getViews())
                .featured(ad.getFeatured())
                .build();

        // Add creator info
        if (ad.getCreator() != null) {
            dto.setCreatorId(ad.getCreator().getId());
            dto.setCreatorFirstName(ad.getCreator().getFirstName());
            dto.setCreatorLastName(ad.getCreator().getLastName());
        }

        // Add categories
        if (ad.getCategories() != null) {
            Set<Long> categoryIds = new HashSet<>();
            Set<String> categoryNames = new HashSet<>();

            for (Category category : ad.getCategories()) {
                categoryIds.add(category.getId());
                categoryNames.add(category.getName());
            }

            dto.setCategoryIds(categoryIds);
            dto.setCategoryNames(categoryNames);
        }

        // Add tags
        if (ad.getTags() != null) {
            Set<Long> tagIds = new HashSet<>();
            Set<String> tagNames = new HashSet<>();

            for (Tag tag : ad.getTags()) {
                tagIds.add(tag.getId());
                tagNames.add(tag.getName());
            }

            dto.setTagIds(tagIds);
            dto.setTagNames(tagNames);
        }

        return dto;
    }

    /**
     * Map Ad entity to AdSummaryDTO
     */
    private AdSummaryDTO mapToSummaryDTO(Ad ad) {
        String creatorName = ad.getCreator() != null ?
                ad.getCreator().getFirstName() + " " + ad.getCreator().getLastName() : "";

        Set<String> categoryNames = ad.getCategories().stream()
                .map(Category::getName)
                .collect(Collectors.toSet());

        return AdSummaryDTO.builder()
                .id(ad.getId())
                .title(ad.getTitle())
                .price(ad.getPrice())
                .publicationDate(ad.getPublicationDate())
                .views(ad.getViews())
                .featured(ad.getFeatured())
                .creatorName(creatorName)
                .categoryNames(categoryNames)
                .build();
    }
}
