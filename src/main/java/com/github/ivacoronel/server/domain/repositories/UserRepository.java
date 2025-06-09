package com.github.ivacoronel.server.domain.repositories;

import com.github.ivacoronel.server.domain.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.RepositoryDefinition;


@RepositoryDefinition(domainClass = User.class, idClass = Long.class)
public interface UserRepository extends JpaRepository<User, Long> {
    
    List<User> findAll();

    Page<User> findAll(Pageable pageable);

    Optional<User> findByName(String name);

    User save(User user);

    void deleteByName(String name);
}