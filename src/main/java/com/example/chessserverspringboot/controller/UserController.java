package com.example.chessserverspringboot.controller;

import com.example.chessserverspringboot.model.User;
import com.example.chessserverspringboot.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }

    @PostMapping("/register")
    public User register(@RequestParam String username, @RequestParam String password) {
        return service.register(username, password);
    }

    @GetMapping
    public List<User> getAll() { return service.getAll(); }

    @GetMapping("/{username}")
    public User get(@PathVariable String username) {
        return service.get(username);
    }
}
