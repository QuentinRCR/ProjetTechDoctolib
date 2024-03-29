package com.docto.protechdoctolib.security.config;


import com.docto.protechdoctolib.filter.CustomAuthenticationFilter;
import com.docto.protechdoctolib.filter.CustomAuthorizationFilter;
import com.docto.protechdoctolib.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.http.HttpMethod.*;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    private Environment environment;

    public WebSecurityConfig(UserService userService, BCryptPasswordEncoder bCryptPasswordEncoder, AuthenticationConfiguration authenticationConfiguration) {
        this.userService = userService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationConfiguration = authenticationConfiguration;
    }

    /**
     * Désactive la sécurité sur le chemin de API pour que le test d'intégration fonctionne (temporaire)
     * @param http
     * @return
     * @throws Exception
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(Customizer.withDefaults()); //use COR policy defined in corsConfigurationSource
        CustomAuthenticationFilter customAuthenticationFilter = new CustomAuthenticationFilter(authenticationManager(authenticationConfiguration));
        customAuthenticationFilter.setFilterProcessesUrl("/api/login");
        http.csrf().disable();

        http.authorizeRequests().antMatchers("/api/login/**", "/api/token/refresh/**","/api/registration/**","/api/forgotten_password/**").permitAll();
        http.authorizeRequests().antMatchers(GET,"/api/rendez_vous/user/**").hasAnyAuthority("USER","ADMIN");
        http.authorizeRequests().antMatchers(POST,"/api/rendez_vous/user/**").hasAnyAuthority("USER","ADMIN");
        http.authorizeRequests().antMatchers(DELETE,"/api/rendez_vous/user/**").hasAnyAuthority("USER","ADMIN");
        http.authorizeRequests().antMatchers(GET,"/api/rendez_vous/admin/**").hasAnyAuthority("ADMIN");


        http.authorizeRequests().antMatchers(GET,"/api/creneaux/user/**").hasAnyAuthority("USER","ADMIN");
        http.authorizeRequests().antMatchers(POST,"/api/creneaux/admin/**").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(DELETE,"/api/creneaux/admin/**").hasAnyAuthority("ADMIN");

        http.authorizeRequests().antMatchers(GET,"/api/users/user/**").hasAnyAuthority("ADMIN","USER");
        http.authorizeRequests().antMatchers(POST,"/api/users/user/**").hasAnyAuthority("ADMIN","USER");
        http.authorizeRequests().antMatchers(DELETE,"/api/users/user/**").hasAnyAuthority("ADMIN","USER");
        http.authorizeRequests().antMatchers(GET,"/api/users/admin/**").hasAnyAuthority("ADMIN");

        http.authorizeRequests().antMatchers(POST,"/api/communicationMean/admin/**").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(DELETE,"/api/communicationMean/admin/**").hasAnyAuthority("ADMIN");
        http.authorizeRequests().antMatchers(GET,"/api/communicationMean/user/**").hasAnyAuthority("USER","ADMIN");

        http.authorizeRequests().anyRequest().authenticated();
        http.formLogin();
        http.addFilter(customAuthenticationFilter);
        http.addFilterBefore(new CustomAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(environment.getProperty("frontend.url")));
        configuration.setAllowedMethods(Arrays.asList("*"));
        configuration.setAllowedHeaders(List.of("Content-Type","AUTHORIZATION")); //"Access-Control-Allow-Headers", "Authorization"
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider provider=
                new DaoAuthenticationProvider();
        provider.setPasswordEncoder(bCryptPasswordEncoder);
        provider.setUserDetailsService(userService);
        return provider;
    }
}
