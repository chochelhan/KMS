package source.inysoft.kms.service.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.*;
import source.inysoft.kms.Repository.customize.*;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;
import source.inysoft.kms.opensearch.customize.CustomizeCommentSearch;

import javax.persistence.Column;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public abstract class BoardArticleService {

    @Autowired
    protected CustomizeBoardArticleSearch boardArticleSearch;

    @Autowired
    protected CustomizeBoardRepository boardRepository;

    @Autowired
    protected CustomizeMemberRepository memberRepository;

    @Autowired
    CustomizeTempArticleRepository tempArticleRepository;

    @Autowired
    protected CustomizeMemberNoticeRepository memberNoticeRepository;

    @Autowired
    protected CustomizeStatisticsDownRepository statisticsDownRepository;



    protected String WAUTH = "all";

    public String imagePath = "fileUpload/board/image";
    public String filePath = "fileUpload/board/file";

    /*
     *@ 게시글 목록
     * params :  id
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getArticleList(HashMap<String, Object> params) throws IOException {


        HashMap<String, Object> result = new HashMap<>();
        String bid = (String) params.get("bid");
        if (bid == null || bid.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        CustomizeBoard board = boardRepository.getFindByBid(bid);


        HashMap<String, Object> noticeData = boardArticleSearch.noticeSearch(params);
        params.put("notice", "no");
        HashMap<String, Object> articleData = boardArticleSearch.search(params);

        result.put("status", "success");
        result.put("noticeData", noticeData);
        result.put("articleData", articleData);
        result.put("board", board);
        return result;
    }

    /*
     *@ 메인 게시글 목록
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getMainArticleList() throws IOException {


        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> bidParams = new HashMap<>();
        bidParams.put("bid", "notice");
        bidParams.put("open", "yes");
        bidParams.put("limit", "7");
        HashMap<String, Object> noticeData = boardArticleSearch.search(bidParams);
        result.put("noticeData", noticeData);

        bidParams.put("bid", "free");
        HashMap<String, Object> freeData = boardArticleSearch.search(bidParams);
        result.put("freeData", freeData);

        bidParams.put("bid", "qna");
        HashMap<String, Object> qnaData = boardArticleSearch.search(bidParams);
        result.put("qnaData", qnaData);

        bidParams.put("bid", "ues");
        HashMap<String, Object> uesData = boardArticleSearch.search(bidParams);
        result.put("uesData", uesData);

        return result;
    }

    /*
     *@  나의 게시글 목록
     * params :  session
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getMyArticleList(HashMap<String, Object> params, HttpSession session) throws IOException {

        Long user_id = (Long) session.getAttribute("user_id");
        CustomizeMember member = memberRepository.getById(user_id);
        params.put("userId", member.getUid());
        return boardArticleSearch.search(params);
    }

    /*
     *@ 게시글 정보
     * params :  id
     * return : {status:(message,success,fail),info:}
     */
    public HashMap<String, Object> getArticleInfo(HashMap<String, String> params, HttpSession session) throws IOException {


        HashMap<String, Object> result = new HashMap<String, Object>();
        String id = params.get("id");
        if (id != null && !id.isEmpty()) {
            HashMap<String, Object> data = boardArticleSearch.getData(id);
            if (data.get("data") != null) {
                HashMap<String, Object> Info = (HashMap<String, Object>) data.get("data");
                String open = (String) Info.get("open");
                if (open.equals("no")) {
                    String sessPass = (String) params.get("sessPass");
                    if (!checkUserAuth(data, sessPass, session)) {
                        result.put("status", "fail");
                        return result;
                    }
                }
                result.put("info", Info);

                CustomizeBoard board = boardRepository.getFindByBid((String) Info.get("bid"));
                result.put("board", board);
                boardArticleSearch.updateWithHitOrCmt(id, "hit");
            } else {
                result.put("status", "fail");
                return result;
            }

        } else {
            result.put("status", "fail");
            return result;
        }


        result.put("status", "success");


        return result;
    }

    /*
     *@ 게시글 작성 폼
     * params :  bid,id
     * return : {status:(message,success,fail),info:}
     */
    public HashMap<String, Object> getArticleRegist(HashMap<String, String> params, HttpSession session) throws IOException {


        HashMap<String, Object> result = new HashMap<String, Object>();

        String bid = params.get("bid");
        if (bid == null || bid.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        CustomizeBoard board = boardRepository.getFindByBid(bid);
        String id = params.get("id");

        if (id != null && !id.isEmpty()) {
            HashMap<String, Object> data = boardArticleSearch.getData(id);
            result.put("info", data.get("data"));
        }

        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            String uid = member.getUid();
            if (id != null && !id.isEmpty()) {
                CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempTypeAndArticleId(uid, "update",id);
                result.put("tempArticle", tempArticle);
            } else {
                CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempType(uid, "insert");
                result.put("tempArticle", tempArticle);
            }
        }
        result.put("status", "success");
        result.put("board", board);

        return result;
    }

    /*
     *@ 게시글 작성
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> insertArticle(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        HashMap<String, String> checkParams = checkAuthValidate(params);

        if (checkParams.get("auth").equals("fail")) {
            result.put("status", "fail");
            return result;
        }
        String userId = "";
        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            String userName = member.getName();
            if (WAUTH.equals("admin")) {
                if (!member.getRole().equals(Role.ROLE_ADMIN)) {
                    result.put("status", "fail");
                    return result;
                }
            }


            userId = member.getUid();
            checkParams.put("userName", userName);
            checkParams.put("userId", userId);
        }
        switch (WAUTH) {
            case "admin":
            case "user":
                if (userId.isEmpty()) {
                    result.put("status", "fail");
                    return result;
                }
                break;
            case "all":
                if (userId.isEmpty()) {
                    String userName = (String) params.get("userName");
                    String userPassword = (String) params.get("userPassword");
                    if (userName == null || userName.isEmpty() || userPassword == null || userPassword.isEmpty()) {
                        result.put("status", "fail");
                        return result;
                    }
                    checkParams.put("userName", userName);
                    checkParams.put("userPassword", userPassword);
                }
                break;

        }
        if (!userId.isEmpty()) {
            CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempType(userId, "insert");
            if (tempArticle != null) {
                if (tempArticle.getUid() != null) {
                    tempArticleRepository.deleteById(tempArticle.getId());
                }
            }
        }

        result.put("articleInfo", boardArticleSearch.insert(checkParams));
        result.put("status", "success");
        return result;
    }

    /*
     *@ 게시글 수정
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> updateArticle(HashMap<String, Object> params, HttpSession session) throws IOException {
        HashMap<String, Object> result = new HashMap<String, Object>();

        String id = (String) params.get("id");
        if (id == null || id.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        HashMap<String, Object> isData = boardArticleSearch.getData(id);
        String sessPass = (String) params.get("sessPass");
        if (!checkUserAuth(isData, sessPass, session)) {
            result.put("status", "fail");
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

        boardArticleSearch.update(checkParams, id);
        if (session.getAttribute("user_id") != null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            String uid = member.getUid();
            CustomizeTempArticle tempArticle = tempArticleRepository.getFindByUidAndTempTypeAndArticleId(uid, "update", id);
            if (tempArticle != null) {
                if (tempArticle.getUid() != null) {
                    tempArticleRepository.deleteById(tempArticle.getId());
                }
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
        //String secret = "no";
        if (subject == null || subject.isEmpty() || content == null || content.isEmpty()) {
            result.put("auth", "fail");
            return result;
        }
        result.put("bid", bid);
        result.put("subject", subject);
        result.put("content", content);
        String notice = (String) params.get("notice");
        if (notice == null || notice.isEmpty()) {
            notice = "no";
        }
        result.put("notice", notice);

        String replyUse = (String) params.get("replyUse");
        String userNotice = (String) params.get("userNotice");
        String open = (String) params.get("open");

        result.put("replyUse", replyUse);
        result.put("userNotice", userNotice);
        result.put("open", open);


        result.put("dfileNames", (String) params.get("dfileNameString"));
        result.put("dfileOrgNames", (String) params.get("orgDfileNameString"));
        result.put("keywords", (String) params.get("keywords"));
        result.put("imgs", (String) params.get("imgString"));
        result.put("orgImgNames", (String) params.get("orgImgString"));
        result.put("searchDfileNames", (String) params.get("searchDfileNameString"));
        result.put("searchImgNames", (String) params.get("searchImgNameString"));

        result.put("auth", "success");
        WAUTH = board.getWauth();
        return result;
    }

    /*
     *@ 게시글 삭제
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> deleteArticle(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String id = (String) params.get("id");
        if (id == null || id.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        HashMap<String, Object> isData = boardArticleSearch.getData(id);
        String sessPass = (String) params.get("sessPass");
        if (!checkUserAuth(isData, sessPass, session)) {
            result.put("status", "fail");
            return result;
        }
        boardArticleSearch.delete(id);
        result.put("status", "success");
        result.put("id", id);
        return result;
    }

    /**
     * @ 작성자 동일인 여부
     **/
    private Boolean checkUserAuth(HashMap<String, Object> isData, String sessPass, HttpSession session) throws IOException {
        if (isData.get("data") != null) {
            CustomizeMember member = new CustomizeMember();
            if (session.getAttribute("user_id") != null) {
                Long user_id = (Long) session.getAttribute("user_id");
                member = memberRepository.getById(user_id);
                if (member.getRole().equals(Role.ROLE_ADMIN)) {
                    return true;
                }
            }

            HashMap<String, Object> isInfo = (HashMap<String, Object>) isData.get("data");
            String userId = (String) isInfo.get("userId");
            if (userId != null && !userId.isEmpty()) {
                if (session.getAttribute("user_id") != null) {
                    if (!userId.equals(member.getUid())) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (sessPass == null || sessPass.isEmpty()) {
                    return false;
                }
                if (session.getAttribute("article_pass") != null) {
                    int articlePass = (int) session.getAttribute("article_pass");
                    String articlePassString = String.valueOf(articlePass);
                    if (!sessPass.equals(articlePassString)) {
                        return false;
                    }

                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

    /*
     *@ 게시글 비밀번호 체크
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> checkArticleUserPass(HashMap<String, String> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String id = params.get("id");
        String userPass = params.get("userPass");
        if (id == null || id.isEmpty() || userPass == null || userPass.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        HashMap<String, Object> isData = boardArticleSearch.getData(id);
        if (isData.get("data") != null) {
            HashMap<String, Object> isInfo = (HashMap<String, Object>) isData.get("data");
            String userPassword = (String) isInfo.get("userPassword");
            if (userPass.equals(userPassword)) {
                Random random = new Random();
                int sessPass = random.nextInt();
                session.setAttribute("article_pass", sessPass);
                result.put("sessPass", sessPass);
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
     *@ 게시글 파일 저장
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
     *@ 게시글 이미지 가져오기
     * params :
     * return :
     */
    public byte[] getImage(String imgName) throws IOException {

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

    /*
     *@ 게시글 파일 다운로드
     * params :
     * return :
     */
    public void fileDownload(String fileName,String articleId,String viewFileName, HttpServletResponse response, HttpServletRequest request) throws IOException {


        HashMap<String, Object> data = boardArticleSearch.getData(articleId);
        if (data.get("data") != null) {
            HashMap<String, Object> Info = (HashMap<String, Object>) data.get("data");
            if(Info==null)return;

            FileInputStream fis = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Path currentPath = Paths.get("");
            String sitePath = currentPath.toAbsolutePath().toString();
            String absolutePath = new File(sitePath).getAbsolutePath() + "/"; // 파일이 저장될 절대 경로

            try {
                fis = new FileInputStream(absolutePath + filePath + "/" + fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int readCount = 0;
            byte[] buffer = new byte[1024];
            byte[] fileArray = null;
            try {
                File file = new File(absolutePath + filePath + "/" + fileName);
                response.setContentType("application/octet-stream; charset=utf-8");
                response.setContentLength((int) file.length());
                String browser = getBrowser(request);
                String disposition = getDisposition(viewFileName, browser);
                response.setHeader("Content-Disposition", disposition);
                response.setHeader("Content-Transfer-Encoding", "binary");
                OutputStream out = response.getOutputStream();
                FileCopyUtils.copy(fis, out);
                if (fis != null)
                    fis.close();
                out.flush();
                out.close();

                String bid = (String) Info.get("bid");

                CustomizeStatisticsDown statisticsDown = CustomizeStatisticsDown.builder()
                        .bid(bid)
                        .articleId(articleId)
                        .fileName(fileName)
                        .orgFileName(viewFileName)
                        .build();

                statisticsDownRepository.save(statisticsDown);

            } catch (IOException e) {
                throw new RuntimeException("File Error");
            }
        }
    }

    private String getBrowser(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        if (header.indexOf("MSIE") > -1 || header.indexOf("Trident") > -1)
            return "MSIE";
        else if (header.indexOf("Chrome") > -1)
            return "Chrome";
        else if (header.indexOf("Opera") > -1)
            return "Opera";
        return "Firefox";
    }

    private String getDisposition(String filename, String browser) throws UnsupportedEncodingException {
        String dispositionPrefix = "attachment;filename=";
        String encodedFilename = getDecodeFileName(filename,browser);
        return dispositionPrefix + encodedFilename;
    }
    private String getDecodeFileName(String filename, String browser) throws UnsupportedEncodingException {
        String encodedFilename = null;
        if (browser.equals("MSIE")) {
            encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll(
                    "\\+", "%20");
        } else if (browser.equals("Firefox")) {
            encodedFilename = "\""
                    + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
        } else if (browser.equals("Opera")) {
            encodedFilename = "\""
                    + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
        } else if (browser.equals("Chrome")) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < filename.length(); i++) {
                char c = filename.charAt(i);
                if (c > '~') {
                    sb.append(URLEncoder.encode("" + c, "UTF-8"));
                } else {
                    sb.append(c);
                }
            }
            encodedFilename = sb.toString();
        }
        return encodedFilename;
    }




    /*
     *@ 게시글 임시작성
     * params :
     * return : {}
     */

    public HashMap<String, Object> insertTempArticle(HashMap<String, Object> params, HttpSession session) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        if (session.getAttribute("user_id") != null) {
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

        } else {
            result.put("status", "fail");
        }

        return result;
    }

    /*
     *@ 게시글 임시작성 (기존 게시글 수정시에 )
     * params :
     * return : {}
     */

    public HashMap<String, Object> insertTempIsArticle(HashMap<String, Object> params, HttpSession session)  {

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
        if (session.getAttribute("user_id") == null) {
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