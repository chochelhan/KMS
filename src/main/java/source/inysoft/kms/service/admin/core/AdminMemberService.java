package source.inysoft.kms.service.admin.core;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.Repository.customize.specification.CustomizeMemberSpecification;
import source.inysoft.kms.jwt.JwtUtil;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;
import source.inysoft.kms.service.auth.UserDetailsServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class AdminMemberService {

    @Autowired
    protected UserDetailsServiceImpl userDetailsService;
    @Autowired
    protected CustomizeMemberRepository memberRepository;
    @Autowired
    protected JwtUtil jwtUtil;

    @Autowired
    protected CustomizeBoardArticleSearch customizeBoardArticleSearch;

    /*
     *@ 관리자 초기 정보 입력
     * params :  uid,upass,email,name
     * return : {status: -> message,success.fail}
     */
    @Transactional
    public HashMap<String, Object> insertAdmin(HashMap<String, String> params, HttpSession session) throws IOException {

        customizeBoardArticleSearch.createIndex();

        HashMap<String, Object> result = new HashMap<String, Object>();
        /*
        List<CustomizeMember> adminMmember = memberRepository.findByRole(Role.ROLE_ADMIN);
        if (adminMmember.size() > 0) {
            result.put("status", "fail");
            return result;
        }
        String uid = params.get("uid");
        Boolean flag = false;
        if (!uid.isEmpty()) {
            Optional<CustomizeMember> member = memberRepository.findByUid(uid);
            if (!member.isEmpty()) {
                result.put("status", "doubleUid");
                return result;
            }
        } else {
            flag = true;
        }
        String email = params.get("email");
        if (!email.isEmpty()) {
            CustomizeMember emailMember = memberRepository.findByEmail(email);
            if (emailMember != null) {
                result.put("status", "doubleEmail");
                return result;
            }
        } else {
            flag = true;
        }
        String upass = params.get("upass");
        if (upass.isEmpty()) {
            flag = true;
        } else {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            upass = encoder.encode(upass);
        }


        if (name.isEmpty()) {
            flag = true;
        }
        if (flag) {
            result.put("status", "fail");
            return result;
        }

         */
        String uid = params.get("uid");
        String email = params.get("email");
        String upass = params.get("upass");
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        upass = encoder.encode(upass);

        String name = params.get("name");
        String emailSend = "no";
        Role role = Role.ROLE_ADMIN;
        CustomizeMember member = CustomizeMember.builder()
                .uid(uid)
                .passwd(upass)
                .role(role)
                .name(name)
                .email(email)
                .emailSend(emailSend)
                .uout("no")
                .actType("insert")
                .build();

        memberRepository.save(member);


        UserDetails userDetails = userDetailsService.loadUserByUsername(member.getUid());
        String token = jwtUtil.generateToken(userDetails);
        Optional<CustomizeMember> findMember = memberRepository.findByUid(uid);

        session.setMaxInactiveInterval(36000);
        session.setAttribute("user_id", findMember.get().getId());

        result.put("access_token", token);
        result.put("status", "success");

        return result;
    }

    /*
     *@ 관리자 로그인
     * params : uid,upass
     * return : {status: -> message,success.fail} access_token
     */
    public HashMap<String, Object> adminLogin(String uid, String pass, HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (!uid.isEmpty() && !pass.isEmpty()) {
            Optional<CustomizeMember> findMember = memberRepository.findByUid(uid);
            if (!findMember.isPresent()) {
                result.put("status", "message");
            } else {
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                if (encoder.matches(pass, findMember.get().getPasswd())) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(findMember.get().getUid());
                    String token = jwtUtil.generateToken(userDetails);
                    result.put("access_token", token);
                    result.put("memberInfo", findMember.get());

                    session.setMaxInactiveInterval(36000);
                    session.setAttribute("user_id", findMember.get().getId());


                    result.put("status", "success");
                } else {
                    result.put("status", "message");
                }
            }

        } else {
            result.put("status", "fail");
        }

        return result;
    }


    public HashMap<String, Object> getAdminInfo(HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            if (member.getRole().equals(Role.ROLE_ADMIN)) {
                result.put("name", member.getName());
                result.put("email", member.getEmail());
                result.put("status", "success");
            } else {
                result.put("status", "fail");
            }
        } else {
            result.put("status", "fail");
        }
        return result;
    }

    @Transactional
    public HashMap<String, Object> updateAdminInfo(HashMap<String, String> params, HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            if (member.getRole().equals(Role.ROLE_ADMIN)) {

                String name = params.get("name");
                String email = params.get("email");
                String nowUpass = params.get("nowUpass");
                String newPass = params.get("upass");

                if (nowUpass == null || nowUpass.isEmpty()) {
                    result.put("status", "fail");
                    return result;
                }
                PasswordEncoder encoder = new BCryptPasswordEncoder();
                if (encoder.matches(nowUpass, member.getPasswd())) {

                    String upass = member.getPasswd();
                    if (newPass != null && !newPass.isEmpty()) {
                        upass = encoder.encode(newPass);
                    }
                    String img = member.getImg();
                    Long id = member.getId();
                    CustomizeMember UpdMember = CustomizeMember.builder()
                            .uid(member.getUid())
                            .passwd(upass)
                            .role(member.getRole())
                            .name(name)
                            .auth("yes")
                            .email(email)
                            .img(img)
                            .uout("no")
                            .actType("update")
                            .actId(id)
                            .build();

                    memberRepository.save(UpdMember);
                    result.put("status", "success");
                } else {
                    result.put("status", "message");
                }
            } else {

                result.put("status", "fail");
            }

        } else {
            result.put("status", "fail");
        }
        return result;
    }


    public HashMap<String, Object> adminLogout(HttpServletRequest request) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        HttpSession session = request.getSession();
        session.invalidate();
        result.put("status", "success");

        return result;
    }

    /*
     *@ 회원정보 목록
     * params : role=>회원구분 (indi:일반,busi:기업)
     *         orderByField=>정렬 기준필드
     *          orderBySort=>정렬 asc, desc
     *          limit => 한번에 보일 목록수
     *          page => 페이지
     *          keywordCmd => 검색필드
     *          keyword => 검색어
     *          stdate => 검색 시작 등록일
     *          endate => 검색 종료 등록일
     *          emailSend => 메일수신 동의 여부 (yes,no)
     *          mstatus => 탈퇴여부 (out,ing)
     * return : {status: ->success} {data: org.springframework.data.domain.Page}
     */
    public HashMap<String, Object> getMemberListByIndi(HashMap<String, String> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String roleString = (!params.get("role").isEmpty()) ? params.get("role") : "indi";
        Role role = Role.ROLE_MEMBER;
        if (roleString.equals("busi")) {
            role = Role.ROLE_MANAGER;
        }


        String orderByField = (!params.get("orderByField").isEmpty()) ? params.get("orderByField") : "createAt";
        String orderBySort = (!params.get("orderBySort").isEmpty()) ? params.get("orderBySort") : "desc";
        Sort.Direction sort = Sort.Direction.ASC;

        if (orderBySort.equals("desc")) {
            sort = Sort.Direction.DESC;

        }
        String limit = (!params.get("limit").isEmpty()) ? params.get("limit") : "20";
        int limitNum = Integer.parseInt(limit);
        String page = (!params.get("page").isEmpty()) ? params.get("page") : "1";
        int pageNum = Integer.parseInt(page) - 1;

        Specification<CustomizeMember> where = (root, query, criteriaBuilder) -> null;
        where = where.and(CustomizeMemberSpecification.equalRole(role));

        String keywordCmd = params.get("keywordCmd");
        String keyword = params.get("keyword");
        if (!keywordCmd.isEmpty() && !keyword.isEmpty()) {
            switch (keywordCmd) {
                case "name":
                    where = where.and(CustomizeMemberSpecification.likeName(keyword));
                    break;
                case "uid":
                    where = where.and(CustomizeMemberSpecification.likeUid(keyword));
                    break;
                case "nickName":
                    where = where.and(CustomizeMemberSpecification.likeNickName(keyword));
                    break;
                case "email":
                    where = where.and(CustomizeMemberSpecification.likeEmail(keyword));
                    break;
            }
        }
        String emailSend = params.get("emailSend");
        if (!emailSend.isEmpty()) {
            where = where.and(CustomizeMemberSpecification.equalEmailSend(emailSend));

        }
        String auth = params.get("auth");
        if (!auth.isEmpty()) {
            where = where.and(CustomizeMemberSpecification.equalAuth(auth));

        }
        String mstatus = params.get("mstatus");
        if(mstatus.equals("out")) {
            where = where.and(CustomizeMemberSpecification.equalNotUout("no"));
        }

        String stdateString = params.get("stdate");
        String endateString = params.get("endate");
        if (!stdateString.isEmpty() && !endateString.isEmpty()) {
            stdateString = stdateString + " 00:00:00.111";
            endateString = endateString + " 23:59:59.111";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            LocalDateTime stdate = LocalDateTime.parse(stdateString, formatter);
            LocalDateTime endate = LocalDateTime.parse(endateString, formatter);

            where = where.and(CustomizeMemberSpecification.startCreateAt(stdate));
            where = where.and(CustomizeMemberSpecification.endCreateAt(endate));
        }

        Pageable paging = PageRequest.of(pageNum, limitNum, sort, orderByField);
        Page<CustomizeMember> member = memberRepository.findAll(where, paging);
        result.put("status", "success");
        result.put("data", member);

        return result;
    }

    /*
     *@ 회원정보 수정

     */
    @Transactional
    public HashMap<String, Object> updateMemberInfo(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String uid = (String) params.get("uid");
        Optional<CustomizeMember> member = memberRepository.findByUid(uid);

        Long id = member.get().getId();
        String name = (String) params.get("name");
        String emailSend = (String) params.get("emailSend");
        String busiName = (String) params.get("busiName");
        String nickName = (String) params.get("nick");
        String email = (String) params.get("email");
        String auth = (String) params.get("auth");

        if(nickName!=null && !nickName.isEmpty()) {
            CustomizeMember nickMember = memberRepository.findByNickNameAndIdNot(nickName, id);
            if (nickMember != null) {
                result.put("code", "doubleNick");
                result.put("status", "message");
                return result;
            }

        }

        CustomizeMember emailMember = memberRepository.findByEmailAndIdNot(email, id);
        if (emailMember != null) {
            result.put("code", "doubleEmail");
            result.put("status", "message");
            return result;
        }

        String img = member.get().getImg();
        CustomizeMember UpdMember = CustomizeMember.builder()
                .uid(member.get().getUid())
                .auth(auth)
                .passwd(member.get().getPasswd())
                .role(member.get().getRole())
                .busiName(busiName)
                .name(name)
                .nickName(nickName)
                .email(email)
                .emailSend(emailSend)
                .img(img)
                .uout(member.get().getUout())
                .createAt(member.get().getCreateAt())
                .actType("update")
                .actId(id)
                .build();

        memberRepository.save(UpdMember);
        result.put("status", "success");


        return result;
    }


    /*
     *@ 회원정보 엑셀 업로드

     */
    @Transactional
    public HashMap<String, Object> uploadMemberExcel(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String roleString = (String) params.get("role");
        Role role = Role.ROLE_MEMBER;
        if(roleString.equals("busi")) {
            role = Role.ROLE_MANAGER;
        }
        List<HashMap<String, String>> dataList = (List<HashMap<String, String>>) params.get("list");
        for (HashMap<String, String> data : dataList) {
            String uid = data.get("uid");
            String name = data.get("name");
            String emailSend = data.get("emailSend");
            String busiName = data.get("busiName");
            String nickName = data.get("nick");
            String email = data.get("email");

            String passwdString = data.get("upass");
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String passwd = encoder.encode(passwdString);


            Optional<CustomizeMember> member = memberRepository.findByUid(uid);
            if (!member.isEmpty()) {
                result.put("code", "doubleUid");
                result.put("status", "message");
                return result;
            }

            CustomizeMember emailMember = memberRepository.findByEmail(email);
            if (emailMember != null) {
                result.put("code", "doubleEmail");
                result.put("status", "message");
                return result;
            }
            if(nickName!=null && !nickName.isEmpty()) {
                CustomizeMember nickMember = memberRepository.findByNickName(nickName);
                if (nickMember != null) {
                    result.put("code", "doubleNick");
                    result.put("status", "message");
                    return result;
                }
            }
            CustomizeMember insMember = CustomizeMember.builder()
                    .uid(uid)
                    .auth("yes")
                    .passwd(passwd)
                    .role(role)
                    .busiName(busiName)
                    .name(name)
                    .nickName(nickName)
                    .email(email)
                    .emailSend(emailSend)
                    .uout("no")
                    .actType("insert")
                    .build();

            memberRepository.save(insMember);

        }
        result.put("status", "success");
        return result;
    }

    /*
     *@ 회원정보 삭제

     */
    @Transactional
    public HashMap<String, Object> deleteMember(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        List<String> dataList = (List<String>) params.get("list");
        for (String idString : dataList) {
            Long id = Long.parseLong(idString);
            memberRepository.deleteById(id);
        }
        result.put("status", "success");
        return result;
    }
}


