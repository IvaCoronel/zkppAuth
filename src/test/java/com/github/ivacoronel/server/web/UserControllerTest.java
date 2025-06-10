package com.github.ivacoronel.server.web;

import com.github.ivacoronel.server.web.dtos.ChallengeDto;
import com.github.ivacoronel.server.web.dtos.ErrorCodes;
import com.github.ivacoronel.server.web.dtos.UserDto;
import com.github.ivacoronel.server.config.JsonConfiguration;
import com.github.ivacoronel.server.domain.model.constraints.UserNameUnique;
import com.github.ivacoronel.server.services.UserService;
import com.github.ivacoronel.server.web.controllers.UserController;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ComponentScan(basePackageClasses = UserNameUnique.class)
@ContextConfiguration(classes = JsonConfiguration.class)
@WebMvcTest(controllers = UserController.class)
@SuppressWarnings("PMD.TooManyStaticImports")
@WithMockUser
public class UserControllerTest {

    @Autowired
    private ObjectWriter writer;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserService service;

    @Value("{$test.username}")
    private String username;

    @Value("${test.passwordless}")
    private String pass;

    private final static String users = "/zkauth/users";
    private final static String usersMike = "/zkauth/users/Mike";
    private final static String NOT_FOUND = "Not found";
    private final static String message = "$.message";
    private final static String errorCode = "$.errorCode";
    private final static String errors = "$.errors";

    @Test
    @WithMockUser(username = "user")
    public void registerSuccess() throws Exception {
        UserDto request = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        UserDto result = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        when(service.register(any(UserDto.class))).thenReturn(result);
        mvc.perform(post(users)
                        .with(csrf())
                .content(writer.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(equalTo(1))))
                .andExpect(jsonPath("$.name", is(equalTo(username))))
                .andExpect(jsonPath("$.passwordless", is(equalTo(pass))));

        verify(service, times(1)).register(any(UserDto.class));
        verifyNoMoreInteractions(service);

    }

    @Test
    @WithMockUser(username = "user")
    public void UnknownPath() throws Exception {
        UserDto request = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        mvc.perform(post("/unknown")
                        .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    public void IncorrectMethod() throws Exception {
        UserDto request = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        mvc.perform(get(users)
                        .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.METHOD_NOT_ALLOWED.toString()))))
                .andExpect(jsonPath(errors).isArray())
                .andExpect(jsonPath("$.errors[0].actualMethod", is(equalTo("GET"))))
                .andExpect(jsonPath("$.errors[0].supportedMethods").isArray())
                .andExpect(jsonPath("$.errors[0].supportedMethods[0]", is(equalTo("POST"))));
    }

    @Test
    @WithMockUser(username = "user")
    public void InternalServerError() throws Exception {
        ChallengeDto request = ChallengeDto.builder().challenge(pass).build();

        mvc.perform(post(users)
                        .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.UNKNOWN.toString()))))
                .andExpect(jsonPath(errors).isArray());
    }

    @Test
    @WithMockUser(username = "user")
    public void registerValidationFailedUniqueName() throws Exception {
        UserDto request = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        when(service.register(any(UserDto.class)))
                .thenThrow(new DataIntegrityViolationException("",
                        new ConstraintViolationException("", null, UserNameUnique.CONSTRAINT_NAME)));
        mvc.perform(post(users)
                        .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.DATA_VALIDATION.toString()))))
                .andExpect(jsonPath(errors).isArray())
                .andExpect(jsonPath("$.errors[0].fieldName", is(equalTo("name"))))
                .andExpect(jsonPath("$.errors[0].errorCode", is(equalTo("UNIQUE"))));

        verify(service, times(1)).register(any(UserDto.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(username = "user")
    public void dataIntegrityViolation() throws Exception {
        UserDto request = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        when(service.register(any(UserDto.class)))
                .thenThrow(new DataIntegrityViolationException("",
                        new LockAcquisitionException("", null, UserNameUnique.CONSTRAINT_NAME)));
        mvc.perform(post(users)
                        .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.UNKNOWN.toString()))))
                .andExpect(jsonPath(errors).isArray());

        verify(service, times(1)).register(any(UserDto.class));
        verifyNoMoreInteractions(service);
    }
    @Test
    @WithMockUser(username = "user")
    public void fetchSuccess() throws Exception {
        UserDto result = UserDto.builder()
                .id(1L)
                .name(username)
                .passwordless(pass)
                .build();

        when(service.fetch(any(String.class), any(String.class))).thenReturn(result);
        mvc.perform(get(usersMike)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(equalTo(1))))
                .andExpect(jsonPath("$.name", is(equalTo(username))))
                .andExpect(jsonPath("$.passwordless", is(equalTo(pass))));

        verify(service, times(1)).fetch(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void fetchNotFound() throws Exception {
        when(service.fetch(any(String.class), any(String.class)))
        .thenThrow(new EmptyResultDataAccessException("Not found",0));
        mvc.perform(get(usersMike)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.NOT_FOUND.toString()))))
                .andExpect(jsonPath(message, is(equalTo(NOT_FOUND))));

        verify(service, times(1)).fetch(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void fetchWrongToken() throws Exception {
        when(service.fetch(any(String.class), any(String.class)))
        .thenThrow(new AccessDeniedException(pass));
        mvc.perform(get(usersMike)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.UNAUTHORIZED.toString()))))
                .andExpect(jsonPath("$.challenge", is(equalTo(pass))))
                .andExpect(jsonPath(message, is(equalTo("Unauthorized"))));

        verify(service, times(1)).fetch(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(username = "user")
    public void removeSuccess() throws Exception {
        doNothing().when(service).removeByName(any(String.class), any(String.class));
        mvc.perform(delete(usersMike)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(service, times(1)).removeByName(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(username = "user")
    public void removeNotFound() throws Exception {
        doThrow(new EmptyResultDataAccessException("Not found",0)).when(service).removeByName(any(String.class), any(String.class));
        mvc.perform(delete(usersMike)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.NOT_FOUND.toString()))))
                .andExpect(jsonPath(message, is(equalTo(NOT_FOUND))));

        verify(service, times(1)).removeByName(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(username = "user")
    public void removeWrongToken() throws Exception {
        doThrow(new AccessDeniedException(pass)).when(service).removeByName(any(String.class), any(String.class));
        mvc.perform(delete(usersMike)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.UNAUTHORIZED.toString()))))
                .andExpect(jsonPath("$.challenge", is(equalTo(pass))))
                .andExpect(jsonPath(message, is(equalTo("Unauthorized"))));

        verify(service, times(1)).removeByName(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }
}
