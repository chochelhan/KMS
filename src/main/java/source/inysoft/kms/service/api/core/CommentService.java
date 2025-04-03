package source.inysoft.kms.service.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;
import source.inysoft.kms.opensearch.customize.CustomizeCommentSearch;

import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;

public abstract class CommentService {

    @Autowired
    protected CustomizeCommentSearch commentSearch;

    @Autowired
    protected CustomizeBoardArticleSearch boardArticleSearch;

    @Autowired
    protected CustomizeMemberRepository memberRepository;

    protected int isInfoDepth;
    protected String isParentId;

    /*
     *@ 댓글 목록
     * params :  id
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getCommentList(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<>();
        String parentType = (String) params.get("parentType");
        String parentId = (String) params.get("parentId");
        if (parentType == null || parentType.isEmpty() || parentId == null || parentId.isEmpty()) {
            result.put("status", "fail");
            return result;
        }

        HashMap<String, Object> commentData = commentSearch.search(params);

        result.put("status", "success");
        result.put("commentData", commentData);
        return result;
    }

    /*
     *@ 메인 댓글 목록
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getMainCommentList() throws IOException {

        HashMap<String, Object> commentParams = new HashMap<>();
        /*
        List<String> searchBids = new ArrayList<String>();
        searchBids.add("free");
        searchBids.add("notice");
        searchBids.add("ues");
        searchBids.add("qna");
*/
        commentParams.put("depth",1);
        commentParams.put("limit","7");
        commentParams.put("orderByField","createAt");
        //commentParams.put("searchBids",searchBids);

        return commentSearch.allSearch(commentParams);
    }
    /*
     *@ 댓글 작성
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> insertComment(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();

        HashMap<String, Object> checkParams = checkAuthValidate(params);
        if (checkParams.get("auth").equals("fail")) {
            result.put("status", "fail");
            return result;
        }
        if(session.getAttribute("user_id")!=null) {
            Long user_id = (Long) session.getAttribute("user_id");
            CustomizeMember member = memberRepository.getById(user_id);
            String userName = member.getName();
            checkParams.put("userName", userName);
            checkParams.put("userId", member.getUid());
        } else {
            result.put("status", "fail");
            return result;
        }
        String pid = (String) params.get("pid");
        if (pid != null && !pid.isEmpty()) {
            checkParams.put("pid", pid);
        } else {
            HashMap<String, Object> articleData = boardArticleSearch.getData((String) checkParams.get("parentId"));
            HashMap<String, Object> articleInfo = (HashMap<String, Object>) articleData.get("data");
            String subject = (String) articleInfo.get("subject");
            if(subject==null || subject.isEmpty()) {
                result.put("status", "fail");
                return result;
            }
            result.put("articleInfo", articleInfo);
            boardArticleSearch.updateWithHitOrCmt((String) checkParams.get("parentId"),"cmtPlus");
        }

        result.put("commentInfo", commentSearch.insert(checkParams));
        result.put("status", "success");
        return result;
    }


    /*
     *@ 댓글 수정
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> updateComment(HashMap<String, Object> params, HttpSession session) throws IOException {
        HashMap<String, Object> result = new HashMap<String, Object>();

        String id = (String) params.get("id");
        if (id == null || id.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        /// 작성자 동일인 여부
        if(!checkAuth(id,session)) {
            result.put("status", "fail");
            return result;
        }

        HashMap<String, Object> checkParams = checkAuthValidate(params);
        if (checkParams.get("auth").equals("fail")) {
            result.put("status", "fail");
            return result;
        }
        commentSearch.update(checkParams,id);
        result.put("status", "success");
        return result;
    }


    /*
     *@  필수입력사항 체크
     * params :
     * return : {status:(success,fail)}
     */
    private HashMap<String, Object> checkAuthValidate(HashMap<String, Object> params) {
        HashMap<String, Object> result = new HashMap<>();

        String parentId = (String) params.get("parentId");
        String parentType = (String) params.get("parentType");
        String content = (String) params.get("content");
        int depth = (int) params.get("depth");
        String bid = (String) params.get("bid");

        if (parentId == null || parentId.isEmpty() || parentType==null || parentType.isEmpty() || content==null || content.isEmpty() || bid==null || bid.isEmpty()) {
            result.put("auth", "fail");
            return result;
        }
        result.put("auth", "success");
        result.put("parentId",parentId);
        result.put("parentType",parentType);
        result.put("depth",depth);
        result.put("bid",bid);
        result.put("content",content);

        return result;
    }
    /*
     *@ 댓글 삭제
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> deleteComment(HashMap<String, Object> params, HttpSession session) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String id = (String) params.get("id");
        if (id == null || id.isEmpty()) {
            result.put("status", "fail");
            return result;
        }
        /// 작성자 동일인 여부
        if(!checkAuth(id,session)) {
            result.put("status", "fail");
            return result;
        }
        if(isInfoDepth<2 && (isParentId!=null && !isParentId.isEmpty())) {
            boardArticleSearch.updateWithHitOrCmt(isParentId,"cmtMinus");
        }
        commentSearch.delete(id);
        result.put("status", "success");
        result.put("id", id);
        return result;
    }

    /*
     *@ 댓글 삭제 (by ParentId)
     * params :
     * return : {status:(message,success,fail)}
     */
    public void deleteCommentByParentId(String parentId) throws IOException {

        commentSearch.deleteCommentListByParentId(parentId);

    }
    /*
     *@ 작성자와 동일인 체크
     * params :
     * return : {status:(message,success,fail)}
     */
    private boolean checkAuth(String id,HttpSession session) throws IOException {

        HashMap<String, Object> isData  = commentSearch.getData(id);
        if(isData.get("data")!=null) {
            HashMap<String, Object> isInfo = (HashMap<String, Object>) isData.get("data");
            isInfoDepth = (int) isInfo.get("depth");
            isParentId = (String) isInfo.get("parentId");
            String userId = (String) isInfo.get("userId");
            if(userId != null && !userId.isEmpty()) {
                if(session.getAttribute("user_id")!=null) {
                    Long user_id = (Long) session.getAttribute("user_id");
                    CustomizeMember member = memberRepository.getById(user_id);
                    if (!userId.equals(member.getUid())) {
                        if (!member.getRole().equals(Role.ROLE_ADMIN)) {
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;

            }
        } else {
            return false;
        }
        return true;
    }

}
