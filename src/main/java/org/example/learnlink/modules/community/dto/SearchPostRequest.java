package org.example.learnlink.modules.community.dto;

import org.example.learnlink.modules.community.entity.PostCategory;
import org.example.learnlink.modules.community.entity.PostType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for post search/filter request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchPostRequest {

    private String keyword;
    private PostCategory category;
    private PostType type;
    private String sortBy; // RECENT, POPULAR, TRENDING
}

