package ru.practicum.explorewithme.user.mapper;

import org.junit.jupiter.api.Test;
import ru.practicum.explorewithme.mapper.UserMapper;
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.entity.User;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {
    @Test
    void toUser_shouldMapNewUserRequestToUser() {
        NewUserRequest request = NewUserRequest.builder()
                .name("Test User")
                .email("test-user@mail.com")
                .build();

        User result = UserMapper.toUser(request);

        assertAll(
                () -> assertNull(result.getId()),
                () -> assertEquals(request.getName(), result.getName()),
                () -> assertEquals(request.getEmail(), result.getEmail())
        );
    }

    @Test
    void toUserDto_shouldMapUserToUserDto() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test-user@mail.com")
                .build();

        UserDto result = UserMapper.toUserDto(user);

        assertAll(
                () -> assertEquals(user.getId(), result.getId()),
                () -> assertEquals(user.getName(), result.getName()),
                () -> assertEquals(user.getEmail(), result.getEmail())
        );
    }

    @Test
    void toUserShortDto_shouldMapUserToUserShortDto() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test-user@mail.com")
                .build();

        UserShortDto result = UserMapper.toUserShortDto(user);

        assertAll(
                () -> assertEquals(user.getId(), result.getId()),
                () -> assertEquals(user.getName(), result.getName())
        );
    }
}
