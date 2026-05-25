package cn.suhoan.evernight.tool;

import java.util.Map;

public record ToolInvocationRequest(
        String tool,
        Map<String, Object> arguments,
        String challengeToken,
        String challengeAnswer) {
}
