package com.example.chessserverspringboot.Registration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private UserProfileRepository profileRepo;

    // --------------------- REGISTER ------------------------------
    public ResponseEntity<?> register(RegisterRequest req) {

        if (userRepo.existsByEmail(req.email())) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        if (userRepo.existsByUsername(req.username())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        String hash = BCrypt.hashpw(req.password(), BCrypt.gensalt());

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(hash);
        userRepo.save(user);

        // Create profile
        UserProfile profile = new UserProfile();
        profile.setUserId(user.getUserId());
        profileRepo.save(profile);

        return ResponseEntity.ok("Registered successfully");
    }

    // ---------------------- LOGIN -------------------------------
    public ResponseEntity<?> login(LoginRequest req) {

        User user = userRepo.findByUsername(req.username());
        if (user == null)
            return ResponseEntity.badRequest().body("User not found");

        if (!BCrypt.checkpw(req.password(), user.getPasswordHash()))
            return ResponseEntity.badRequest().body("Wrong password");

        user.setLastLogin(LocalDateTime.now());
        userRepo.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("user_id", user.getUserId());
        response.put("username", user.getUsername());

        return ResponseEntity.ok(response);
    }

    // ----------------------- PROFILE -----------------------------
    public ResponseEntity<?> getProfile(int id) {

        User user = userRepo.findById(id).orElse(null);
        UserProfile profile = profileRepo.findById(id).orElse(null);

        if (user == null) return ResponseEntity.badRequest().body("User not found");

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("profile", profile);

        return ResponseEntity.ok(map);
    }
}
