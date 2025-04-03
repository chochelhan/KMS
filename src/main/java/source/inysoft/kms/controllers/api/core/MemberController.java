package source.inysoft.kms.controllers.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.opensearch.core.OpenSearch;
import source.inysoft.kms.service.api.core.MailService;
import source.inysoft.kms.service.api.customize.CustomizeMemberService;
import source.inysoft.kms.service.api.customize.CustomizeStatisticsService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;


public abstract class MemberController {

    @Autowired
    protected CustomizeMemberService memberService;

    @Autowired
    protected CustomizeStatisticsService statisticsService;

    @Autowired
    protected MailService mailService;

    @PostMapping("login")
    public ResponseEntity<HashMap<String, Object>> memberLogin(@RequestBody HashMap<String, String> params, HttpServletRequest request) {
        String uid = params.get("uid");
        String upass = params.get("upass");
        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = memberService.memberLogin(uid,upass,session);

        String status = (String) resultMap.get("status");
        if(status.equals("success")) {
            String sessionId = request.getHeader("sessionId"); // 헤더 파싱
            statisticsService.setMemberLogin(sessionId,resultMap);
        }

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    /**
     * @ 인증메일
     * @param params
     * @return
     */
    @PostMapping("updateAuthEmail")
    public ResponseEntity<HashMap<String, Object>> updateAuthEmail(@RequestBody HashMap<String, String> params) {
        String code = params.get("code");
        HashMap<String, Object> resultMap = memberService.setAuthEmail(code);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }
    /**
     * @ 비밀번호 변경 메일발송
     * @param params
     * @return
     */
    @PostMapping("findMemberUpass")
    public ResponseEntity<HashMap<String, Object>> findMemberUpass(@RequestBody HashMap<String, String> params, HttpServletRequest request) {
        String email = params.get("email");
        HashMap<String, Object> resultMap = memberService.upassLinkWithEmail(email,request);
        String status = (String) resultMap.get("status");
        if(status.equals("success")) {
            String to = (String) resultMap.get("mailTo");
            String subject = (String) resultMap.get("mailSubject");
            String content = (String) resultMap.get("mailContent");
            mailService.mailSend(to,subject,content);
        }
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    /**
     * @ 메일링크에서 비밀번호 변경
     * @param params
     * @return
     */
    @PostMapping("updateEmailMemberUpass")
    public ResponseEntity<HashMap<String, Object>> updateEmailMemberUpass(@RequestBody HashMap<String, String> params) {
        HashMap<String, Object> resultMap = memberService.updateUpassWithEmail(params);
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }


    @PostMapping("checkUid")
    public ResponseEntity<HashMap<String, Object>> checkUid(@RequestBody HashMap<String, String> params) {

        String uid = params.get("uid");
        HashMap<String, Object> resultMap = memberService.checkDoubleKey("uid",uid);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    @PostMapping("checkEmail")
    public ResponseEntity<HashMap<String, Object>> checkEmail(@RequestBody HashMap<String, String> params) {

        String email = params.get("email");
        HashMap<String, Object> resultMap = memberService.checkDoubleKey("email",email);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    @PostMapping("checkNick")
    public ResponseEntity<HashMap<String, Object>> checkNick(@RequestBody HashMap<String, String> params) {

        String nick = params.get("nick");
        HashMap<String, Object> resultMap = memberService.checkDoubleKey("nick",nick);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    /**
     *  마이페이지 닉네임 체크
     * @return
     */
    @PostMapping("checkMemberNick")
    public ResponseEntity<HashMap<String, Object>> checkMemberNick(@RequestBody HashMap<String, String> params, HttpServletRequest request) {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = memberService.checkMemberNick(params,session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /*
    *@ 회원가입
    * params: uid,nick,email.upass,emailSend,name
    *
    */

    @PostMapping("join")
    public ResponseEntity<HashMap<String, Object>> memberJoin(@RequestBody HashMap<String, String> params,HttpServletRequest request) {

        HashMap<String, Object> resultMap = memberService.joinMember(params, request);

        String status = (String) resultMap.get("status");
        String code = (String) resultMap.get("code");
        if(status.equals("success") && code.equals("emailAuth")) {
            String to = (String) resultMap.get("mailTo");
            String subject = (String) resultMap.get("mailSubject");
            String content = (String) resultMap.get("mailContent");

            mailService.mailSend(to,subject,content);
        }

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }



    /**
     * 회원 이미지 가져오기
     * @param imgName
     * @return
     */
    @GetMapping("getMemberImage")
    public  @ResponseBody byte[] getMemberImage(@RequestParam(name = "imgName") String imgName) throws IOException {

        byte[] imageUrl = memberService.getMemberImage(imgName);
        return imageUrl;
    }



}



