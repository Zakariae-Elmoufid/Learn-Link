package org.example.learnlink.modules.community.controller;

import org.example.learnlink.modules.community.dto.CreatePostRequest;
import org.example.learnlink.modules.community.dto.PostResponse;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;
import org.example.learnlink.modules.community.service.IPostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for PostController
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PostControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;


    @Autowired
    private IPostService postService;

    private CreatePostRequest createPostRequest;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        createPostRequest = CreatePostRequest.builder()
            .title("Test Post Title")
            .content("This is a detailed test post content with sufficient length")
            .type(PostType.SUMMARY)
            .category(PostCategory.MATHEMATICS)
            .build();
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        mockMvc.perform(post("/api/community/posts")
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("Test Post Title"))
            .andExpect(jsonPath("$.userId").value(1))
            .andExpect(jsonPath("$.viewCount").value(0));
    }

    @Test
    public void testCreatePost_InvalidTitle() throws Exception {
        createPostRequest.setTitle("Bad"); // Too short

        mockMvc.perform(post("/api/community/posts")
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreatePost_MissingContent() throws Exception {
        createPostRequest.setContent(null);

        mockMvc.perform(post("/api/community/posts")
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createPostRequest)))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllPosts_Success() throws Exception {
        // Create a post first
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        mockMvc.perform(get("/api/community/posts")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content[0].id").exists());
    }

    @Test
    public void testGetPostById_Success() throws Exception {
        // Create a post first
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        mockMvc.perform(get("/api/community/posts/" + createdPost.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(createdPost.getId()))
            .andExpect(jsonPath("$.title").value("Test Post Title"));
    }

    @Test
    public void testGetPostById_NotFound() throws Exception {
        mockMvc.perform(get("/api/community/posts/999999"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void testGetPostsByCategory_Success() throws Exception {
        // Create a post
        postService.createPost(1L, createPostRequest);

        mockMvc.perform(get("/api/community/posts/category/MATHEMATICS")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    public void testGetPopularPosts_Success() throws Exception {
        // Create a post
        postService.createPost(1L, createPostRequest);

        mockMvc.perform(get("/api/community/posts/popular")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk());
    }

    @Test
    public void testLikePost_Success() throws Exception {
        // Create a post
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        mockMvc.perform(post("/api/community/posts/" + createdPost.getId() + "/like")
                .header("X-User-Id", 2L))
            .andExpect(status().isOk());
    }

    @Test
    public void testUnlikePost_Success() throws Exception {
        // Create a post
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        // Like it first
        postService.likePost(createdPost.getId(), 2L);

        // Now unlike
        mockMvc.perform(delete("/api/community/posts/" + createdPost.getId() + "/like")
                .header("X-User-Id", 2L))
            .andExpect(status().isOk());
    }

    @Test
    public void testUpdatePost_Success() throws Exception {
        // Create a post
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        CreatePostRequest updateRequest = CreatePostRequest.builder()
            .title("Updated Title")
            .content("Updated content with sufficient length to pass validation")
            .type(PostType.TUTORIAL)
            .category(PostCategory.PROGRAMMING)
            .build();

        mockMvc.perform(put("/api/community/posts/" + createdPost.getId())
                .header("X-User-Id", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    public void testUpdatePost_Unauthorized() throws Exception {
        // Create a post by user 1
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        CreatePostRequest updateRequest = CreatePostRequest.builder()
            .title("Updated Title")
            .content("Updated content with sufficient length")
            .type(PostType.TUTORIAL)
            .category(PostCategory.PROGRAMMING)
            .build();

        // Try to update with different user
        mockMvc.perform(put("/api/community/posts/" + createdPost.getId())
                .header("X-User-Id", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void testDeletePost_Success() throws Exception {
        // Create a post
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        mockMvc.perform(delete("/api/community/posts/" + createdPost.getId())
                .header("X-User-Id", 1L))
            .andExpect(status().isNoContent());
    }

    @Test
    public void testDeletePost_Unauthorized() throws Exception {
        // Create a post by user 1
        PostResponse createdPost = postService.createPost(1L, createPostRequest);

        // Try to delete with different user
        mockMvc.perform(delete("/api/community/posts/" + createdPost.getId())
                .header("X-User-Id", 2L))
            .andExpect(status().isInternalServerError());
    }

    @Test
    public void testSearchPosts_Success() throws Exception {
        // Create posts
        postService.createPost(1L, createPostRequest);

        mockMvc.perform(get("/api/community/posts/search")
                .param("keyword", "Post")
                .param("page", "0")
                .param("size", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray());
    }
}

