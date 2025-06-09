package com.github.ivacoronel.server.services;

import java.util.List;

import com.github.ivacoronel.server.web.dtos.DiaryDto;

public interface DiaryService {

	public DiaryDto add(DiaryDto dto, String sessionId);
	
    public void removeByUsernameAndEntryname(String username, String entryname, String sessionId);
    
    public void removeAll(String username, String sessionId);
    
    public DiaryDto fetch(String username, String entryname, String sessionId);

    public List<DiaryDto> fetchByUsername(String username, String sessionId);
}