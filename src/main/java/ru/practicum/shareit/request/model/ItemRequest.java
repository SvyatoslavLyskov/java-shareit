package ru.practicum.shareit.request.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@Entity
@Table(name = "requests")
@AllArgsConstructor
@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemRequest {
    public static final int MAX_DESCRIPTION_LENGTH = 512;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    Long id;
    @Column(name = "request_description", length = MAX_DESCRIPTION_LENGTH)
    String description;
    @ManyToOne
    @JoinColumn(name = "requester_id")
    User requester;
    @Column(name = "creation_time")
    LocalDateTime created;
}
