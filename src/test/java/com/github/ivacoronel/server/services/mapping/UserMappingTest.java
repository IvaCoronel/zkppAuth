package com.github.ivacoronel.server.services.mapping;

import com.github.ivacoronel.server.web.dtos.UserDto;
import com.github.ivacoronel.server.domain.model.User;
import com.github.rozidan.springboot.modelmapper.WithModelMapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@RunWith(SpringRunner.class)
@WithModelMapper(basePackageClasses = MappingBasePackage.class)
public class UserMappingTest {

    @Autowired
    private ModelMapper mapper;

    @Test
    public void productDtoToEntityMappedSuccess() {
        
        UserDto dto = UserDto.builder()
                .id(1L)
                .name("John")
                .passwordless("455632178871263")
                .build();

        User result = mapper.map(dto, User.class);

        assertThat(result.getId(), is(equalTo(1L)));
        assertThat(result.getName(), is(equalTo("John")));
        assertThat(result.getPasswordless(), is(equalTo("455632178871263")));
    }
}
