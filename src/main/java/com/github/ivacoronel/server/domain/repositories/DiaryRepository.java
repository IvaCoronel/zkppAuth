package com.github.ivacoronel.server.domain.repositories;

import com.github.ivacoronel.server.domain.model.Diary;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;


@RepositoryDefinition(domainClass = Diary.class, idClass = Long.class)
public interface DiaryRepository extends JpaRepository<Diary, Long> {
    
    List<Diary> findAll();

    Page<Diary> findAll(Pageable pageable);

    Optional<Diary> findByUsernameAndEntryname(String username, String entryname);
    
    List<Diary> findByUsername(String username);
  
    Diary save(Diary diary);

    void deleteByUsernameAndEntryname(String username, String entryname);
}