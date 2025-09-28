package com.solo.ptmatch.user.infrastructure;

import com.solo.ptmatch.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
