package cn.suhoan.evernight.online;


import cn.suhoan.evernight.config.ActiveUserProperties;
import cn.suhoan.evernight.online.ActiveUserService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableConfigurationProperties(ActiveUserProperties.class)
public class ActiveUserController {

    private final ActiveUserService activeUserService;

    public ActiveUserController(ActiveUserService activeUserService) {
        this.activeUserService = activeUserService;
    }

    @GetMapping("/api/online-users")
    public OnlineUsersResponse onlineUsers() {
        return new OnlineUsersResponse(activeUserService.count(), activeUserService.windowSeconds());
    }

    public record OnlineUsersResponse(
            long count,
            long windowSeconds) {
    }

}
