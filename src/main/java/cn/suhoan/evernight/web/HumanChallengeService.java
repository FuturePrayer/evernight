package cn.suhoan.evernight.web;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class HumanChallengeService {

    private static final long EXPIRES_IN_SECONDS = 300;

    private final SecureRandom secureRandom = new SecureRandom();

    private final Cache<String, String> answers = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(EXPIRES_IN_SECONDS))
            .maximumSize(10000)
            .build();

    public HumanChallenge createChallenge() {
        int left = secureRandom.nextInt(31) + 10;
        int right = secureRandom.nextInt(21) + 3;
        int operatorIndex = secureRandom.nextInt(3);
        String operator;
        int answer;
        if (operatorIndex == 0) {
            operator = "+";
            answer = left + right;
        }
        else if (operatorIndex == 1) {
            operator = "-";
            if (right > left) {
                int currentLeft = left;
                left = right;
                right = currentLeft;
            }
            answer = left - right;
        }
        else {
            operator = "*";
            answer = left * right;
        }
        String token = randomToken();
        answers.put(token, Integer.toString(answer));
        return new HumanChallenge(token, "%d %s %d = ?".formatted(left, operator, right), EXPIRES_IN_SECONDS);
    }

    public void verify(String token, String answer) {
        if (!StringUtils.hasText(token) || !StringUtils.hasText(answer)) {
            throw new IllegalArgumentException("人机校验不能为空");
        }
        String expectedAnswer = answers.asMap().remove(token.trim());
        if (expectedAnswer == null || !expectedAnswer.equals(answer.trim())) {
            throw new IllegalArgumentException("人机校验失败，请刷新后重试");
        }
    }

    private String randomToken() {
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record HumanChallenge(
            String token,
            String question,
            long expiresInSeconds) {
    }

}
