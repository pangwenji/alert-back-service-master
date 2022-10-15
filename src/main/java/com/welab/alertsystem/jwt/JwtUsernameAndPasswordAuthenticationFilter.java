package com.welab.alertsystem.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.welab.alertsystem.auth.ApplicationUser;
import com.welab.alertsystem.exception.ApiException;
import io.jsonwebtoken.Jwts;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

public class JwtUsernameAndPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtConfig jwtConfig;
    private final SecretKey secretKey;

    public JwtUsernameAndPasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtConfig jwtConfig, SecretKey secretKey) {
        this.authenticationManager = authenticationManager;
        this.jwtConfig = jwtConfig;
        this.secretKey = secretKey;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            UsernameAndPasswordAuthenticationRequest authenticationRequest = new ObjectMapper().readValue(request.getInputStream(), UsernameAndPasswordAuthenticationRequest.class);

            Authentication authentication = new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword());

            Authentication authenticate = authenticationManager.authenticate(authentication);
            return authenticate;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        setErrorResponse(HttpStatus.UNAUTHORIZED,response, null, "NOT AUTHORIZED");

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        ApplicationUser user = (ApplicationUser) authResult.getPrincipal();
        String token = Jwts.builder()
                .setSubject(authResult.getName())
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("authorities", authResult.getAuthorities()
                ).setIssuedAt(new Date())
                .setExpiration(Date.from(Instant.now().plus(jwtConfig.getTokenExpirationAfterMinutes(), ChronoUnit.MINUTES)))
                .signWith(secretKey)
                .compact();
        response.addHeader(jwtConfig.getAuthorizationHeader(), jwtConfig.getTokenPrefix() + " " + token);
        response.setStatus(200);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("email", user.getEmail());
        userMap.put("username", user.getUsername());
        userMap.put("authorities", authResult.getAuthorities().stream().map(e -> e.getAuthority()).collect(Collectors.toList()));
        userMap.put("accessToken", token);
        PrintWriter out = response.getWriter();
        out.write(String.valueOf(new JSONObject(userMap)));
    }


    public void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex, String message) {
        response.setStatus(status.value());
        response.setContentType("application/json");
        // A class used for errors
        ApiException apiException = new ApiException(message, status, ZonedDateTime.now(ZoneId.of("Z")));
        try {
            ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
            response.getWriter().write(objectMapper.writeValueAsString(apiException));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
