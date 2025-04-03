package source.inysoft.kms.service.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Entity.customize.CustomizeTempArticle;
import source.inysoft.kms.Repository.customize.CustomizeBoardRepository;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.Repository.customize.CustomizeTempArticleRepository;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;
import source.inysoft.kms.opensearch.customize.CustomizeCommentSearch;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AdminBoardArticleService {

    @Autowired
    protected CustomizeBoardArticleSearch customizeBoardArticleSearch;

    @Autowired
    CustomizeBoardRepository boardRepository;


    @Autowired
    CustomizeMemberRepository memberRepository;

    @Autowired
    CustomizeTempArticleRepository tempArticleRepository;

    @Autowired
    protected CustomizeCommentSearch commentSearch;


    public String imagePath = "fileUpload/board/image";
    public String filePath = "fileUpload/board/file";


    public HashMap<String, Object> insertDirectOpenSearch(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        HashMap<String, String> checkParams = new HashMap<String, String>();
        List<HashMap<String, String>> dataList = (List<HashMap<String, String>>) params.get("list");
        for (HashMap<String, String> data : dataList) {
            String userName = "관리자";
            String userId = "admin";
            data.put("userName", userName);
            data.put("userId", userId);
            customizeBoardArticleSearch.insert(data);

        }

        result.put("status", "success");
        return result;
    }
    /*
     *@ 게시글 작성
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */

    public HashMap<String, Object> insertBoardArticle(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        HashMap<String, String> checkParams = checkAuthValidate(params);

        if (checkParams.get("auth").equals("fail")) {
            result.put("status", "fail");
            return result;
        }

        Long user_id = (Long) session.getAttribute("user_id");
        CustomizeMember member = memberRepository.getById(user_id);

        String userName = member.getName();
        String userId = member.getUid();
        checkParams.put("userName", userName);
        checkParams.put("userId", userId);


        CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempType(userId, "insert");
        if (tempArticle != null) {
            if (tempArticle.getUid() != null) {
                tempArticleRepository.deleteById(tempArticle.getId());
            }
        }

        result.put("articleInfo", customizeBoardArticleSearch.insert(checkParams));
        result.put("status", "success");
        return result;
    }


    /*
     *@ 게시글 수정
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    @Transactional
    public HashMap<String, Object> updateBoardArticle(HashMap<String, Object> params, HttpSession session) throws IOException {
        HashMap<String, Object> result = new HashMap<String, Object>();

        String id = (String) params.get("id");
        if (id == null || id.isEmpty()) {
            result.put("auth", "fail");
            return result;
        }
        HashMap<String, String> checkParams = checkAuthValidate(params);

        if (checkParams.get("auth").equals("fail")) {
            result.put("status", "fail");
            return result;
        }
        String userName = (String) params.get("userName");
        String userPassword = (String) params.get("userPassword");
        if (userName != null && !userName.isEmpty() && userPassword != null && !userPassword.isEmpty()) {
            checkParams.put("userName", userName);
            checkParams.put("userPassword", userPassword);
        }
        customizeBoardArticleSearch.update(checkParams, id);

        Long user_id = (Long) session.getAttribute("user_id");
        CustomizeMember member = memberRepository.getById(user_id);
        String uid = member.getUid();
        CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempTypeAndArticleId(uid, "update", id);
        if (tempArticle != null) {
            if (tempArticle.getUid() != null) {
                tempArticleRepository.deleteById(tempArticle.getId());
            }
        }

        result.put("status", "success");
        return result;
    }


    /*
     *@  권한 및 필수입력사항 체크
     * params :
     * return : {status:(success,fail)}
     */
    private HashMap<String, String> checkAuthValidate(HashMap<String, Object> params) {
        HashMap<String, String> result = new HashMap<String, String>();

        String bid = (String) params.get("bid");
        if (bid == null || bid.isEmpty()) {
            result.put("auth", "fail");
            return result;
        }
        CustomizeBoard board = boardRepository.getFindByBid(bid);
        if (board == null) {
            result.put("auth", "fail");
            return result;
        }

        if (board.getCategoryUse().equals("yes")) {
            String category = (String) params.get("category");
            if (category == null || category.isEmpty()) {
                result.put("auth", "fail");
                return result;
            }
            result.put("category", category);
        }
        String subject = (String) params.get("subject");
        String content = (String) params.get("content");
        String secret = "no";
        if (subject == null || subject.isEmpty() || content == null || content.isEmpty()) {
            result.put("auth", "fail");
            return result;
        }
        result.put("bid", bid);
        result.put("subject", subject);
        result.put("content", content);
        String replyUse = (String) params.get("replyUse");
        String userNotice = (String) params.get("userNotice");
        String open = (String) params.get("open");
        String notice = (String) params.get("notice");

        result.put("replyUse", replyUse);
        result.put("userNotice", userNotice);
        result.put("open", open);
        result.put("notice", notice);

        result.put("dfileNames", (String) params.get("dfileNameString"));
        result.put("dfileOrgNames", (String) params.get("orgDfileNameString"));
        result.put("searchDfileNames", (String) params.get("searchDfileNameString"));
        result.put("keywords", (String) params.get("keywords"));
        result.put("imgs", (String) params.get("imgString"));
        result.put("orgImgNames", (String) params.get("orgImgString"));
        result.put("searchImgNames", (String) params.get("searchImgNameString"));


        result.put("auth", "success");

        return result;
    }

    /*
     *@ 게시글 삭제
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> deleteBoardArticle(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        List<String> ids = (List<String>) params.get("ids");

        if (ids.size() < 1) {
            result.put("status", "fail");
            return result;
        }
        for (String id : ids) {
            customizeBoardArticleSearch.delete(id);
            commentSearch.deleteCommentListByParentId(id);
        }

        result.put("status", "success");
        return result;
    }

    /*
     *@ 게시글 작성 폼
     * params :  id
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getBoardArticleRegist(HashMap<String, String> params, HttpSession session) throws IOException {

        List<CustomizeBoard> boardList = boardRepository.getFindByBuse("yes");
        HashMap<String, Object> result = new HashMap<String, Object>();

        Long user_id = (Long) session.getAttribute("user_id");
        CustomizeMember member = memberRepository.getById(user_id);

        String uid = member.getUid();
        if (!params.get("id").isEmpty()) {
            HashMap<String, Object> data = customizeBoardArticleSearch.getData(params.get("id"));
            CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempTypeAndArticleId(uid, "update",params.get("id"));
            result.put("tempArticle",tempArticle);
            result.put("info", data.get("data"));
        } else {
            CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempType(uid, "insert");
            result.put("tempArticle",tempArticle);
        }
        result.put("status", "success");
        result.put("boardList", boardList);

        return result;
    }


    /*
     *@ 게시글 목록 (게시판 목록 포함)
     * params :  id
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getBoardArticleListWithBoard(HashMap<String, Object> params) throws IOException {

        List<CustomizeBoard> boardList = boardRepository.findAll();
        HashMap<String, Object> articleData = customizeBoardArticleSearch.search(params);

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("status", "success");
        result.put("boardList", boardList);
        result.put("articleData", articleData);

        return result;
    }

    /*
     *@ 게시글 목록
     * params :  id
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getBoardArticleList(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> articleData = customizeBoardArticleSearch.search(params);
        HashMap<String, Object> result = new HashMap<String, Object>();

        result.put("status", "success");
        result.put("articleData", articleData);
        return result;
    }


    /*
     *@ 게시글 이미지 / 첨부파일 저장
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> insertArticleFile(MultipartFile dFile, String type) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        Path currentPath = Paths.get("");
        String sitePath = currentPath.toAbsolutePath().toString();

        Date now = new Date();
        Long nowTime = now.getTime();

        String absolutePath = new File(sitePath).getAbsolutePath() + "/"; // 파일이 저장될 절대 경로
        String newFileName = "dFile_" + nowTime; // 새로 부여한 이미지명
        String fileDir = filePath;
        if (type.equals("img")) {
            newFileName = "image_" + nowTime;
            fileDir = imagePath;
        }
        String fileExtension = '.' + dFile.getOriginalFilename().replaceAll("^.*\\.(.*)$", "$1"); // 정규식 이용하여 확장자만 추출

        try {
            if (!dFile.isEmpty()) {
                File file = new File(absolutePath + fileDir);
                if (!file.exists()) {
                    file.mkdirs(); // mkdir()과 다르게 상위 폴더가 없을 때 상위폴더까지 생성
                }

                File saveFile = new File(absolutePath + fileDir + "/" + newFileName + fileExtension);
                dFile.transferTo(saveFile);

                result.put("orgFileName", dFile.getOriginalFilename());
                result.put("ext", fileExtension);
                result.put("saveFileUrl", newFileName + fileExtension);
                result.put("status", "success");
            } else {
                result.put("status", "fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return result;
    }

    /*
     *@ 게시글 임시작성
     * params :
     * return : {}
     */

    public HashMap<String, Object> insertTempArticle(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        Long user_id = (Long) session.getAttribute("user_id");
        CustomizeMember member = memberRepository.getById(user_id);

        String uid = member.getUid();
        CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempType(uid, "insert");
        if (tempArticle != null) {
            if (tempArticle.getUid() != null) {
                tempArticleRepository.deleteById(tempArticle.getId());
            }
        }
        params.put("uid", uid);
        params.put("tempType", "insert");
        insetTempActive(params);
        result.put("status", "success");
        return result;
    }

    /*
     *@ 게시글 임시작성 (기존 게시글 수정시에 )
     * params :
     * return : {}
     */

    public HashMap<String, Object> insertTempIsArticle(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String id = (String) params.get("id");
        String bid = (String) params.get("bid");
        if (id == null || id.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        if (bid == null || bid.isEmpty()) {
            result.put("status", "fail");
            return result;
        }

        Long user_id = (Long) session.getAttribute("user_id");
        CustomizeMember member = memberRepository.getById(user_id);

        String uid = member.getUid();
        CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempTypeAndArticleId(uid, "update", id);
        if (tempArticle != null) {
            if (tempArticle.getUid() != null) {
                tempArticleRepository.deleteById(tempArticle.getId());
            }
        }
        params.put("uid", uid);
        params.put("tempType", "update");

        insetTempActive(params);
        result.put("status", "success");
        return result;
    }

    private void insetTempActive(HashMap<String, Object> params) {
        HashMap<String, String> result = new HashMap<String, String>();

        String articleId = (String) params.get("id");
        String bid = (String) params.get("bid");
        String category = (String) params.get("category");
        String subject = (String) params.get("subject");
        String content = (String) params.get("content");

        String replyUse = (String) params.get("replyUse");
        String userNotice = (String) params.get("userNotice");
        String open = (String) params.get("open");
        String notice = (String) params.get("notice");


        String dfileNames = (String) params.get("dfileNameString");
        String dfileOrgNames = (String) params.get("orgDfileNameString");
        String searchDfileNames = (String) params.get("searchDfileNameString");
        String keywords = (String) params.get("keywords");
        String imgs = (String) params.get("imgString");
        String orgImgNames = (String) params.get("orgImgString");
        String searchImgNames = (String) params.get("searchImgNameString");

        String userName = (String) params.get("userName");
        String userPassword = (String) params.get("userPassword");

        String uid = (String) params.get("uid");
        String tempType = (String) params.get("tempType");

        CustomizeTempArticle tempArticle = CustomizeTempArticle.builder()
                .bid(bid)
                .articleId(articleId)
                .uid(uid)
                .tempType(tempType)
                .userName(userName)
                .userPassword(userPassword)
                .category(category)
                .subject(subject)
                .content(content)
                .dfileNames(dfileNames)
                .searchDfileNames(searchDfileNames)
                .dfileOrgNames(dfileOrgNames)
                .keywords(keywords)
                .imgs(imgs)
                .searchImgNames(searchImgNames)
                .orgImgNames(orgImgNames)
                .replyUse(replyUse)
                .open(open)
                .userNotice(userNotice)
                .notice(notice)
                .build();

        CustomizeTempArticle resultData = tempArticleRepository.save(tempArticle);


    }
}
