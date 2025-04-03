package source.inysoft.kms.service.api.core;


import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeSetting;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.Repository.customize.CustomizeSettingRepository;
import source.inysoft.kms.jwt.JwtUtil;
import source.inysoft.kms.service.auth.UserDetailsServiceImpl;

import javax.persistence.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class MemberService {

    @Autowired
    protected UserDetailsServiceImpl userDetailsService;
    @Autowired
    protected CustomizeMemberRepository memberRepository;
    @Autowired
    CustomizeSettingRepository settingRepository;
    @Autowired
    protected JwtUtil jwtUtil;


    public String imagePath = "fileUpload/member/image";

    /*
     *@  로그인
     * params : uid,upass
     * return : {status: -> message,success.fail} access_token
     */
    @Transactional
    public HashMap<String, Object> memberLogin(String uid, String pass, HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        if (!uid.isEmpty() && !pass.isEmpty()) {
            Optional<CustomizeMember> findMember = memberRepository.findByUid(uid);
            if (!findMember.isPresent()) {
                result.put("code", "wrong");
                result.put("status", "message");
            } else {
                if (!findMember.get().getRole().equals(Role.ROLE_ADMIN)) {
                    if (!findMember.get().getAuth().equals("yes")) {
                        result.put("code", "notauth");
                        result.put("status", "message");
                        return result;
                    }
                    if (findMember.get().getUout().equals("yes")) {
                        result.put("code", "uout");
                        result.put("status", "message");
                        return result;
                    }
                }


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
                    result.put("code", "wrong");
                    result.put("status", "message");
                }
            }
        } else {
            result.put("status", "fail");
        }

        return result;
    }


    /*
     *@ 회원정보
     * params : session
     * return : {status: -> message,success.fail}
     */

    public HashMap<String, Object> getMemberInfo(HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            HashMap<String, Object> memberInfo = new HashMap<String, Object>();
            memberInfo.put("id", member.getId());
            memberInfo.put("uid", member.getUid());
            memberInfo.put("name", member.getName());
            memberInfo.put("email", member.getEmail());
            memberInfo.put("emailSend", member.getEmailSend());
            memberInfo.put("busiName", member.getBusiName());
            memberInfo.put("role", member.getRole());
            memberInfo.put("nick", member.getNickName());
            memberInfo.put("img", member.getImg());

            result.put("memberInfo", memberInfo);
            result.put("status", "success");
        } else {
            result.put("status", "fail");
        }


        return result;
    }


    /*
     *@ 아이디,닉네임,이메일 중복 여부 검사
     * params : type=> (uid,email,nick) , key=>value
     * return : {status: -> message,success.fail}
     */
    public HashMap<String, Object> checkDoubleKey(String type, String key) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (!key.isEmpty() && !type.isEmpty()) {
            Boolean flag = false;
            switch (type) {
                case "uid":
                    Optional<CustomizeMember> member = memberRepository.findByUid(key);
                    if (member.isEmpty()) flag = true;
                    break;
                case "email":
                    CustomizeMember emailMember = memberRepository.findByEmail(key);
                    if (emailMember == null) flag = true;
                    break;
                case "nick":
                    CustomizeMember nickMember = memberRepository.findByNickName(key);
                    if (nickMember == null) flag = true;
                    break;

            }

            if (flag) {
                result.put("status", "success");
            } else {
                result.put("status", "message");

            }
        } else {
            result.put("status", "fail");
        }

        return result;
    }


    /*
     *@  마이페이지 닉네임 중복 여부 검사
     * params :
     * return : {status: -> message,success.fail}
     */
    public HashMap<String, Object> checkMemberNick(HashMap<String, String> params, HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (session.getAttribute("user_id") != null) {
            Long id = (Long) session.getAttribute("user_id");
            String nick = params.get("nick");
            CustomizeMember nickMember = memberRepository.findByNickNameAndIdNot(nick, id);
            if (nickMember == null) {
                result.put("status", "success");
            } else {
                result.put("status", "message");

            }
        } else {
            result.put("status", "fail");
        }
        return result;
    }

    /*
     *@ 회원가입
     * params :  uid,nick,email.upass,emailSend,name
     * return : {status: -> message,success.fail}
     */
    @Transactional
    public HashMap<String, Object> joinMember(HashMap<String, String> params, HttpServletRequest request) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String uid = params.get("uid");
        Boolean flag = false;
        if (!uid.isEmpty()) {
            Optional<CustomizeMember> member = memberRepository.findByUid(uid);
            if (!member.isEmpty()) {
                result.put("code", "doubleUid");
                result.put("status", "message");
                return result;
            }
        } else {
            flag = true;
        }
        String email = params.get("email");
        if (!email.isEmpty()) {
            CustomizeMember emailMember = memberRepository.findByEmail(email);
            if (emailMember != null) {
                result.put("code", "doubleEmail");
                result.put("status", "message");
                return result;
            }
        } else {
            flag = true;
        }
        String nick = params.get("nick");
        if (!nick.isEmpty()) {
            CustomizeMember nickMember = memberRepository.findByNickName(nick);
            if (nickMember != null) {
                result.put("code", "doubleNick");
                result.put("status", "message");
                return result;
            }
        }
        String roleStr = params.get("role");
        if (roleStr.isEmpty()) {
            flag = true;
        }
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String upass = params.get("upass");
        if (upass.isEmpty()) {
            flag = true;
        } else {
            upass = encoder.encode(upass);
        }
        String name = params.get("name");
        if (name.isEmpty()) {
            flag = true;
        }
        Role role = Role.ROLE_MEMBER;
        String busiName = "";
        if (roleStr.equals("busi")) {
            role = Role.ROLE_MANAGER;
            busiName = params.get("busiName");
            if (busiName.isEmpty()) {
                flag = true;
            }
        }
        String emailSend = (!params.get("emailSend").isEmpty()) ? params.get("emailSend") : "no";

        if (flag) {
            result.put("status", "fail");
            return result;
        }
        String uidEncode = encoder.encode(uid);

        String auth = "yes";
        String subject = "";
        String content = "";
        String use = "no";

        List<CustomizeSetting> settingList = settingRepository.findAll();
        if (settingList.size() > 0) {
            CustomizeSetting setting = settingRepository.getFindById(settingList.get(0).getId());
            String emailSettingString = setting.getEmail();
            JSONObject jObject = new JSONObject(emailSettingString);
            JSONObject obj = jObject.getJSONObject("join");
            use = obj.getString("use");
            if (use.equals("yes")) {
                auth = "no";
                subject = obj.getString("subject");
                content = obj.getString("content");
            }
        }

        CustomizeMember member = CustomizeMember.builder()
                .uid(uid)
                .uidEncode(uidEncode)
                .auth(auth)
                .busiName(busiName)
                .passwd(upass)
                .role(role)
                .name(name)
                .nickName(nick)
                .email(email)
                .emailSend(emailSend)
                .uout("no")
                .actType("insert")
                .build();

        CustomizeMember resultMember = memberRepository.save(member);
        if (use.equals("yes") && resultMember != null && !resultMember.getUid().isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            String joinDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String siteUrl = getSiteUrl(request);
            String joinAuthUrl = "<a href='"+siteUrl+"/emailAuth/" + uidEncode + "' target='_blank'>";
            content = content.replace("{{uid}}", uid);
            content = content.replace("{{name}}", name);
            content = content.replace("{{joinDate}}", joinDate);
            content = content.replace("{{joinAuthUrl}}", joinAuthUrl);
            content = content.replace("{{/joinAuthUrl}}", "</a>");

            subject = subject.replace("{{uid}}", uid);
            subject = subject.replace("{{name}}", name);
            subject = subject.replace("{{joinDate}}", joinDate);

            result.put("mailTo", email);
            result.put("mailSubject", subject);
            result.put("mailContent", content);
            result.put("code", "emailAuth");
            result.put("status", "success");
        } else if (resultMember != null && !resultMember.getUid().isEmpty()) {
            result.put("code", "auth");
            result.put("status", "success");
        } else {
            result.put("code", "error");
            result.put("status", "message");
            return result;
        }

        return result;
    }

    /*
     *@  회원 인증메일 확인
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> setAuthEmail(String code) {
        HashMap<String, Object> result = new HashMap<String, Object>();

        CustomizeMember member = memberRepository.findByUidEncode(code);
        if (member != null && !member.getUid().isEmpty()) {
            if (member.getAuth().equals("no")) {
                HashMap<String, String> updateParams = new HashMap<String, String>();
                updateParams.put("updateType", "auth");
                updateParams.put("auth", "yes");
                memberUpdateActive(member.getId(), updateParams);
                result.put("status", "success");
            } else {
                result.put("status", "fail");
            }

        } else {
            result.put("status", "fail");
        }


        return result;
    }

    /*
     *@  비밀번호 변경시 변경링크 전송
     * params :
     * return : {status:(message,success,fail)}
     */
    @Transactional
    public HashMap<String, Object> upassLinkWithEmail(String email, HttpServletRequest request) {
        HashMap<String, Object> result = new HashMap<String, Object>();

        CustomizeMember member = memberRepository.findByEmail(email);
        if (member != null && !member.getUid().isEmpty()) {
            if (member.getAuth().equals("yes")) {

                List<CustomizeSetting> settingList = settingRepository.findAll();
                if (settingList.size() > 0) {
                    CustomizeSetting setting = settingRepository.getFindById(settingList.get(0).getId());
                    String emailSettingString = setting.getEmail();
                    JSONObject jObject = new JSONObject(emailSettingString);
                    JSONObject obj = jObject.getJSONObject("findpass");
                    String use = obj.getString("use");
                    if (use.equals("yes")) {
                        String subject = obj.getString("subject");
                        String content = obj.getString("content");

                        String uid = member.getUid();
                        String name = member.getName();

                        PasswordEncoder encoder = new BCryptPasswordEncoder();
                        String uidEncode = encoder.encode(uid);
                        memberRepository.updateSQLUidEncode(uidEncode, uid);

                        String siteUrl = getSiteUrl(request);
                        String findPassUrl = "<a href='"+siteUrl+"/memberPassword/" + uidEncode + "' target='_blank'>";

                        content = content.replace("{{uid}}", uid);
                        content = content.replace("{{name}}", name);
                        content = content.replace("{{findPassUrl}}", findPassUrl);
                        content = content.replace("{{/findPassUrl}}", "</a>");

                        subject = subject.replace("{{uid}}", uid);
                        subject = subject.replace("{{name}}", name);

                        result.put("mailTo", email);
                        result.put("mailSubject", subject);
                        result.put("mailContent", content);
                        result.put("status", "success");

                    } else {
                        result.put("code", "noEmailSetting");
                        result.put("status", "message");
                    }
                } else {
                    result.put("code", "noEmailSetting");
                    result.put("status", "message");
                }


            } else {
                result.put("code", "noAuth");
                result.put("status", "message");
            }

        } else {
            result.put("code", "noEmail");
            result.put("status", "message");
        }

        return result;
    }

    /*
     *@  비밀번호 찾기시 비밀번호 변경
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> updateUpassWithEmail(HashMap<String, String> params) {
        HashMap<String, Object> result = new HashMap<String, Object>();
        String uidEncode = params.get("uidEncode");
        String newPass = params.get("newPass");
        CustomizeMember member = memberRepository.findByUidEncode(uidEncode);
        if (member != null && !member.getUid().isEmpty()) {
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String upass = encoder.encode(newPass);
            HashMap<String, String> updateParams = new HashMap<String, String>();
            updateParams.put("updateType", "password");
            updateParams.put("password", upass);
            memberUpdateActive(member.getId(), updateParams);
            result.put("status", "success");

        } else {
            result.put("status", "fail");
        }
        return result;

    }

    /*
     *@  회원정보 변경
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> updateMember(HashMap<String, String> params, HttpSession session) {
        HashMap<String, Object> result = new HashMap<String, Object>();

        Long id = (Long) session.getAttribute("user_id");
        HashMap<String, String> updateParams = new HashMap<String, String>();
        updateParams.put("updateType", "nickEmailSend");
        updateParams.put("nick", params.get("nick"));
        updateParams.put("emailSend", params.get("emailSend"));

        memberUpdateActive(id, updateParams);
        result.put("status", "success");
        return result;
    }

    private void memberUpdateActive(Long id, HashMap<String, String> params) {
        CustomizeMember isMember = memberRepository.getById(id);
        if (isMember != null) {
            String img = isMember.getImg();
            String nick = isMember.getNickName();
            String emailSend = isMember.getEmailSend();
            String password = isMember.getPasswd();
            String auth = isMember.getAuth();
            String uout = isMember.getUout();

            switch (params.get("updateType")) {
                case "img":
                    img = params.get("img");
                    break;
                case "nickEmailSend":
                    nick = params.get("nick");
                    emailSend = params.get("emailSend");
                    break;
                case "password":
                    password = params.get("password");
                    break;
                case "auth":
                    auth = params.get("auth");
                    break;
                case "out":
                    uout = "yes";
                    break;
            }
            CustomizeMember member = CustomizeMember.builder()
                    .uid(isMember.getUid())
                    .busiName(isMember.getBusiName())
                    .passwd(password)
                    .auth(auth)
                    .role(isMember.getRole())
                    .name(isMember.getName())
                    .nickName(nick)
                    .email(isMember.getEmail())
                    .emailSend(emailSend)
                    .img(img)
                    .uout(uout)
                    .createAt(isMember.getCreateAt())
                    .actType("update")
                    .actId(id)
                    .build();

            memberRepository.save(member);
        }
    }

    /*
     *@  비밀번호 변경
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> updateMemberPassword(HashMap<String, String> params, HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (session.getAttribute("user_id") != null) {
            Long id = (Long) session.getAttribute("user_id");
            CustomizeMember isMember = memberRepository.getById(id);

            String nowPass = params.get("nowPass");
            String newPass = params.get("newPass");
            if (nowPass == null || nowPass.isEmpty() || newPass == null || newPass.isEmpty()) {
                result.put("status", "fail");
                return result;
            }
            PasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(nowPass, isMember.getPasswd())) {

                String upass = encoder.encode(newPass);
                HashMap<String, String> updateParams = new HashMap<String, String>();
                updateParams.put("updateType", "password");
                updateParams.put("password", upass);
                memberUpdateActive(id, updateParams);
                result.put("status", "success");
            } else {
                result.put("status", "message");
            }

        } else {
            result.put("status", "fail");
        }
        return result;
    }

    /*
     *@  회원탈퇴
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> memberOut(HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (session.getAttribute("user_id") != null) {
            Long id = (Long) session.getAttribute("user_id");
            CustomizeMember isMember = memberRepository.getById(id);

            if (isMember!=null && !isMember.getUid().isEmpty()) {

                HashMap<String, String> updateParams = new HashMap<String, String>();
                updateParams.put("updateType", "out");
                memberUpdateActive(id, updateParams);
                session.invalidate(); // 세션삭제

                result.put("status", "success");
            } else {
                result.put("status", "fail");
            }

        } else {
            result.put("status", "fail");
        }
        return result;
    }


    /*
     *@  이미지 저장
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> updateImage(MultipartFile dFile, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        Path currentPath = Paths.get("");
        String sitePath = currentPath.toAbsolutePath().toString();

        Date now = new Date();
        Long nowTime = now.getTime();

        String absolutePath = new File(sitePath).getAbsolutePath() + "/"; // 파일이 저장될 절대 경로
        String newFileName = "image_" + nowTime;
        String fileExtension = '.' + dFile.getOriginalFilename().replaceAll("^.*\\.(.*)$", "$1"); // 정규식 이용하여 확장자만 추출

        try {
            if (!dFile.isEmpty()) {
                File file = new File(absolutePath + imagePath);
                if (!file.exists()) {
                    file.mkdirs(); // mkdir()과 다르게 상위 폴더가 없을 때 상위폴더까지 생성
                }
                File saveFile = new File(absolutePath + imagePath + "/" + newFileName + fileExtension);
                dFile.transferTo(saveFile);
                if (session.getAttribute("user_id") != null) {
                    Long user_id = (Long) session.getAttribute("user_id");
                    HashMap<String, String> updateParams = new HashMap<String, String>();
                    updateParams.put("updateType", "img");
                    updateParams.put("img", newFileName + fileExtension);
                    memberUpdateActive(user_id, updateParams);

                    result.put("memImg", newFileName + fileExtension);
                    result.put("status", "success");
                }
            } else {
                result.put("status", "fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /*
     *@ 회원 이미지 가져오기
     * params :
     * return :
     */
    public byte[] getMemberImage(String imgName) throws IOException {

        FileInputStream fis = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Path currentPath = Paths.get("");
        String sitePath = currentPath.toAbsolutePath().toString();
        String absolutePath = new File(sitePath).getAbsolutePath() + "/"; // 파일이 저장될 절대 경로

        try {
            fis = new FileInputStream(absolutePath + imagePath + "/" + imgName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int readCount = 0;
        byte[] buffer = new byte[1024];
        byte[] fileArray = null;


        try {
            while ((readCount = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, readCount);
            }
            fileArray = baos.toByteArray();
            fis.close();
            baos.close();
        } catch (IOException e) {
            throw new RuntimeException("File Error");
        }
        return fileArray;
    }

    private String getSiteUrl(HttpServletRequest request) {
        String protocol = request.isSecure() ? "https://" : "http://";
        String url = protocol+request.getServerName();
        return url;
    }
}


