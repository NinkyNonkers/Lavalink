package lavalink.server.info;

import org.springframework.stereotype.Component;

/**
 * Created by napster on 25.06.18.
 * <p>
 * Provides access to the values of the property file generated by whatever git info plugin we're using
 * <p>
 * Requires a generated git.properties, which can be achieved with the gradle git plugin
 */
@Component
public class GitRepoState {
}

