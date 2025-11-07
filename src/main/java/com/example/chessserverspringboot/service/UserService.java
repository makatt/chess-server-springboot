package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.model.User;
import com.example.chessserverspringboot.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;

    public UserService(UserRepository repo) { this.repo = repo; }

    public User register(String username, String password) {
        if (repo.findByUsername(username) != null) return null;
        return repo.save(new User(username, password));
    }

    public User get(String username) { return repo.findByUsername(username); }
    public List<User> getAll() { return repo.findAll(); }
}
