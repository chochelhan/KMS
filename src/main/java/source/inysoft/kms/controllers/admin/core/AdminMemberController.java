package source.inysoft.kms.controllers.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import source.inysoft.kms.service.admin.customize.CustomizeAdminMemberService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

public class AdminMemberController {

    @Autowired
    protected CustomizeAdminMemberService adminMemberService;



    @PostMapping("getAdminInfo")
    public ResponseEntity<HashMap<String, Object>> getAdminInfo(HttpServletRequest request) {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminMemberService.getAdminInfo(session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("updateAdminInfo")
    public ResponseEntity<HashMap<String, Object>> updateAdminInfo(@RequestBody HashMap<String, String> params,HttpServletRequest request) {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminMemberService.updateAdminInfo(params,session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("updateMemberInfo")
    public ResponseEntity<HashMap<String, Object>> updateMemberInfo(@RequestBody HashMap<String, Object> params) {

        HashMap<String, Object> result = adminMemberService.updateMemberInfo(params);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("logout")
    public ResponseEntity<HashMap<String, Object>> logout(HttpServletRequest request) {

        HashMap<String, Object> result = adminMemberService.adminLogout(request);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("getMemberList")
    public ResponseEntity<HashMap<String, Object>> getMemberList(@RequestBody HashMap<String, String> params) {

        HashMap<String, Object> result = adminMemberService.getMemberListByIndi(params);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("uploadMemberExcel")
    public ResponseEntity<HashMap<String, Object>> uploadMemberExcel(@RequestBody HashMap<String, Object> params) {

        HashMap<String, Object> result = adminMemberService.uploadMemberExcel(params);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("deleteMember")
    public ResponseEntity<HashMap<String, Object>> deleteMember(@RequestBody HashMap<String, Object> params) {

        HashMap<String, Object> result = adminMemberService.deleteMember(params);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}



