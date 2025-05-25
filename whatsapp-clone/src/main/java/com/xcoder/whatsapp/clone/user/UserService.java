package com.xcoder.whatsapp.clone.user;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public List<UserResponse> getAllUsersExceptSelf(Authentication connectedUser) {
        return userRepository.findAllUsersExceptSelf(connectedUser.getName())
            .stream()
            .map(userMapper::toUserResponse)
            .toList();
    }
}
