package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.User;
import com.asal.ecommerce.enums.Role;
import com.asal.ecommerce.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPasswordAndRoleAndProvider(String email, String password, Role role, Provider provider);
    Optional<User> findByGoogleIdAndProvider(String googleId, Provider provider);
    Optional<User> findByVerificationToken(String verificationToken);
    Optional<User> findByLoginToken(String loginToken);
    boolean existsByEmailAndRole(String email, Role role);
    long countByRole(Role role);
    List<User> findAllByRoleAndEmailVerifiedTrue(Role role);
}
