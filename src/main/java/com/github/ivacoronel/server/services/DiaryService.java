package com.github.ivacoronel.server.services;

import java.util.List;

import com.github.ivacoronel.server.web.dtos.DiaryDto;

public interface DiaryService {

	public DiaryDto add(DiaryDto dto, String token);
	
    public void removeByUsernameAndEntryname(String username, String entryname, String token);
    
    public void removeAll(String username, String token);
    
    public DiaryDto fetch(String username, String entryname, String token);

    public List<DiaryDto> fetchByUsername(String username, String token);
}