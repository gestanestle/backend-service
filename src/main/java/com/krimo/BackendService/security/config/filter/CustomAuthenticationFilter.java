package com.krimo.BackendService.security.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.krimo.BackendService.security.UsernamePasswordAuthToken;
import com.krimo.BackendService.security.utils.JWTUtility;
import com.krimo.BackendService.user.entity.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RequiredArgsConstructor
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

    private final JWTUtility jwtUtility;
    private final UsernamePasswordAuthToken usernamePasswordAuthToken;

    /**
     * This method is a custom implementation of a attemptAuthentication
     * method provided by Spring Security.
     *
     * @param request       from which to extract parameters and perform the authentication
     * @param response      the response, which may be needed if the implementation has to do a
     *                      redirect as part of a multi-stage authentication process (such as OpenID).
     * @return authentication      the authentication of credentials
     * @throws AuthenticationException  authentication exception
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        return authenticationManager.authenticate(usernamePasswordAuthToken.authenticationToken(username, password));
    }

    /**
     * This method sets access and refresh tokens upon successful authentication.
     *
     * @param request
     * @param response
     * @param chain
     * @param authResult the object returned from the <tt>attemptAuthentication</tt>
     * method.
     * @throws IOException
     * @throws ServletException
     */

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {

        User user = (User) authResult.getPrincipal();

        // Access tokens last for 15 minutes
        String access_token = jwtUtility.createJwt(
                user.getUsername(),
                request.getRequestURL().toString(),
                user.getAuthorities(),
                900000);

        // Refresh tokens last for 14 days
        String refresh_token = jwtUtility.createJwt(
                user.getUsername(),
                request.getRequestURL().toString(),
                user.getAuthorities(),
                1209600000);

        Map <String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);

        OutputStream out = response.getOutputStream();
        response.setContentType(APPLICATION_JSON_VALUE);

        new ObjectMapper().writeValue(out, tokens);
    }

}

