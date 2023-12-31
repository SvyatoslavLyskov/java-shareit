package ru.practicum.shareit.item.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;

@NoArgsConstructor
@Entity
@AllArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Table(name = "items")
public class Item {
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    Long id;
    @Column(name = "item_name", nullable = false)
    String name;
    @Column(name = "item_description", nullable = false, length = MAX_DESCRIPTION_LENGTH)
    String description;
    @Column(name = "is_available", nullable = false)
    Boolean available;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "owner_id", nullable = false)
    User owner;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "request_id")
    ItemRequest request;
}