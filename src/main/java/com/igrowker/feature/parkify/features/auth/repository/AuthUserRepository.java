package com.igrowker.feature.parkify.features.auth.repository;

import com.igrowker.feature.parkify.features.auth.entities.AuthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {

    Optional<AuthUser> findByEmail(String email);
}
