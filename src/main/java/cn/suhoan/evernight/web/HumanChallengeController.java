package cn.suhoan.evernight.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HumanChallengeController {

    private final HumanChallengeService humanChallengeService;

    public HumanChallengeController(HumanChallengeService humanChallengeService) {
        this.humanChallengeService = humanChallengeService;
    }

    @GetMapping("/api/human-challenge")
    public HumanChallengeService.HumanChallenge humanChallenge() {
        return humanChallengeService.createChallenge();
    }

}
