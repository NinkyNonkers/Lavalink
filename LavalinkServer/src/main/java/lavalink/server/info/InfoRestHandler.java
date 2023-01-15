package lavalink.server.info;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by napster on 08.03.19.
 */
@RestController
public class InfoRestHandler {
    @GetMapping("/version")
    public String version() {
        return "NonkPlayer/Lavalink@1.7.0";
    }
}
