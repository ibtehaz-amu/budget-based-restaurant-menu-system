package com.restaurant.backend.config;

import com.restaurant.backend.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // ✅ YEH IMPORTANT METHOD ADD KARO - PUBLIC ENDPOINTS KO SKIP KARNE KE LIYE
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        System.out.println("🔍 Checking path: " + path);

        // ✅ PUBLIC ENDPOINTS - INPAR JWT FILTER NAHI CHALEGA
        boolean shouldSkip = path.startsWith("/api/auth/") ||
                path.startsWith("/api/dishes/") ||
                path.startsWith("/api/restaurants/") ||
                path.equals("/") ||
                path.contains("/test");

        System.out.println("🔍 Skip JWT filter for this path: " + shouldSkip);
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // ✅ YEH LINE ADD KARO - Debug ke liye
        System.out.println("🎯 JWT Filter processing: " + request.getServletPath());

        try {
            String token = getTokenFromRequest(request);
            System.out.println("🔐 JWT Filter - Token received: " + (token != null ? "YES" : "NO"));

            if (token != null) {
                System.out.println("🔐 Token: " + token.substring(0, Math.min(20, token.length())) + "...");
                System.out.println("🔐 Validating token...");

                boolean isValid = jwtUtil.validateToken(token);
                System.out.println("🔐 Token valid: " + isValid);

                if (isValid) {
                    String email = jwtUtil.extractUsername(token);
                    String role = jwtUtil.extractRole(token);
                    System.out.println("🔐 User: " + email + ", Role: " + role);

                    // Create authentication object
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(email, null,
                                    Collections.singletonList(new SimpleGrantedAuthority(role)));

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    System.out.println("🔐 Authentication set successfully for: " + email);
                } else {
                    System.out.println("❌ Token validation failed!");
                }
            } else {
                System.out.println("❌ No token found in request!");
            }
        } catch (Exception e) {
            System.out.println("❌ JWT Filter error: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}