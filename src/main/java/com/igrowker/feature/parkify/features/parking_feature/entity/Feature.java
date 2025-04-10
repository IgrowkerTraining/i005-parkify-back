package com.igrowker.feature.parkify.features.parking_feature.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "features", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"slug"})
})
public class Feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Feature slug cannot be blank")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase alphanumeric with hyphens")
    @Column(nullable = false, unique = true)
    private String slug;

    @NotBlank(message = "Feature name cannot be blank")
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return java.util.Objects.equals(slug, feature.slug);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(slug);
    }
}
