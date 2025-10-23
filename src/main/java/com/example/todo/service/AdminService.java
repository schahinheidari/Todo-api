package com.example.todo.service;

import com.example.todo.model.dto.UserDTO;
import com.example.todo.model.entity.User;
import com.example.todo.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class AdminService {
    private final UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();

        return users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoles(user.getRoles());
        // توجه: رمز عبور در DTO قرار نمی‌گیرد برای امنیت
        return dto;
    }
}
