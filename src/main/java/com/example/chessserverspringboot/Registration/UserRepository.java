package com.example.chessserverspringboot.Registration;
import com.example.chessserverspringboot.Registration.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    User findByUsername(String username);
}

