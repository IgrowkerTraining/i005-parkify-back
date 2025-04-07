package com.igrowker.feature.parkify.features.user.repository;

import com.igrowker.feature.parkify.features.user.entities.User;
import org.springframework.data.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
}
