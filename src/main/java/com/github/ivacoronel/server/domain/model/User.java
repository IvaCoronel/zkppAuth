package com.github.ivacoronel.server.domain.model;

import com.github.ivacoronel.server.domain.model.constraints.UserNameUnique;
import com.github.ivacoronel.server.domain.model.types.SessionStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table( name = "users",
        uniqueConstraints =
        {@UniqueConstraint(name = UserNameUnique.CONSTRAINT_NAME, columnNames = UserNameUnique.FIELD_NAME)})
@SequenceGenerator(name = "userSeq", sequenceName = "user_seq")
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(generator = "userSeq", strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotEmpty
    @Column(nullable = false, name = UserNameUnique.FIELD_NAME)
    private String name;

    @NotNull
    @Column(nullable = false, length = 600)
    private String passwordless;
    
    @Column(nullable = true, length = 600)
    private String secret;
    
    @Column
    private SessionStatus sstatus;

}