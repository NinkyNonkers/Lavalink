package lavalink.server.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lavalink.server.logging.ConsoleLogging;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class RequestAuthorizationFilter implements HandlerInterceptor, WebMvcConfigurer {

    private ServerConfig serverConfig;
    private MetricsPrometheusConfigProperties metricsConfig;

    public RequestAuthorizationFilter(ServerConfig serverConfig, MetricsPrometheusConfigProperties metricsConfig) {
        this.serverConfig = serverConfig;
        this.metricsConfig = metricsConfig;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Collecting metrics is anonymous
        if (!metricsConfig.getEndpoint().isEmpty()
                && request.getServletPath().equals(metricsConfig.getEndpoint())) return true;

        if (request.getServletPath().equals("/error")) return true;

        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.equals(serverConfig.getPassword())) {
            if (authorization == null) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                return false;
            }
            ConsoleLogging.LogError("Authorization failed");
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this);
    }
}
