package com.welab.alertsystem.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.google.common.base.Strings;
import com.welab.alertsystem.auth.ApplicationUser;
import com.welab.alertsystem.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JwtTokenVerifier extends OncePerRequestFilter {

    private final SecretKey secretKey;
    private final JwtConfig jwtConfig;

    public JwtTokenVerifier(SecretKey secretKey, JwtConfig jwtConfig) {
        this.secretKey = secretKey;
        this.jwtConfig = jwtConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(jwtConfig.getAuthorizationHeader());

        if (Strings.isNullOrEmpty(authorizationHeader) || !authorizationHeader.startsWith(jwtConfig.getTokenPrefix())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorizationHeader.replace(jwtConfig.getTokenPrefix(), "");

        try {

            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);

            Claims body = claimsJws.getBody();

            String username = body.getSubject();

            String email = (String) body.get("email");

            Integer id = (Integer) body.get("id");

            List<Map<String, String>> authorities = (List<Map<String, String>>) body.get("authorities");

            if(id == null || username == null || email == null){
                throw new IllegalStateException(String.format("Token %s cannot be trusted", token));
            }

            Set<SimpleGrantedAuthority> simpleGrantedAuthorities = authorities.stream().map(m -> new SimpleGrantedAuthority(m.get("authority"))).collect(Collectors.toSet());

            ApplicationUser userDetails = new ApplicationUser(
                    id,
                    email,
                    username,
                    null,
                    simpleGrantedAuthorities,
                    true,
                    true,
                    true,
                    true
            );

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    simpleGrantedAuthorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);

        } catch (JwtException e) {
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, e, "Token can't be trust");
        }


    }

    public void setErrorResponse(HttpStatus status, HttpServletResponse response, Throwable ex, String message){
        response.setStatus(status.value());
        response.setContentType("application/json");
        // A class used for errors
        ApiException apiException = new ApiException(message,status, ZonedDateTime.now(ZoneId.of("Z")));
        try {
            ObjectMapper objectMapper = JsonMapper.builder().findAndAddModules().build();
            response.getWriter().write(objectMapper.writeValueAsString(apiException));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
