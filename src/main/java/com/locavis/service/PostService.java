package com.locavis.service;

import com.locavis.exception.PostNotFoundException;
import com.locavis.locavis.dto.PostDto;
import com.locavis.model.Post;
import com.locavis.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class PostService{

    @Autowired
    AuthService authService;

    @Autowired
    private PostRepository postRepository;

    public void createPost(PostDto postDto){
        Post post = new Post();
            post.setTitle(postDto.getTitle());
            post.setContent(postDto.getContent());
            User username = authService.getCurrentUser().orElseThrow(() ->
                new IllegalArgumentException("No User Lounged In"));
            post.setUsername(username.getUsername());
            post.setCreatedOn(Instant.now());
            postRepository.save(post);
    }

    public Object showAllPosts() {
        List<Post> posts = postRepository.findAll();
        return posts.stream().map(this::mapFromPostToDto).collect(toList());
    }

    private PostDto mapFromPostToDto(Post post) {
        PostDto postDto = new PostDto();
        postDto.setId(post.getId());
        postDto.setTitle(post.getTitle());
        postDto.setContent(post.getContent());
        postDto.setUsername(post.getUsername());
        return postDto;
    }

    private Post mapFromDtoToPost(PostDto postDto) {
        Post post = new Post();
        post.setTitle(post.getTitle());
        post.setContent(post.getContent());
        User loggedInUser = authService.getCurrentUser().orElseThrow(() -> new IllegalArgumentException("User Not Found"));
        post.setCreatedOn(Instant.now());
        post.setUsername(loggedInUser.getUsername());
        post.setUpdatedOn(Instant.now());
        return post;
    }


    public Object readSinglePost(Long id) {
        Post post = postRepository.findById(id).orElseThrow(()-> new PostNotFoundException("For id "+id));
        return mapFromPostToDto(post);
    }
}
