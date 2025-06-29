package com.github.ivacoronel.server.services.impl;

import com.github.ivacoronel.server.web.dtos.UserDto;

import com.github.ivacoronel.server.domain.model.User;
import com.github.ivacoronel.server.domain.model.types.SessionStatus;
import com.github.ivacoronel.server.domain.repositories.UserRepository;
import com.github.ivacoronel.server.services.DiaryService;
import com.github.ivacoronel.server.services.UserService;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final ModelMapper mapper;

    private final UserRepository repository;
    
    @Value("${security.session.challengeFrequency}")
    private String chalFreq;
    
    @Value("${security.session.inactivityKickOut}")
    private String inactThreshold;
    
    @Value("${security.crypto.generator}")
    private String generator;
    
    @Value("${security.crypto.prime}")
    private String prime;

    @Autowired
    @Lazy
    private DiaryService diaryService;
   
    @Autowired
    public UserServiceImpl(ModelMapper mapper, UserRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    @Transactional
    public UserDto register(UserDto dto) {
        User user = mapper.map(dto, User.class);
        repository.findByName(user.getName()).ifPresent(u -> { throw new DataIntegrityViolationException("User already exists with name: " + u.getName()); });
        
        User newuser = repository.save(user);
        return mapper.map(newuser, UserDto.class);
    }

    @Override
    @Transactional
    public void removeByName(String name, String token) {
        User user = repository.findByName(name).orElseThrow(() -> new EmptyResultDataAccessException("No user found with name: " + name, 1));
        if (user.getSecret()!=null && verify(user,token))
        {
            diaryService.removeAll(name, token);
            repository.deleteByName(name);
        }
        else 
        {
            throwChallengedException(user);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = {AccessDeniedException.class, EmptyResultDataAccessException.class})
    public UserDto fetch(String name, String token) throws AccessDeniedException, EmptyResultDataAccessException{
        User user = repository.findByName(name).orElseThrow(() -> new EmptyResultDataAccessException("No user found with name: " + name, 1));
        
        if (user.getSecret()!=null && verify(user,token))
        {
            return mapper.map(user, UserDto.class);
        }
        else 
        {
            throwChallengedException(user);
        }
        return null;
    }
    
    private boolean verify(User user,String response)
    {
        BigInteger passwordless = new BigInteger(user.getPasswordless(),16); 
        BigInteger secret = new BigInteger(user.getSecret(), 16);
    
        BigInteger verify = passwordless.modPow(secret, new BigInteger(prime,16));
        
        if (equals(verify.toString(), response))
        {
            if (!user.getSstatus().equals(SessionStatus.VALIDATED))
            {
                user.setSstatus(SessionStatus.VALIDATED);
                repository.save(user);
            }
        }
        else 
        {
            user.setSstatus(SessionStatus.INVALIDATED);
            user.setSecret(null);
            repository.save(user);
            return false;
        }
        return true;
    }
    
    // perform constant time string comparison against timing attacks
    private boolean equals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    private void throwChallengedException(User user) throws AccessDeniedException{
        if (user.getSecret() == null)
        {
            generateServerSecret(user);  
        }
        BigInteger power = new BigInteger(generator,16).modPow(new BigInteger(user.getSecret(),16), new BigInteger(prime,16)); 
        throw new AccessDeniedException(""+power);
    }

    public void generateServerSecret(User olduser) {
    	User user = repository.findByName(olduser.getName()).orElseThrow(() -> new EmptyResultDataAccessException("No user found with name: " + olduser.getName(), 1));
        SecureRandom random = new SecureRandom();
        BigInteger bigint =  new BigInteger(256, random);
        user.setSecret(bigint.toString(16));
        user.setSstatus(SessionStatus.INITIATING);
        repository.save(user);
    }
}