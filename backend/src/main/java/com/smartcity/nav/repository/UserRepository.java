package com.smartcity.nav.repository;

import com.smartcity.nav.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * JOIN FETCH pulls the Role in the SAME query, instead of leaving it as a
     * lazy proxy. This matters specifically because JwtAuthFilter calls
     * loadUserByUsername() -> findByEmail() on every authenticated request,
     * and accesses user.getRole().getName() afterward - if the Hibernate
     * session has already closed by then (which it reliably has, in a
     * filter running ahead of Spring MVC's request-scoped session handling),
     * that lazy access throws LazyInitializationException. Fetching eagerly
     * here removes the dependency on session lifetime entirely.
     */
    @Query("SELECT u FROM User u JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    boolean existsByEmail(String email);
}
