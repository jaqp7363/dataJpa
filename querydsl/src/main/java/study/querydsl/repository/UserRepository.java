package study.querydsl.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import study.querydsl.Entity.User;

public interface UserRepository extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);
}
