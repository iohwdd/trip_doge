package com.tripdog.ai.rewrite;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RewriteResult {
    private String original;
    private String rewritten;
    private boolean changed;
    private List<String> reasons;
}
