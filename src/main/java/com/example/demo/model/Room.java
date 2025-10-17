package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private RoomType type;
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Device> devices = new ArrayList<>();
    
    public enum RoomType {
        LIVING_ROOM, BEDROOM, KITCHEN, BATHROOM, STUDY
    }
}