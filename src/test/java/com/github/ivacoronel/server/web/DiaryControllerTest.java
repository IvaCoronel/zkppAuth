package com.github.ivacoronel.server.web;

import com.github.ivacoronel.server.web.dtos.DiaryDto;
import com.github.ivacoronel.server.web.dtos.ErrorCodes;
import com.github.ivacoronel.server.config.JsonConfiguration;
import com.github.ivacoronel.server.domain.model.constraints.UserEntryNameUnique;
import com.github.ivacoronel.server.domain.model.constraints.UserNameUnique;
import com.github.ivacoronel.server.services.DiaryService;
import com.github.ivacoronel.server.services.UserService;
import com.github.ivacoronel.server.web.controllers.DiaryController;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
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

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@ComponentScan(basePackageClasses = UserNameUnique.class)
@ContextConfiguration(classes = JsonConfiguration.class)
@WebMvcTest(controllers = DiaryController.class)
@WithMockUser
@SuppressWarnings("PMD.TooManyStaticImports")
public class DiaryControllerTest {

    @Autowired
    private ObjectWriter writer;

    @Autowired
    private MockMvc mvc;

    @MockBean
    private DiaryService service;

    @MockBean
    private UserService userService;
    
    @Value("${test.username}")
    private String username;
    
    @Value("${test.entryname}")
    private String entryname;
    
    @Value("${test.passwordless}")
    private String pass;
    
    private final static String diary = "/zkauth/diary";
    private final static String diaryUser = "/zkauth/diary/John/";
    private final static String diaryUserEntry = "/zkauth/diary/John/MyDiary/";
    private final static String errorCode = "$.errorCode";
    private final static String errors = "$.errors";

    @Test
    @WithMockUser(username = "user")
    public void addSuccess() throws Exception {
        DiaryDto request = DiaryDto.builder()
                .id(1L)
                .username(username)
                .entryname(entryname)
                .content(pass)
                .build();

        DiaryDto result = DiaryDto.builder()
                .id(1L)
                .username(username)
                .entryname(entryname)
                .content(pass)
                .build();

        when(service.add(any(DiaryDto.class), eq("some-test-token"))).thenReturn(result);
        mvc.perform(post(diary)
                .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                        .header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(equalTo(1))))
                .andExpect(jsonPath("$.username", is(equalTo(username))))
                .andExpect(jsonPath("$.entryname", is(equalTo(entryname))))
                .andExpect(jsonPath("$.content", is(equalTo(pass))));

        verify(service, times(1)).add(any(DiaryDto.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void registerValidationFailedUniqueName() throws Exception {
        DiaryDto request = DiaryDto.builder()
                .id(1L)
                .username(username)
                .entryname(entryname)
                .content(pass)
                .build();

        when(service.add(any(DiaryDto.class),eq("some-test-token")))
                .thenThrow(new DataIntegrityViolationException("",
                        new ConstraintViolationException("", null, UserEntryNameUnique.CONSTRAINT_NAME)));
        mvc.perform(post(diary)
                 .with(csrf())
                .content(writer.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isConflict())
                .andExpect(jsonPath(errorCode, is(equalTo(ErrorCodes.DATA_VALIDATION.toString()))))
                .andExpect(jsonPath(errors).isArray())
                .andExpect(jsonPath("$.errors[0].fieldName", is(equalTo("username, entryname"))))
                .andExpect(jsonPath("$.errors[0].errorCode", is(equalTo("UNIQUE"))));

        verify(service, times(1)).add(any(DiaryDto.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void fetchSuccess() throws Exception {

        DiaryDto result = DiaryDto.builder()
                .id(1L)
                .username(username)
                .entryname(entryname)
                .content(pass)
                .build();

        List<DiaryDto> list = new ArrayList<>();
        list.add(result);

        when(service.fetchByUsername(any(String.class), eq("some-test-token"))).thenReturn(list);
        mvc.perform(get(diaryUser)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(equalTo(1))))
                .andExpect(jsonPath("$[0].username", is(equalTo(username))))
                .andExpect(jsonPath("$[0].entryname", is(equalTo(entryname))))
                .andExpect(jsonPath("$[0].content", is(equalTo(pass))));

        verify(service, times(1)).fetchByUsername(any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }

    @Test
    public void removeSuccess() throws Exception {
        doNothing().when(service).removeByUsernameAndEntryname(any(String.class), any(String.class), eq("some-test-token"));
        mvc.perform(delete(diaryUserEntry)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON).header("ZKAuth-Token", "some-test-token"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(service, times(1)).removeByUsernameAndEntryname(any(String.class), any(String.class), any(String.class));
        verifyNoMoreInteractions(service);
    }
}
