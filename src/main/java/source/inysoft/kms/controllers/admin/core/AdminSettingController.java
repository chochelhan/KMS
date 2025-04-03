package source.inysoft.kms.controllers.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import source.inysoft.kms.service.admin.customize.CustomizeAdminSettingService;

import java.io.IOException;
import java.util.HashMap;

public class AdminSettingController {

    @Autowired
    protected CustomizeAdminSettingService adminSettingService;


    @PostMapping("updateSettingMenu")
    public ResponseEntity<HashMap<String, Object>> updateSettingMenu(@RequestBody HashMap<String, String> params) {


        HashMap<String, Object> result = adminSettingService.checkUpdateSetting(params,"menu");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("updateSettingSns")
    public ResponseEntity<HashMap<String, Object>> updateSettingSns(@RequestBody HashMap<String, String> params) {

        HashMap<String, Object> result = adminSettingService.checkUpdateSetting(params,"sns");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("updateSettingAgree")
    public ResponseEntity<HashMap<String, Object>> updateSettingAgree(@RequestBody HashMap<String,String> params) {

        HashMap<String, Object> result = adminSettingService.checkUpdateSetting(params,"agree");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("updateSettingEmail")
    public ResponseEntity<HashMap<String, Object>> updateSettingEmail(@RequestBody HashMap<String,String> params) {

        HashMap<String, Object> result = adminSettingService.checkUpdateSetting(params,"email");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 메뉴 설정 정보
     */
    @PostMapping("getSettingMenu")
    public ResponseEntity<HashMap<String, Object>> getSettingMenu()  {

        HashMap<String, Object> result = adminSettingService.getSettingByType("menu");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ sns 설정 정보
     */
    @PostMapping("getSettingSns")
    public ResponseEntity<HashMap<String, Object>> getSettingSns()  {

        HashMap<String, Object> result = adminSettingService.getSettingByType("sns");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /*
     *@ 약관 설정 정보
     */
    @PostMapping("getSettingAgree")
    public ResponseEntity<HashMap<String, Object>> getSettingAgree()  {

        HashMap<String, Object> result = adminSettingService.getSettingByType("agree");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /*
     *@ 이메일 설정 정보
     */
    @PostMapping("getSettingEmail")
    public ResponseEntity<HashMap<String, Object>> getSettingEmail()  {

        HashMap<String, Object> result = adminSettingService.getSettingByType("email");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
