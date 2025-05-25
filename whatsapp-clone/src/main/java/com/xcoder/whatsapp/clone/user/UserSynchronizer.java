package com.xcoder.whatsapp.clone.user;

import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSynchronizer {
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public void synchronizedWithIdp(Jwt token) {
        log.info("Synchronizing user with idp");
        getUserEmail(token).ifPresent(userEmail -> {
            log.info("Synchronizing user having email: {}", userEmail);
            //Optional<User> optUser = userRepository.findByEmail(userEmail);
            User user = userMapper.fromTokenAttributes(token.getClaims());
            //optUser.ifPresent(value -> user.setId(optUser.get().getId()));

            userRepository.save(user);
        });
    }

    private Optional<String> getUserEmail(Jwt token) {
        Map<String, Object> attributes = token.getClaims();

        if (attributes.containsKey("email")) {
            return Optional.of(attributes.get("email").toString());
        }

        return Optional.empty();
    }
}
