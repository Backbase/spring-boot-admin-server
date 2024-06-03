package com.backbase.oss.admin.server.config;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.POST;

import de.codecentric.boot.admin.server.config.AdminServerProperties;
import jakarta.servlet.DispatcherType;
import java.util.UUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration(proxyBeanMethods = false)
public class SecuritySecureConfig {

    private final AdminServerProperties adminServer;

    public SecuritySecureConfig(AdminServerProperties adminServer) {
        this.adminServer = adminServer;
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        SavedRequestAwareAuthenticationSuccessHandler successHandler = new SavedRequestAwareAuthenticationSuccessHandler();
        successHandler.setTargetUrlParameter("redirectTo");
        successHandler.setDefaultTargetUrl(this.adminServer.path("/"));

        http.authorizeHttpRequests((authorizeRequests) -> authorizeRequests
                .requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/assets/**")))
                .permitAll()
                .requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/actuator/**")))
                .permitAll()
                .requestMatchers(new AntPathRequestMatcher(this.adminServer.path("/login")))
                .permitAll()
                .dispatcherTypeMatchers(DispatcherType.ASYNC)
                .permitAll() // https://github.com/spring-projects/spring-security/issues/11027
                .anyRequest()
                .authenticated())
            .formLogin(
                (formLogin) -> formLogin.loginPage(this.adminServer.path("/login")).successHandler(successHandler))
            .logout((logout) -> logout.logoutUrl(this.adminServer.path("/logout")))
            .httpBasic(Customizer.withDefaults());

        http.csrf((csrf) -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
            .ignoringRequestMatchers(
                new AntPathRequestMatcher(this.adminServer.path("/instances"), POST.toString()),
                new AntPathRequestMatcher(this.adminServer.path("/instances/*"), DELETE.toString()),
                new AntPathRequestMatcher(this.adminServer.path("/actuator/**"))
            ));

        http.rememberMe((rememberMe) -> rememberMe.key(UUID.randomUUID().toString()).tokenValiditySeconds(1209600));

        return http.build();

    }

}
