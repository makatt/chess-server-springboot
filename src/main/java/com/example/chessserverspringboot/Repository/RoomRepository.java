package com.example.chessserverspringboot.Repository;


import com.example.chessserverspringboot.Entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, String> { }
