package org.example.learnlink.modules.community.service;

import org.example.learnlink.modules.community.dto.AddCommentRequest;
import org.example.learnlink.modules.community.dto.CommentResponse;
import org.example.learnlink.modules.community.dto.CreatePostRequest;
import org.example.learnlink.modules.community.dto.PostResponse;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;
import org.example.learnlink.modules.community.repository.CommentRepository;
import org.example.learnlink.modules.community.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for CommentService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CommentServiceIntegrationTest {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IPostService postService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostRepository postRepository;

    private PostResponse testPost;
    private AddCommentRequest addCommentRequest;

    @BeforeEach
    public void setUp() {
        // Clear databases
        commentRepository.deleteAll();
        postRepository.deleteAll();

        // Create a test post
        CreatePostRequest postRequest = CreatePostRequest.builder()
            .title("Guide to Calculus Derivatives")
            .content("This is a comprehensive guide explaining calculus derivatives with examples")
            .type(PostType.SUMMARY)
            .category(PostCategory.MATHEMATICS)
            .build();
        testPost = postService.createPost(1L, postRequest);

        addCommentRequest = AddCommentRequest.builder()
            .content("This is a great explanation!")
            .build();
    }

    @Test
    public void testAddCommentToPost_Success() {
        CommentResponse response = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        assertNotNull(response.getId());
        assertEquals(testPost.getId(), response.getPostId());
        assertNull(response.getAnswerId());
        assertEquals(2L, response.getUserId());
        assertEquals("This is a great explanation!", response.getContent());
    }

    @Test
    public void testGetCommentById_Success() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        CommentResponse retrieved = commentService.getCommentById(created.getId());

        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(testPost.getId(), retrieved.getPostId());
    }

    @Test
    public void testGetCommentsForPost() {
        // Add multiple comments
        commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        AddCommentRequest comment2 = AddCommentRequest.builder()
            .content("I found this very helpful!")
            .build();
        commentService.addCommentToPost(testPost.getId(), 3L, comment2);

        List<CommentResponse> comments = commentService.getCommentsForPost(testPost.getId());

        assertEquals(2, comments.size());
        assertTrue(comments.stream().allMatch(c -> c.getPostId().equals(testPost.getId())));
    }

    @Test
    public void testUpdateComment_Success() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        AddCommentRequest updateRequest = AddCommentRequest.builder()
            .content("Updated comment with better content")
            .build();

        CommentResponse updated = commentService.updateComment(created.getId(), 2L, updateRequest);

        assertEquals("Updated comment with better content", updated.getContent());
    }

    @Test
    public void testUpdateComment_Unauthorized() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        AddCommentRequest updateRequest = AddCommentRequest.builder()
            .content("Unauthorized update")
            .build();

        // User 3 tries to update User 2's comment
        assertThrows(RuntimeException.class, () ->
            commentService.updateComment(created.getId(), 3L, updateRequest)
        );
    }

    @Test
    public void testDeleteComment_Success() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        commentService.deleteComment(created.getId(), 2L);

        // Verify deletion
        List<CommentResponse> comments = commentService.getCommentsForPost(testPost.getId());
        assertTrue(comments.isEmpty());
    }

    @Test
    public void testDeleteComment_DecreasesPostCommentCount() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        // Check post has comment count increased
        PostResponse postWithComment = postService.getPostById(testPost.getId());
        assertEquals(1L, postWithComment.getCommentsCount());

        // Delete comment
        commentService.deleteComment(created.getId(), 2L);

        // Check post comment count decreased
        PostResponse postAfterDelete = postService.getPostById(testPost.getId());
        assertEquals(0L, postAfterDelete.getCommentsCount());
    }

    @Test
    public void testLikeComment_Success() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        commentService.likeComment(created.getId(), 3L);

        CommentResponse likedComment = commentService.getCommentById(created.getId());
        assertEquals(1L, likedComment.getLikesCount());
    }

    @Test
    public void testLikeComment_MultipleLikes() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        // Like from multiple users
        commentService.likeComment(created.getId(), 3L);
        commentService.likeComment(created.getId(), 4L);
        commentService.likeComment(created.getId(), 5L);

        CommentResponse likedComment = commentService.getCommentById(created.getId());
        assertEquals(3L, likedComment.getLikesCount());
    }

    @Test
    public void testUnlikeComment_Success() {
        CommentResponse created = commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        // Like first
        commentService.likeComment(created.getId(), 3L);
        CommentResponse liked = commentService.getCommentById(created.getId());
        assertEquals(1L, liked.getLikesCount());

        // Unlike
        commentService.unlikeComment(created.getId(), 3L);
        CommentResponse unliked = commentService.getCommentById(created.getId());
        assertEquals(0L, unliked.getLikesCount());
    }

    @Test
    public void testGetUserComments() {
        // User 2 posts 2 comments
        commentService.addCommentToPost(testPost.getId(), 2L, addCommentRequest);

        AddCommentRequest comment2 = AddCommentRequest.builder()
            .content("Another comment from user 2")
            .build();
        commentService.addCommentToPost(testPost.getId(), 2L, comment2);

        // User 3 posts 1 comment
        commentService.addCommentToPost(testPost.getId(), 3L, addCommentRequest);

        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponse> user2Comments = commentService.getUserComments(2L, pageable);

        assertEquals(2, user2Comments.getContent().size());
        assertTrue(user2Comments.getContent().stream()
            .allMatch(c -> c.getUserId().equals(2L)));
    }

    @Test
    public void testAddCommentToAnswer() {
        CommentResponse response = commentService.addCommentToAnswer(1L, 2L, addCommentRequest);

        assertNotNull(response.getId());
        assertEquals(1L, response.getAnswerId());
        assertNull(response.getPostId());
        assertEquals(2L, response.getUserId());
    }

    @Test
    public void testGetCommentsForAnswer() {
        // Add multiple comments to answer
        commentService.addCommentToAnswer(1L, 2L, addCommentRequest);

        AddCommentRequest comment2 = AddCommentRequest.builder()
            .content("Another comment on answer")
            .build();
        commentService.addCommentToAnswer(1L, 3L, comment2);

        List<CommentResponse> comments = commentService.getCommentsForAnswer(1L);

        assertEquals(2, comments.size());
        assertTrue(comments.stream().allMatch(c -> c.getAnswerId().equals(1L)));
    }

    @Test
    public void testAddCommentInvalidContent() {
        AddCommentRequest invalidRequest = AddCommentRequest.builder()
            .content("")  // Empty content
            .build();

        assertThrows(Exception.class, () ->
            commentService.addCommentToPost(testPost.getId(), 2L, invalidRequest)
        );
    }
}


