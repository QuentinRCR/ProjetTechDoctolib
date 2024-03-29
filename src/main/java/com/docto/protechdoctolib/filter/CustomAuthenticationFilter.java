package com.docto.protechdoctolib.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.docto.protechdoctolib.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        String email =request.getParameter("email");
        String password = request.getParameter("password");
        //log.info("Email is: {}", email); log.info("Password is : {}", password);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        User user = (User)authentication.getPrincipal();
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes()); //TODO check video around 17 min should crypt the token
        String access_token = JWT.create() //pour recréer un access token valable plus longtemps
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() +2 * 60 *1000)) //token available 2 minutes
                .withIssuer(request.getRequestURI().toString())
                .withClaim("roles", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))      //prend le role du user
                .withKeyId(user.getId().toString())
                .sign(algorithm);

        String refresh_token = JWT.create() //pour recréer un refresh token valable plus longtemps
                .withSubject(user.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() +140 *1000))
                .withIssuer(request.getRequestURI().toString())
                .sign(algorithm);
        /*response.setHeader("access_token", access_token);         // envoie au frontend
        response.setHeader("refresh_token", refresh_token); */
        Map<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        response.setContentType(APPLICATION_JSON_VALUE);
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

    }
}
