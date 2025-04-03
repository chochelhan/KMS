package source.inysoft.kms.controllers.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import source.inysoft.kms.service.api.customize.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;

public class SettingController {

    @Autowired
    protected CustomizeSettingService settingService;

    @Autowired
    protected CustomizeBoardArticleService boardArticleService;

    @Autowired
    protected CustomizeCommentService commentService;


    @Autowired
    protected CustomizeMemberNoticeService memberNoticeService;

    @Autowired
    protected CustomizeKeywordService keywordService;

    @Autowired
    protected CustomizeStatisticsService statisticsService;

    /*
    *@ 설정 정보 가져오기
    *
     */
    @PostMapping("getBase")
    public ResponseEntity<HashMap<String, Object>> getBase(HttpServletRequest request) {


        HttpSession session = request.getSession();

        HashMap<String, Object> result = settingService.getSetting();
        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            result.put("memberNoticeList",memberNoticeService.getMyMemberNoticeList(user_id));
        }
        result.put("keywordList",keywordService.getKeywordLimit());

        String sessionId = request.getHeader("sessionId"); // 헤더 파싱
        statisticsService.setMemberStart(sessionId);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    /*
     *@ 사이트에서 떠날때
     *
     */
    @PostMapping("outMemberStatistics")
    public ResponseEntity<HashMap<String, Object>> outMemberStatistics(HttpServletRequest request) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String sessionId = request.getHeader("sessionId"); // 헤더 파싱
        HttpSession session = request.getSession();
        statisticsService.setMemberOut(session,sessionId);
        result.put("status","success");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /*
     *@ 메인페이지
     *
     */
    @PostMapping("getMain")
    public ResponseEntity<HashMap<String, Object>> getMain()  throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        HashMap<String, Object> boardData = boardArticleService.getMainArticleList();
        HashMap<String, Object> commentData = commentService.getMainCommentList();
        result.put("boardData",boardData);
        result.put("commentData",commentData);
        result.put("status","success");

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
