package com.tripdog.ai.rewrite;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 多轮对话用户输入改写：
 * 1. 处理"继续"、"再来一个"、"换一个"、"详细点"等指令补全
 * 2. 处理对上一个助手答案的引用（如："第2个再具体点"）
 * 3. 规则优先，后续可接入小模型做指代消解
 */
@Component
@Slf4j
public class MultiTurnRewriteService {

    private static final Pattern CONTINUE_PATTERN = Pattern.compile("^(继续|接着|再来一个|再来|换一个|详细点|展开说说)(.*)?$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern INDEX_REF_PATTERN = Pattern.compile("第(\\d+)[个条]", Pattern.CASE_INSENSITIVE);


    public RewriteResult rewrite(String userInput, List<ChatMessage> recentMessages) {
        String trimmed = userInput == null ? "" : userInput.trim();
        if (trimmed.isEmpty()) {
            return RewriteResult.builder()
                    .original(userInput)
                    .rewritten(userInput)
                    .changed(false)
                    .reasons(List.of("empty"))
                    .build();
        }

        List<String> reasons = new ArrayList<>();
        String rewritten = trimmed;
        boolean changed = false;

        ChatMessage lastAssistant = findLastAssistant(recentMessages);
        if (lastAssistant != null) {
            // 1. “继续”类补全
            if (CONTINUE_PATTERN.matcher(trimmed).find()) {
                String assistantContent = extractAssistantContent(lastAssistant);
                String focus = clipForContinuation(assistantContent);
                rewritten = "基于你刚才的回答(摘要片段): " + focus + " ，" + normalizeContinueCommand(trimmed);
                reasons.add("continue-expand");
                changed = true;
            }

            // 2. 索引引用（第2个/第3条）
            var m = INDEX_REF_PATTERN.matcher(trimmed);
            if (m.find()) {
                String indexRef = m.group(1);
                // 这里先直接附加说明，后续可解析出对应列表项内容
                rewritten = rewritten + " (指代上一个回答里的第" + indexRef + "项，请基于该项继续/修改)";
                reasons.add("index-ref");
                changed = true;
            }
        }

        return RewriteResult.builder()
                .original(userInput)
                .rewritten(rewritten)
                .changed(changed)
                .reasons(reasons)
                .build();
    }

    private ChatMessage findLastAssistant(List<ChatMessage> messages) {
        if (messages == null) return null;
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg instanceof AiMessage) {
                return msg;
            }
        }
        return null;
    }

    private String extractAssistantContent(ChatMessage msg) {
        if (msg instanceof AiMessage) {
            return ((AiMessage) msg).text();
        }
        return "";
    }

    private String clipForContinuation(String text) {
        if (text == null) return "";
        text = text.replaceAll("\\s+", " ");
        return text.length() > 120 ? text.substring(0, 120) + "..." : text;
    }

    private String normalizeContinueCommand(String original) {
        if (original == null) return "继续";
        if (original.length() <= 8) return original; // 原样保留短指令
        return original; // 暂不做复杂清洗
    }
}
