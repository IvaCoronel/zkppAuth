package com.github.ivacoronel.server.services.impl;

import com.github.ivacoronel.server.web.dtos.DiaryDto;
import com.github.ivacoronel.server.domain.model.Diary;
import com.github.ivacoronel.server.domain.repositories.DiaryRepository;
import com.github.ivacoronel.server.services.DiaryService;
import com.github.ivacoronel.server.services.UserService;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiaryServiceImpl implements DiaryService {

    private final ModelMapper mapper;

    private final DiaryRepository repository;
    
    @Autowired
    @Lazy
    private UserService userService;
   
    @Autowired
    public DiaryServiceImpl(ModelMapper mapper, DiaryRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    @Override
    @Transactional
    public DiaryDto add(DiaryDto dto, String token) {
    	userService.fetch(dto.getUsername(), token);
        Diary diary = mapper.map(dto, Diary.class);
        Diary newdiary = repository.save(diary);
        return mapper.map(newdiary, DiaryDto.class);
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeByUsernameAndEntryname(String username, String entryname, String token) {
    	userService.fetch(username, token);
        repository.deleteByUsernameAndEntryname(username, entryname);
    }

    @Override
    @Transactional
    public DiaryDto fetch(String username, String entryname, String token) {
    	userService.fetch(username, token);
        Diary diary = repository.findByUsernameAndEntryname(username, entryname).orElseThrow(() -> new EmptyResultDataAccessException("No diary entry found for user: " + username + " and entry " + entryname, 1));
        return mapper.map(diary, DiaryDto.class);
    }

    @Override
    @Transactional
    public List<DiaryDto> fetchByUsername(String username, String token) {
    	userService.fetch(username, token);
        List<Diary> entries = repository.findByUsername(username);
        return entries.stream().map(d -> mapper.map(d, DiaryDto.class)).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removeAll(String username, String token) {
        userService.fetch(username, token);
        List<Diary> list = repository.findByUsername(username);
        for (Diary d : list)
            repository.deleteByUsernameAndEntryname(username, d.getEntryname());
    }
}