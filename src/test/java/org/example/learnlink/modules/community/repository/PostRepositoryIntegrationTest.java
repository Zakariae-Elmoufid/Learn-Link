package org.example.learnlink.modules.community.repository;

import org.example.learnlink.modules.community.entity.Post;
import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for PostRepository
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PostRepositoryIntegrationTest {

    @Autowired
    private PostRepository postRepository;

    private Post testPost;

    @BeforeEach
    public void setUp() {
        testPost = Post.builder()
            .userId(1L)
            .title("Test Post Title")
            .content("This is test post content with sufficient length for validation")
            .type(PostType.SUMMARY)
            .category(PostCategory.MATHEMATICS)
            .build();
    }

    @Test
    public void testSavePost() {
        Post savedPost = postRepository.save(testPost);

        assertNotNull(savedPost.getId());
        assertEquals("Test Post Title", savedPost.getTitle());
        assertEquals(1L, savedPost.getUserId());
    }

    @Test
    public void testFindByUserId() {
        postRepository.save(testPost);

        Page<Post> posts = postRepository.findByUserId(1L, PageRequest.of(0, 10));

        assertEquals(1, posts.getContent().size());
        assertEquals("Test Post Title", posts.getContent().get(0).getTitle());
    }

    @Test
    public void testFindByCategory() {
        postRepository.save(testPost);

        Page<Post> posts = postRepository.findByCategory(PostCategory.MATHEMATICS, PageRequest.of(0, 10));

        assertEquals(1, posts.getContent().size());
        assertEquals(PostCategory.MATHEMATICS, posts.getContent().get(0).getCategory());
    }

    @Test
    public void testFindByType() {
        postRepository.save(testPost);

        Page<Post> posts = postRepository.findByType(PostType.SUMMARY, PageRequest.of(0, 10));

        assertEquals(1, posts.getContent().size());
        assertEquals(PostType.SUMMARY, posts.getContent().get(0).getType());
    }

    @Test
    public void testFindPopularPosts() {
        Post post1 = testPost;
        post1.setViewCount(100L);
        postRepository.save(post1);

        Post post2 = Post.builder()
            .userId(2L)
            .title("Another Post")
            .content("Another post content with sufficient length for validation")
            .type(PostType.DISCUSSION)
            .category(PostCategory.SCIENCE)
            .viewCount(50L)
            .build();
        postRepository.save(post2);

        Page<Post> posts = postRepository.findPopularPosts(PageRequest.of(0, 10));

        assertTrue(posts.getContent().size() >= 1);
        assertEquals(100L, posts.getContent().get(0).getViewCount());
    }

    @Test
    public void testSearchByKeyword() {
        postRepository.save(testPost);

        Page<Post> posts = postRepository.searchByKeyword("Title", PageRequest.of(0, 10));

        assertEquals(1, posts.getContent().size());
        assertEquals("Test Post Title", posts.getContent().get(0).getTitle());
    }

    @Test
    public void testIncrementViewCount() {
        Post savedPost = postRepository.save(testPost);
        assertEquals(0L, savedPost.getViewCount());

        savedPost.incrementViewCount();
        postRepository.save(savedPost);

        Post retrievedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertNotNull(retrievedPost);
        assertEquals(1L, retrievedPost.getViewCount());
    }

    @Test
    public void testIncrementLikesCount() {
        Post savedPost = postRepository.save(testPost);
        assertEquals(0L, savedPost.getLikesCount());

        savedPost.incrementLikesCount();
        postRepository.save(savedPost);

        Post retrievedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertNotNull(retrievedPost);
        assertEquals(1L, retrievedPost.getLikesCount());
    }

    @Test
    public void testFindByCategoryAndType() {
        postRepository.save(testPost);

        Page<Post> posts = postRepository.findByCategoryAndType(
            PostCategory.MATHEMATICS,
            PostType.SUMMARY,
            PageRequest.of(0, 10)
        );

        assertEquals(1, posts.getContent().size());
    }

    @Test
    public void testUpdatePost() {
        Post savedPost = postRepository.save(testPost);

        savedPost.setTitle("Updated Title");
        savedPost.setContent("Updated content with sufficient length for validation");
        postRepository.save(savedPost);

        Post updatedPost = postRepository.findById(savedPost.getId()).orElse(null);
        assertNotNull(updatedPost);
        assertEquals("Updated Title", updatedPost.getTitle());
    }

    @Test
    public void testDeletePost() {
        Post savedPost = postRepository.save(testPost);
        Long postId = savedPost.getId();

        postRepository.delete(savedPost);

        assertTrue(postRepository.findById(postId).isEmpty());
    }
}


