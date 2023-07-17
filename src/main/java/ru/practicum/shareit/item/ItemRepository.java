package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findItemsByOwnerId(Long ownerId);

    List<Item> findByAvailableTrueAndDescriptionContainingIgnoreCaseOrNameContainingIgnoreCase(String text, String texts);
}

