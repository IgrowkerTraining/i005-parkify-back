package com.igrowker.feature.parkify.repositories;

import com.igrowker.feature.parkify.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {
}
