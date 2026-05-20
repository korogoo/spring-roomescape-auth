package roomescape.config;

import java.util.List;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import roomescape.filter.AuthFilter;
import roomescape.interceptor.AuthInterceptor;
import roomescape.interceptor.LoginMemberArgumentResolver;
import roomescape.repository.token.TokenRepository;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TokenRepository tokenRepository;

    public WebConfig(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(tokenRepository))
            .addPathPatterns("/**")
            .excludePathPatterns(List.of(
                "/members/login",
                "/members/normal/join",
                "/members/admin/join"
            ));
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
    }

    // @Bean
    public FilterRegistrationBean<AuthFilter> authFilter() {
        FilterRegistrationBean<AuthFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new AuthFilter());
        registration.addUrlPatterns("/**");
        return registration;
    }
}
