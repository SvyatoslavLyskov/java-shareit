package ru.practicum.shareit.item.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.util.List;

@NoArgsConstructor
@Entity
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "items")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    Long id;
    @Column(name = "item_name", nullable = false)
    String name;
    @Column(name = "item_description", nullable = false)
    String description;
    @Column(name = "is_available", nullable = false)
    Boolean available;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "request_id")
    List<ItemRequest> request;
}