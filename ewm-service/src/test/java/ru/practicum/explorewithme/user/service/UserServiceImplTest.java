package ru.practicum.explorewithme.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.explorewithme.dto.user.NewUserRequest;
import ru.practicum.explorewithme.dto.user.UserDto;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.service.UserServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_whenEmailIsUnique_shouldSaveAndReturnUserDto() {
        NewUserRequest request = NewUserRequest.builder()
                .name("Test User")
                .email("test-user@mail.com")
                .build();
        User savedUser = User.builder()
                .id(1L)
                .name(request.getName())
                .email(request.getEmail())
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserDto result = userService.createUser(request);

        assertAll(
                () -> assertEquals(savedUser.getId(), result.getId()),
                () -> assertEquals(savedUser.getName(), result.getName()),
                () -> assertEquals(savedUser.getEmail(), result.getEmail())
        );

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User userToSave = userCaptor.getValue();

        assertAll(
                () -> assertEquals(request.getName(), userToSave.getName()),
                () -> assertEquals(request.getEmail(), userToSave.getEmail())
        );
    }

    @Test
    void createUser_whenEmailAlreadyExists_shouldThrowConflictException() {
        NewUserRequest request = NewUserRequest.builder()
                .name("Test User")
                .email("test-user@mail.com")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        DuplicatedDataException exception = assertThrows(DuplicatedDataException.class, () -> userService.createUser(request));

        assertEquals("USER with parameter 'email'=test-user@mail.com already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUsers_withoutIds_shouldReturnUsersFromPage() {
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test-user@mail.com")
                .build();

        when(userRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(user)));

        List<UserDto> result = userService.getUsers(null, 0, 10);

        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals(user.getId(), result.getFirst().getId()),
                () -> assertEquals(user.getName(), result.getFirst().getName()),
                () -> assertEquals(user.getEmail(), result.getFirst().getEmail())
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAll(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertAll(
                () -> assertEquals(0, pageable.getOffset()),
                () -> assertEquals(10, pageable.getPageSize())
        );
        verify(userRepository, never()).findAllByIdIn(any(), any(Pageable.class));
    }

    @Test
    void getUsers_withIds_shouldReturnOnlyRequestedUsers() {
        List<Long> ids = List.of(1L, 2L);
        User firstUser = User.builder()
                .id(1L)
                .name("First User")
                .email("first-user@mail.com")
                .build();
        User secondUser = User.builder()
                .id(2L)
                .name("Second User")
                .email("second-user@mail.com")
                .build();

        when(userRepository.findAllByIdIn(eq(ids), any(Pageable.class))).thenReturn(List.of(firstUser, secondUser));

        List<UserDto> result = userService.getUsers(ids, 5, 10);

        assertAll(
                () -> assertEquals(2, result.size()),
                () -> assertEquals(firstUser.getId(), result.get(0).getId()),
                () -> assertEquals(secondUser.getId(), result.get(1).getId())
        );

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(userRepository).findAllByIdIn(eq(ids), pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();

        assertAll(
                () -> assertEquals(5, pageable.getOffset()),
                () -> assertEquals(10, pageable.getPageSize())
        );
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> userService.deleteUser(1L));

        verify(userRepository, never()).deleteById(1L);
    }
}
