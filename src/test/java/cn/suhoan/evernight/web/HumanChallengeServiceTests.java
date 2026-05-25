package cn.suhoan.evernight.web;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class HumanChallengeServiceTests {

    private static final Pattern QUESTION_PATTERN = Pattern.compile("(\\d+) ([+\\-*]) (\\d+) = \\?");

    private final HumanChallengeService service = new HumanChallengeService();

    @Test
    void challengeAnswerCanOnlyBeUsedOnce() {
        HumanChallengeService.HumanChallenge challenge = service.createChallenge();
        String answer = solve(challenge.question());

        service.verify(challenge.token(), answer);

        assertThatThrownBy(() -> service.verify(challenge.token(), answer))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("人机校验失败");
    }

    @Test
    void rejectsWrongAnswer() {
        HumanChallengeService.HumanChallenge challenge = service.createChallenge();

        assertThatThrownBy(() -> service.verify(challenge.token(), "not-a-number"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("人机校验失败");
    }

    private static String solve(String question) {
        Matcher matcher = QUESTION_PATTERN.matcher(question);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("无法解析测试问题: " + question);
        }
        int left = Integer.parseInt(matcher.group(1));
        int right = Integer.parseInt(matcher.group(3));
        int answer = switch (matcher.group(2)) {
            case "+" -> left + right;
            case "-" -> left - right;
            case "*" -> left * right;
            default -> throw new IllegalArgumentException("未知运算符");
        };
        return Integer.toString(answer);
    }

}
