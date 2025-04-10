package com.igrowker.feature.parkify.features.user.repository;

import com.igrowker.feature.parkify.features.user.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
