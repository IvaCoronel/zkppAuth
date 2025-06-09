package com.github.ivacoronel.server.services;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;

import com.github.ivacoronel.server.domain.model.User;
import com.github.ivacoronel.server.web.dtos.UserDto;

public interface UserService {

    public UserDto register(UserDto dto);

    public void removeByName(String name, String sessionId);
    
    public UserDto fetch(String name, String sessionId) throws AccessDeniedException, EmptyResultDataAccessException;

    public void generateServerSecret(User user);
}