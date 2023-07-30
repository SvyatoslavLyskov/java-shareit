package ru.practicum.shareit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum SortType {
    CREATED(Sort.by(Sort.Direction.DESC, "created")),
    START(Sort.by(Sort.Direction.DESC, "start"));

    private final Sort sortValue;
}
