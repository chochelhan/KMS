package source.inysoft.kms.controllers.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import source.inysoft.kms.service.admin.customize.CustomizeAdminStatisticsService;

import java.io.IOException;
import java.util.HashMap;

public class AdminStatisticsController {

    @Autowired
    protected CustomizeAdminStatisticsService adminStatisticsService;

    @PostMapping("getAdminMainInfo")
    public ResponseEntity<HashMap<String, Object>> getAdminMainInfo(@RequestBody HashMap<String, String> params) throws IOException {

        HashMap<String, Object> result = adminStatisticsService.getAdminMainInfo(params);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * @ 게시판 및 키워드 통계정보
     * @return
     * @throws IOException
     */
    @PostMapping("getContent")
    public ResponseEntity<HashMap<String, Object>> getContent() throws IOException {

        HashMap<String, Object> result = adminStatisticsService.getContent();

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /**
     * @ 회원접속 통계정보
     * @param params
     * @return
     */
    @PostMapping("getMember")
    public ResponseEntity<HashMap<String, Object>> getMember(@RequestBody HashMap<String, Object> params)  {

        HashMap<String, Object> result = adminStatisticsService.getMember(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     * @ 다운로드 통계정보
     * @param params
     * @return
     */
    @PostMapping("getDownload")
    public ResponseEntity<HashMap<String, Object>> getDownload(@RequestBody HashMap<String, Object> params)  {

        HashMap<String, Object> result = adminStatisticsService.getDownload(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }



    /**
     * @ 키워드 통계정보
     * @param params
     * @return
     * @throws IOException
     */
    @PostMapping("getKeyword")
    public ResponseEntity<HashMap<String, Object>> getKeyword(@RequestBody HashMap<String, Object> params)  {

        HashMap<String, Object> result = adminStatisticsService.getKeyword(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
