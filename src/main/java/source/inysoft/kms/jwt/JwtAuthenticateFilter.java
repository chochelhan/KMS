package source.inysoft.kms.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.service.auth.UserDetailsServiceImpl;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class JwtAuthenticateFilter extends OncePerRequestFilter {

    private final UserDetailsServiceImpl userDetailsService;
    private final CustomizeMemberRepository memberRepository;
    private final JwtUtil jwtUtil;

    @JsonIgnore
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization"); // 헤더 파싱
        String username = "", token = "";

        if (authorization != null && authorization.startsWith("Bearer ")) { // Bearer 토큰 파싱
            token = authorization.substring(7); // jwt token 파싱
            username = jwtUtil.getUsernameFromToken(token); // username 얻어오기
        } else {
            filterChain.doFilter(request, response);
        }
        // 현재 SecurityContextHolder 에 인증객체가 있는지 확인
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtUtil.isValidToken(token, userDetails)) {
                Boolean flag = false;
                HttpSession session = request.getSession();
                String url = request.getRequestURI();
                if (url.contains("/api/admin/controller/")) {
                    if(session.getAttribute("user_id") !=null) {
                        Optional<CustomizeMember> findMember = memberRepository.findByUid(username);
                        if (findMember.get().getRole().equals(Role.ROLE_ADMIN)) {
                            flag = true;
                        }
                    }
                } else if (url.contains("/api/controller/mypage")) {
                    if(session.getAttribute("user_id") !=null) {
                        Optional<CustomizeMember> findMember = memberRepository.findByUid(username);
                        if (findMember.isPresent()) {
                            flag = true;
                        }
                    }
                }
                if (flag) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken
                            = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    usernamePasswordAuthenticationToken
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }

            }
            filterChain.doFilter(request, response);
        }

    }
}
