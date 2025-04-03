package source.inysoft.kms.controllers.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import source.inysoft.kms.service.admin.customize.CustomizeAdminBoardArticleService;
import source.inysoft.kms.service.admin.customize.CustomizeAdminBoardService;
import source.inysoft.kms.service.admin.customize.CustomizeAdminMemberNoticeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;

public class AdminBoardController {

    @Autowired
    protected CustomizeAdminBoardService adminBoardService;

    @Autowired
    protected CustomizeAdminBoardArticleService adminBoardArticleService;

    @Autowired
    protected CustomizeAdminMemberNoticeService adminMemberNoticeService;



    @PostMapping("insertDirectOpenSearch")
    public ResponseEntity<HashMap<String, Object>> insertDirectOpenSearch(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = adminBoardArticleService.insertDirectOpenSearch(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("insertBoard")
    public ResponseEntity<HashMap<String, Object>> insertBoard(@RequestBody HashMap<String, Object> params) {

        HashMap<String, Object> result = adminBoardService.insertBoard(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("updateBoard")
    public ResponseEntity<HashMap<String, Object>> updateBoard(@RequestBody HashMap<String, Object> params) {

        HashMap<String, Object> result = adminBoardService.updateBoard(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("deleteBoard")
    public ResponseEntity<HashMap<String, Object>> deleteBoard(@RequestBody HashMap<String,String> params) throws IOException {

        HashMap<String, Object> result = adminBoardService.deleteBoard(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping("getBoardList")
    public ResponseEntity<HashMap<String, Object>> getBoardListAll() {

        HashMap<String, Object> result = adminBoardService.getBoardListAll();

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    /*
     *@ 게시판 글쓰기 폼
     */
    @PostMapping("getBoardArticleRegist")
    public ResponseEntity<HashMap<String, Object>> getBoardArticleRegist(@RequestBody HashMap<String,String> params, HttpServletRequest request)  throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminBoardArticleService.getBoardArticleRegist(params,session);


        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    /*
     *@ 이미지 등록
     */
    @PostMapping("insertArticleImage")
    public ResponseEntity<HashMap<String, Object>> insertArticleImage(@RequestParam(name = "image") MultipartFile imgFile) throws IOException {

        HashMap<String, Object> result = adminBoardArticleService.insertArticleFile(imgFile,"img");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 파일 등록
     */
    @PostMapping("insertArticleFile")
    public ResponseEntity<HashMap<String, Object>> insertArticleFile(@RequestParam(name = "dfile") MultipartFile dFile) throws IOException {

        HashMap<String, Object> result = adminBoardArticleService.insertArticleFile(dFile,"file");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 게시판 글 등록
     */
    @PostMapping("insertBoardArticle")
    public ResponseEntity<HashMap<String, Object>> insertBoardArticle(@RequestBody HashMap<String,Object> params, HttpServletRequest request)  throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminBoardArticleService.insertBoardArticle(params,session);
        String resultStatus = (String) result.get("status");
        if(resultStatus.equals("success")) {
            adminMemberNoticeService.insertMemberNoticeWithArticle(result);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 게시판 글 수정
     */
    @PostMapping("updateBoardArticle")
    public ResponseEntity<HashMap<String, Object>> updateBoardArticle(@RequestBody HashMap<String,Object> params, HttpServletRequest request)  throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminBoardArticleService.updateBoardArticle(params,session);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 게시판 글 삭제
     */
    @PostMapping("deleteBoardArticle")
    public ResponseEntity<HashMap<String, Object>> deleteBoardArticle(@RequestBody HashMap<String,Object> params)  throws IOException {

        HashMap<String, Object> result = adminBoardArticleService.deleteBoardArticle(params);
        String resultStatus = (String) result.get("status");
        if(resultStatus.equals("success")) {
            adminMemberNoticeService.deleteMemberNotice(params);
        }
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 게시판 글 (게시판목록포함) 목록
     * return
     */
    @PostMapping("getBoardArticleListWithBoard")
    public ResponseEntity<HashMap<String, Object>> getBoardArticleListWithBoard(@RequestBody HashMap<String,Object> params)  throws IOException {

        HashMap<String, Object> result = adminBoardArticleService.getBoardArticleListWithBoard(params);

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }


    /*
     *@ 게시판 글  목록
     * return
     */
    @PostMapping("getBoardArticleList")
    public ResponseEntity<HashMap<String, Object>> getBoardArticleList(@RequestBody HashMap<String,Object> params)  throws IOException {


        HashMap<String, Object> result = adminBoardArticleService.getBoardArticleList(params);
        result.put("status", "success");
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }



    /*
     *@ 게시판 글  임시저장
     */
    @PostMapping("insertTempArticle")
    public ResponseEntity<HashMap<String, Object>> insertTempArticle(@RequestBody HashMap<String,Object> params, HttpServletRequest request)  throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminBoardArticleService.insertTempArticle(params,session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    /*
     *@ 게시판 글  임시저장 (게시글 수정시에 임시저장)
     */
    @PostMapping("insertTempIsArticle")
    public ResponseEntity<HashMap<String, Object>> insertTempIsArticle(@RequestBody HashMap<String,Object> params, HttpServletRequest request)  throws IOException {

        HttpSession session = request.getSession();
        HashMap<String, Object> result = adminBoardArticleService.insertTempIsArticle(params,session);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

}
