package source.inysoft.kms.controllers.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import source.inysoft.kms.service.api.customize.CustomizeCommentService;
import source.inysoft.kms.service.api.customize.CustomizeMemberNoticeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;


public abstract class CommentController {

    @Autowired
    protected CustomizeCommentService commentService;

    @Autowired
    protected CustomizeMemberNoticeService memberNoticeService;

    /**
     * 댓글  목록
     * @param params
     * @return
     */

    @PostMapping("getCommentList")
    public ResponseEntity<HashMap<String, Object>> getCommentList(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> resultMap = commentService.getCommentList(params);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    /**
     * 댓글 저장
     * @param params
     * @return
     */
    @PostMapping("insertComment")
    public ResponseEntity<HashMap<String, Object>> insertComment(@RequestBody HashMap<String, Object> params, HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = commentService.insertComment(params,session);

        String resultStatus = (String) resultMap.get("status");
        String pid = (String) params.get("pid");
        if ((pid == null || pid.isEmpty()) && resultStatus.equals("success")) {
                memberNoticeService.insertMemberNoticeWithComment(resultMap);

        }
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }


    /**
     * 댓글 수정
     * @param params
     * @return
     */
    @PostMapping("updateComment")
    public ResponseEntity<HashMap<String, Object>> updateComment(@RequestBody HashMap<String, Object> params, HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = commentService.updateComment(params, session);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }
    /**
     * 댓글 삭제
     * @param params
     * @return
     */
    @PostMapping("deleteComment")
    public ResponseEntity<HashMap<String, Object>> deleteComment(@RequestBody HashMap<String, Object> params, HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = commentService.deleteComment(params, session);
        String resultStatus = (String) resultMap.get("status");
        if(resultStatus.equals("success")) {
            String id = (String) resultMap.get("id");
            memberNoticeService.deleteMemberNotice(id);
        }

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }





}



