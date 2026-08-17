package ru.practicum.explorewithme.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.explorewithme.dto.user.UserShortDto;

@FeignClient(name = "user-service")
public interface UserClient {
    @GetMapping("/internal/users/{userId}")
    UserShortDto getUser(@PathVariable("userId") long userId);
}
