package source.inysoft.kms.controllers.api.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import source.inysoft.kms.service.api.customize.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;

public abstract class MypageController {

    @Autowired
    protected CustomizeMemberService memberService;

    @Autowired
    protected CustomizeBoardArticleService articleService;

    @Autowired
    protected CustomizeMemberNoticeService memberNoticeService;

    @Autowired
    protected CustomizeStatisticsService statisticsService;

    /**
     *  로그아웃
     * @return
     */
    @PostMapping("logout")
    public ResponseEntity<HashMap<String, Object>> memberLogout(HttpServletRequest request) {
        HashMap<String, Object> resultMap = new HashMap<String, Object>();
        HttpSession session = request.getSession();
        session.invalidate();

        String sessionId = request.getHeader("sessionId"); // 헤더 파싱
        statisticsService.setMemberLogout(sessionId);

        resultMap.put("status","success");
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }



    /**
     *  회원활동 내역 가져오기
     * @return
     */
    @PostMapping("getMyMain")
    public ResponseEntity<HashMap<String, Object>> getMyMain(@RequestBody HashMap<String, Object> params,HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> memberInfo = memberService.getMemberInfo(session);
        result.put("memberInfo",memberInfo.get("memberInfo"));

        HashMap<String, Object> articleList = articleService.getMyArticleList(params,session);
        result.put("articleList",articleList);

        Long user_id = (Long) session.getAttribute("user_id");
        result.put("memberNoticeList",memberNoticeService.getMyMemberNoticeListAll(user_id));

        result.put("status","success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     *  회원활동 내역(게시글 정보) 가져오기
     * @return
     */
    @PostMapping("getMyArticleList")
    public ResponseEntity<HashMap<String, Object>> getMyArticleList(@RequestBody HashMap<String, Object> params,HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> articleList = articleService.getMyArticleList(params,session);
        result.put("articleList",articleList);

        result.put("status","success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /**
     *  회원정보 가져오기
     * @return
     */
    @PostMapping("getMemberInfo")
    public ResponseEntity<HashMap<String, Object>> getMemberInfo(HttpServletRequest request) {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = memberService.getMemberInfo(session);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     *  회원정보 수정
     * @return
     */
    @PostMapping("updateMemberInfo")
    public ResponseEntity<HashMap<String, Object>> updateMemberInfo(@RequestBody HashMap<String, String> params,HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();

        HashMap<String, Object> result = memberService.updateMember(params,session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
    /*
     *@ 이미지 등록
     */
    @PostMapping("updateMemberImage")
    public ResponseEntity<HashMap<String, Object>> updateMemberImage(@RequestParam(name = "image") MultipartFile img,HttpServletRequest request) throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = memberService.updateImage(img,session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     *  회원비번 변경
     * @return
     */
    @PostMapping("updateMemberPassword")
    public ResponseEntity<HashMap<String, Object>> updateMemberPassword(@RequestBody HashMap<String, String> params,HttpServletRequest request) {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = memberService.updateMemberPassword(params,session);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /**
     *  회원 탈퇴
     * @return
     */
    @PostMapping("memberOut")
    public ResponseEntity<HashMap<String, Object>> memberOut(HttpServletRequest request) {

        HttpSession session = request.getSession();
        HashMap<String, Object> resultMap = memberService.memberOut(session);
        String status = (String) resultMap.get("status");
        if(status.equals("success")) {
            String sessionId = request.getHeader("sessionId"); // 헤더 파싱
            statisticsService.setMemberLogout(sessionId);
        }
        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    public abstract static class BoardController {

        @Autowired
        protected CustomizeBoardArticleService boardArticleService;

        @Autowired
        protected CustomizeMemberNoticeService memberNoticeService;

        @Autowired
        protected CustomizeCommentService commentService;


        /**
         * 게시판 글 임시저장
         * @param params
         * @return
         */
        @PostMapping("activeTempArticle")
        public ResponseEntity<HashMap<String, Object>> insertTempArticle(@RequestBody HashMap<String, Object> params, HttpServletRequest request)  {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.insertTempArticle(params,session);


            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }

        /**
         * 게시판 글 임시저장 (게시글 수정시 임시저장)
         * @param params
         * @return
         */
        @PostMapping("activeTempIsArticle")
        public ResponseEntity<HashMap<String, Object>> insertTempIsArticle(@RequestBody HashMap<String, Object> params, HttpServletRequest request) {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.insertTempIsArticle(params,session);


            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }


        /**
         * 게시판 글 목록
         * @param params
         * @return
         */

        @PostMapping("getArticleList")
        public ResponseEntity<HashMap<String, Object>> getArticleList(@RequestBody HashMap<String, Object> params) throws IOException {

            HashMap<String, Object> resultMap = boardArticleService.getArticleList(params);

            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }

        /**
         * 게시글 정보
         * @param params
         * @return
         */
        @PostMapping("getArticleInfo")
        public ResponseEntity<HashMap<String, Object>> getArticleInfo(@RequestBody HashMap<String, String> params, HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.getArticleInfo(params, session);
            String resultStatus = (String) resultMap.get("status");
            if(resultStatus.equals("success") && session.getAttribute("user_id") != null) {
                Long user_id = (Long) session.getAttribute("user_id");
                String id = params.get("id");
                memberNoticeService.userNoticeView(id, user_id);
                resultMap.put("memberNoticeList",memberNoticeService.getMyMemberNoticeList(user_id));
            }


            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }

        /**
         * 게시판 글 글쓰기
         * @param params
         * @return
         */
        @PostMapping("getArticleRegist")
        public ResponseEntity<HashMap<String, Object>> getArticleRegist(@RequestBody HashMap<String, String> params, HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.getArticleRegist(params, session);

            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }


        /**
         * 게시판 글 저장
         * @param params
         * @return
         */
        @PostMapping("insertArticle")
        public ResponseEntity<HashMap<String, Object>> insertArticle(@RequestBody HashMap<String, Object> params, HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.insertArticle(params,session);

            String resultStatus = (String) resultMap.get("status");
            if(resultStatus.equals("success")) {
                memberNoticeService.insertMemberNoticeWithArticle(resultMap);
            }


            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }


        /**
         * 게시판 글 수정
         * @param params
         * @return
         */
        @PostMapping("updateArticle")
        public ResponseEntity<HashMap<String, Object>> updateArticle(@RequestBody HashMap<String, Object> params, HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.updateArticle(params, session);

            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }
        /**
         * 게시판 글 삭제
         * @param params
         * @return
         */
        @PostMapping("deleteArticle")
        public ResponseEntity<HashMap<String, Object>> deleteArticle(@RequestBody HashMap<String, Object> params, HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.deleteArticle(params, session);
            String resultStatus = (String) resultMap.get("status");
            if(resultStatus.equals("success")) {
                String id = (String) resultMap.get("id");
                commentService.deleteCommentByParentId(id);
                memberNoticeService.deleteMemberNotice(id);

            }
            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }


        /**
         * 게시판 글 비밀번호 체크
         * @param params
         * @return
         */
        @PostMapping("checkArticleUserPass")
        public ResponseEntity<HashMap<String, Object>> checkArticleUserPass(@RequestBody HashMap<String, String> params, HttpServletRequest request) throws IOException {

            HttpSession session = request.getSession();
            HashMap<String, Object> resultMap = boardArticleService.checkArticleUserPass(params,session);
            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }


        /**
         * 게시판 이미지 저장
         * @param imgFile
         * @return
         */
        @PostMapping("insertArticleTempImage")
        public ResponseEntity<HashMap<String, Object>> insertArticleImage(@RequestParam(name = "image") MultipartFile imgFile) throws IOException {


            HashMap<String, Object> resultMap = boardArticleService.insertArticleFile(imgFile,"img");

            return ResponseEntity.status(HttpStatus.OK).body(resultMap);
        }


        /*
         *@ 파일 등록
         */
        @PostMapping("insertArticleTempFile")
        public ResponseEntity<HashMap<String, Object>> insertArticleFile(@RequestParam(name = "dfile") MultipartFile dFile) throws IOException {

            HashMap<String, Object> result = boardArticleService.insertArticleFile(dFile,"file");
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }

        /**
         * 게시판 이미지 가져오기
         * @param imgName
         * @return
         */
        @GetMapping("getArticleImage")
        public  @ResponseBody byte[] getImage(@RequestParam(name = "imgName") String imgName) throws IOException {

            byte[] imageUrl = boardArticleService.getImage(imgName);
            return imageUrl;
        }

        /**
         * 게시판 파일 다운로드
         * @param fileName
         * @return
         */
        @GetMapping("fileDownload")
        public  void fileDownload(@RequestParam(name = "fileName") String fileName,
                                  @RequestParam(name = "articleId") String articleId,
                                  @RequestParam(name = "viewFileName") String viewFileName,
                                  HttpServletResponse response, HttpServletRequest request) throws IOException {
            boardArticleService.fileDownload(fileName,articleId,viewFileName,response,request);
        }


    }
}
