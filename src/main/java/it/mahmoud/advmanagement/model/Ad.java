package it.mahmoud.advmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ads")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"id", "title"})
@ToString(exclude = {"categories", "tags"})
public class Ad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ad title is required")
    @Size(min = 5, max = 100, message = "Title must be between 5 and 100 characters")
    @Column(nullable = false, length = 100)
    private String title;

    @NotBlank(message = "Ad description is required")
    @Size(min = 20, max = 2000, message = "Description must be between 20 and 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    @CreationTimestamp
    @Column(name = "creation_date", nullable = false, updatable = false)
    private LocalDateTime creationDate;

    @UpdateTimestamp
    @Column(name = "modification_date")
    private LocalDateTime modificationDate;

    @Future(message = "Expiration date must be in the future")
    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "publication_date")
    private LocalDateTime publicationDate;

    @NotNull(message = "Ad status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdStatus status;

    @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Price can have at most 8 integer digits and 2 decimal places")
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Number of views cannot be negative")
    @Column(name = "views")
    @Builder.Default
    private Integer views = 0;

    @Column(name = "featured")
    @Builder.Default
    private Boolean featured = false;

    @NotNull(message = "Ad must have a creator")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany
    @JoinTable(
            name = "ad_category",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "ad_tag",
            joinColumns = @JoinColumn(name = "ad_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // Enum for ad status
    public enum AdStatus {
        DRAFT, PUBLISHED, EXPIRED, SUSPENDED, DELETED
    }

    // Constructor with essential parameters
    public Ad(String title, String description, User creator) {
        this.title = title;
        this.description = description;
        this.creator = creator;
        this.status = AdStatus.DRAFT;
        this.creationDate = LocalDateTime.now();
        this.views = 0;
        this.featured = false;
    }

    // Utility methods
    public void addCategory(Category category) {
        categories.add(category);
        category.getAds().add(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getAds().remove(this);
    }

    public void addTag(Tag tag) {
        tags.add(tag);
        tag.getAds().add(this);
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
        tag.getAds().remove(this);
    }

    public void publishAd(LocalDateTime expirationDate) {
        this.status = AdStatus.PUBLISHED;
        this.publicationDate = LocalDateTime.now();
        this.expirationDate = expirationDate;
    }

    public void incrementViews() {
        this.views++;
    }

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = AdStatus.DRAFT;
        }
        if (this.views == null) {
            this.views = 0;
        }
        if (this.featured == null) {
            this.featured = false;
        }
    }
}