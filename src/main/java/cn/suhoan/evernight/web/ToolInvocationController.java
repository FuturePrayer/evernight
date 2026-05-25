package cn.suhoan.evernight.web;

import cn.suhoan.evernight.tool.ToolInvocationRequest;
import cn.suhoan.evernight.tool.ToolInvocationResponse;
import cn.suhoan.evernight.tool.ToolInvocationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToolInvocationController {

    private final HumanChallengeService humanChallengeService;

    private final ToolInvocationService toolInvocationService;

    public ToolInvocationController(HumanChallengeService humanChallengeService,
            ToolInvocationService toolInvocationService) {
        this.humanChallengeService = humanChallengeService;
        this.toolInvocationService = toolInvocationService;
    }

    @PostMapping("/api/tool-invocations")
    public ToolInvocationResponse invoke(@RequestBody ToolInvocationRequest request) {
        humanChallengeService.verify(request.challengeToken(), request.challengeAnswer());
        return new ToolInvocationResponse(request.tool(), toolInvocationService.invoke(request.tool(), request.arguments()));
    }

}
