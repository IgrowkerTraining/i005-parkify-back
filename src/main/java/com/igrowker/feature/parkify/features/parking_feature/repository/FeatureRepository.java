package com.igrowker.feature.parkify.features.parking_feature.repository;

import com.igrowker.feature.parkify.features.parking_feature.entity.Feature;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, Long> {
    Optional<Feature> findBySlug(String slug);

    Set<Feature> findBySlugIn(Collection<String> slugs);
}
