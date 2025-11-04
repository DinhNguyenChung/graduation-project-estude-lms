package org.example.estudebackendspring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/ai/**").permitAll()
                        .requestMatchers("/api/admin/**").permitAll()
                        .requestMatchers("/api/admin/create-student").permitAll()
                        .requestMatchers("/api/admin/create-teacher").permitAll()
                        .requestMatchers("/api/schools/**").permitAll()
                        .requestMatchers("/api/auth/reset-password").permitAll()
                        .requestMatchers("/api/auth/forgot-password").permitAll()
                        .requestMatchers("/api/auth/verify-otp").permitAll()
                        .requestMatchers("/api/auth/update-password").permitAll()
                        .requestMatchers("/api/auth/login-student").permitAll()
                        .requestMatchers("/api/auth/login-admin").permitAll()
                        .requestMatchers("/api/auth/login-teacher").permitAll()
                        .requestMatchers("/api/auth/logout").permitAll()
                        .requestMatchers("/api/ai/predict-semeter").permitAll()
                        .requestMatchers("/api/ai/analyze/").permitAll()
                        .requestMatchers("api/teachers/**").permitAll()
                        .requestMatchers("/api/students/**").permitAll()
                        .requestMatchers("/api/assignments/**").permitAll()
                        .requestMatchers("/api/questions/**").permitAll()
                        .requestMatchers("/api/class-subjects/**").permitAll()
                        .requestMatchers("/api/submissions/**").permitAll()
                        .requestMatchers("/api/grades/**").permitAll()
                        .requestMatchers("/api/subject-grades/**").permitAll()
                        .requestMatchers("/api/subjects/**").permitAll()
                        .requestMatchers("/api/classes/**").permitAll()
                        .requestMatchers("/api/enrollments/**").permitAll()
                        .requestMatchers("/api/users/**").permitAll()
                        .requestMatchers("/api/attendance/**").permitAll()
                        .requestMatchers("/api/statistics/**").permitAll()
                        .requestMatchers("/ws-attendance/**").permitAll()
                        .requestMatchers("/api/schedules/**").permitAll()
                        .requestMatchers("/api/logentries/**").permitAll()
                        .requestMatchers("/api/notifications/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/practice-tests/**").permitAll()
                        .requestMatchers("/api/topics/**").permitAll()
                        .requestMatchers("/api/topic-progress/**").permitAll()
                        .requestMatchers("/api/assessment/**").permitAll()
                        .requestMatchers("/api/teacher/analytics/**").permitAll()
                        .requestMatchers("/api/homeroom/analytics/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()

                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {}); // CORS mặc định theo bean corsConfigurationSource()

        return http.build();
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOriginPatterns(Arrays.asList("*")); // cho phép tất cả origins
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        configuration.setAllowedHeaders(Arrays.asList("*"));
//        configuration.setAllowCredentials(false);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//@Bean
//public CorsConfigurationSource corsConfigurationSource() {
//    CorsConfiguration configuration = new CorsConfiguration();
//
//    // Dùng allowedOrigins thay vì allowedOriginPatterns
//    configuration.setAllowedOrigins(Arrays.asList(
//            "http://localhost:5173",
//            "http://localhost:3000"
//    ));
//
//    configuration.setAllowedMethods(Arrays.asList(
//            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
//    ));
//    configuration.setAllowedHeaders(Arrays.asList("*"));
//    configuration.setAllowCredentials(true); // Bắt buộc cho WebSocket
//    configuration.setMaxAge(3600L);
//
//    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//    source.registerCorsConfiguration("/**", configuration);
//    return source;
//}
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    //Cho phép tất cả origins (bao gồm mobile apps)
    configuration.addAllowedOriginPattern("*");

    configuration.addAllowedMethod("*");
    configuration.addAllowedHeader("*");
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}

}