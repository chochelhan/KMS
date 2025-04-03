package source.inysoft.kms.controllers.admin.core;


import org.aspectj.apache.bcel.classfile.Module;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.jwt.JwtUtil;
import source.inysoft.kms.service.admin.customize.CustomizeAdminMemberService;
import source.inysoft.kms.service.auth.UserDetailsServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;


public class AdminLoginController {

    @Autowired
    protected CustomizeAdminMemberService adminMemberService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private CustomizeMemberRepository memberRepository;
    @Autowired
    private JwtUtil jwtUtil;



    @PostMapping("checkStatus")
    public ResponseEntity<HashMap<String, Object>> checkStatus(HttpServletRequest request, HttpServletResponse response) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String authorization = request.getHeader("Authorization"); // 헤더 파싱
        String username = "", token = "";

        result.put("status","message");
        result.put("data","noLogin");
        if (authorization != null && authorization.startsWith("Bearer ")) { // Bearer 토큰 파싱
            token = authorization.substring(7); // jwt token 파싱
            username = jwtUtil.getUsernameFromToken(token); // username 얻어오기
            if (username != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtUtil.isValidToken(token, userDetails)) {
                    Boolean flag = false;
                    HttpSession session = request.getSession();
                    if(session.getAttribute("user_id") !=null) {

                        //String url = request.getRequestURI();
                        Optional<CustomizeMember> findMember = memberRepository.findByUid(username);
                        if (findMember.get().getRole().equals(Role.ROLE_ADMIN)) {
                            flag = true;
                        }
                    }
                    if (flag) { // 관리자로 로그인
                        result.put("status","success");
                        result.put("data","success");
                    }
                }
            }
        } else {
            List<CustomizeMember> adminMmember = memberRepository.findByRole(Role.ROLE_ADMIN);
            if(adminMmember.size()<1) {
                result.put("data","emptyAdmin");
            }

        }

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    @PostMapping("insertAdmin")
    public ResponseEntity<HashMap<String, Object>> insertAdmin(@RequestBody HashMap<String, String> params,HttpServletRequest request)  throws IOException {
        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = adminMemberService.insertAdmin(params,session);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }


    @PostMapping("login")
    public ResponseEntity<HashMap<String, Object>> login(@RequestBody HashMap<String, String> params,HttpServletRequest request) {

        String uid = params.get("uid");
        String upass = params.get("upass");
        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = adminMemberService.adminLogin(uid,upass,session);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

}



