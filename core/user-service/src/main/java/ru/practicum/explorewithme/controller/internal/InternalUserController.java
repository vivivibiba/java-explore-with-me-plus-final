package ru.practicum.explorewithme.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.repository.UserRepository;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalUserController {
    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    public UserShortDto getUser(@PathVariable long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(Entities.USER, userId));
        return UserMapper.toUserShortDto(user);
    }
}
