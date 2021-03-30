package com.locavis.service;

import com.locavis.dto.LoginRequest;
import com.locavis.locavis.dto.PostDto;
import com.locavis.model.Post;
import com.locavis.model.User;
import com.locavis.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;
    @Mock
    private AuthService authService;
    private PostService underTest;

    @BeforeEach
    void setUp() {
        underTest = new PostService(postRepository,authService);
    }

    @Test
    @WithMockUser(username = "El Mehdi", authorities = { "ADMIN", "USER" })
    void createPost() {
        //TODO:vérify how to pass this test even with the login user.
        //given
        PostDto postDto = new PostDto();
        postDto.setUsername("El Mehdi");
        postDto.setContent("content post");
        postDto.setTitle("title post");

        User user = new User();
        user.setUserName("El Mehdi");
        user.setPassword("password");

        //authService.login(loginRequest);
        //given(authService.getCurrentUser()).willReturn();

        //when
        underTest.createPost(postDto);
        //then
        ArgumentCaptor<Post> postArgumentCaptor = ArgumentCaptor.forClass(Post.class);

        verify(postRepository).save(postArgumentCaptor.capture());

        Post capturedPost = postArgumentCaptor.getValue();

        assertThat(capturedPost).isEqualTo(postDto);
    }

    @Test
    void willThrowWhencreatePost() {
        //TODO:vérify how to pass this test even with the login user.
        //given
        PostDto postDto = new PostDto();
        postDto.setUsername("El Mehdi");
        postDto.setContent("content post");
        postDto.setTitle("title post");

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("El Mehdi");
        loginRequest.setPassword("password");

        //when
        //then
        assertThatThrownBy(() -> underTest.createPost(postDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No User Lounged In");

        verify(postRepository, never()).save(any());
    }

    @Test
    void canShowAllPosts() {
        //when
        underTest.showAllPosts();
        //then
        verify(postRepository).findAll();
    }

    @Test
    @Disabled
    void readSinglePost() {
    }
}