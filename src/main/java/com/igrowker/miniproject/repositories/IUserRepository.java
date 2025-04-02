package com.igrowker.miniproject.repositories;

import com.igrowker.miniproject.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IUserRepository extends JpaRepository<User, Long> {
}
