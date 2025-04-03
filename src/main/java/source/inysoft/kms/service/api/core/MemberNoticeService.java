package source.inysoft.kms.service.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Entity.customize.CustomizeMemberNotice;
import source.inysoft.kms.Repository.customize.CustomizeBoardRepository;
import source.inysoft.kms.Repository.customize.CustomizeMemberNoticeRepository;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.Repository.customize.specification.CustomizeMemberNoticeSpecification;
import source.inysoft.kms.Repository.customize.specification.CustomizeMemberSpecification;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public abstract class MemberNoticeService {


    @Autowired
    protected CustomizeMemberRepository memberRepository;


    @Autowired
    protected CustomizeMemberNoticeRepository memberNoticeRepository;

    /*
     *@  나의 알림 목록
     * params :  session
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public Page<CustomizeMemberNotice> getMyMemberNoticeList(Long user_id) {

        CustomizeMember member = memberRepository.getById(user_id);
        String userId = member.getUid();

        Specification<CustomizeMemberNotice> where = (root, query, criteriaBuilder) -> null;
        where = where.and(CustomizeMemberNoticeSpecification.equalUid(userId));
        where = where.and(CustomizeMemberNoticeSpecification.equalView());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime checkDay = now.minusDays(3); // 3일 전
        where = where.and(CustomizeMemberNoticeSpecification.startCreateAt(checkDay));

        String orderByField =  "createAt";
        Sort.Direction sort = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(0,20, sort,orderByField);
        Page<CustomizeMemberNotice> memberNoticeList = memberNoticeRepository.findAll(where,paging);

        return memberNoticeList;
    }


    /*
     *@  나의 알림 전체 목록
     * params :  session
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public Page<CustomizeMemberNotice> getMyMemberNoticeListAll(Long user_id) {

        CustomizeMember member = memberRepository.getById(user_id);
        String userId = member.getUid();

        Specification<CustomizeMemberNotice> where = (root, query, criteriaBuilder) -> null;
        where = where.and(CustomizeMemberNoticeSpecification.equalUid(userId));

        //LocalDateTime now = LocalDateTime.now();
        //LocalDateTime checkDay = now.minusDays(30); // 30일 전
        //where = where.and(CustomizeMemberNoticeSpecification.startCreateAt(checkDay));

        String orderByField =  "createAt";
        Sort.Direction sort = Sort.Direction.DESC;
        Pageable paging = PageRequest.of(0,500, sort,orderByField);
        Page<CustomizeMemberNotice> memberNoticeList = memberNoticeRepository.findAll(where,paging);

        return memberNoticeList;
    }
    /*
     *@ 공지사항 알림 작성
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    @Transactional
    public void insertMemberNoticeWithArticle(HashMap<String, Object> params) {

        HashMap<String, Object> articleInfo = (HashMap<String, Object>) params.get("articleInfo");
        String bid = (String) articleInfo.get("bid");
        String notice = (String) articleInfo.get("notice");
        String userId = (String) articleInfo.get("userId");

        if (userId != null && !userId.isEmpty()) {
            Optional<CustomizeMember> member = memberRepository.findByUid(userId);
            if (member.isPresent()) {
                Boolean insertFlag = false;
                if (bid.equals("notice") && member.get().getRole().equals(Role.ROLE_ADMIN)) {
                    insertFlag = true;
                } else if (notice != null && notice.equals("yes") && member.get().getRole().equals(Role.ROLE_ADMIN)) {
                    insertFlag = true;
                }
                if (insertFlag) {
                    String gid = (String) articleInfo.get("gid");
                    String subject = (String) articleInfo.get("subject");

                    HashMap<String, String> inParams = new HashMap<>();
                    inParams.put("bid", bid);
                    inParams.put("gtype", "article");
                    inParams.put("gid", gid);
                    inParams.put("subject", subject);

                    Role role = Role.ROLE_ADMIN;
                    Specification<CustomizeMember> where = (root, query, criteriaBuilder) -> null;
                    where = where.and(CustomizeMemberSpecification.equalNotRole(role));
                    where = where.and(CustomizeMemberSpecification.equalNotUout("yes"));
                    List<CustomizeMember> memberList = memberRepository.findAll(where);
                    for (CustomizeMember mem : memberList) {

                        inParams.put("uid", mem.getUid());
                        actionMemberNotice(inParams);
                    }

                }
            }
        }

    }

    /*
     *@  댓글 알림 작성
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    @Transactional
    public void insertMemberNoticeWithComment(HashMap<String, Object> params) {

        HashMap<String, Object> articleInfo = (HashMap<String, Object>) params.get("articleInfo");
        HashMap<String, Object> commentInfo = (HashMap<String, Object>) params.get("commentInfo");

        String userNotice = (String) articleInfo.get("userNotice");
        String userId = (String) articleInfo.get("userId");

        if (userId != null && !userId.isEmpty()) {
            Optional<CustomizeMember> member = memberRepository.findByUid(userId);
            if (member.isPresent()) {
                Boolean insertFlag = false;
                if (userNotice.equals("yes")) {

                    String bid = (String) commentInfo.get("bid");
                    String gid = (String) commentInfo.get("gid");
                    String subject = (String) commentInfo.get("subject");
                    String parentId = (String) commentInfo.get("parentId");
                    String userName = (String) commentInfo.get("userName");

                    HashMap<String, String> inParams = new HashMap<>();
                    inParams.put("bid", bid);
                    inParams.put("gtype", "comment");
                    inParams.put("gid", gid);
                    inParams.put("userName",userName);
                    inParams.put("subject", subject);
                    inParams.put("parentId", parentId);
                    inParams.put("uid", userId);
                    actionMemberNotice(inParams);
                }
            }
        }

    }

    // 알림글 저장
    private void actionMemberNotice(HashMap<String, String> params) {
        CustomizeMemberNotice memberNotice = CustomizeMemberNotice.builder()
                .uid(params.get("uid"))
                .bid(params.get("bid"))
                .gtype(params.get("gtype"))
                .gid(params.get("gid"))
                .parentId(params.get("parentId"))
                .view("no")
                .userName(params.get("userName"))
                .subject(params.get("subject"))
                .build();

        memberNoticeRepository.save(memberNotice);
    }


    /*
     *@ 알림 삭제
     * params :
     * return : {status:(message,success,fail)}
     */
    @Transactional
    public void deleteMemberNotice(String id) {

        if (id != null && !id.isEmpty()) {
            memberNoticeRepository.deleteSQLByGid(id);
        }

    }
    /*
     *@ 알림 확인
     * params :
     * return : {status:(message,success,fail)}
     */
    @Transactional
    public void userNoticeView(String gid,Long user_id) {
        CustomizeMember member = memberRepository.getById(user_id);
        List<CustomizeMemberNotice> memberNoticeList = memberNoticeRepository.getSQLByGidAndUserId(gid,member.getUid());
        for(CustomizeMemberNotice item : memberNoticeList) {
            memberNoticeRepository.updateSQLView(item.getId());

        }
    }

}