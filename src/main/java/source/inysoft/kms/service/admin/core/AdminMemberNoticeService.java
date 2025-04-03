package source.inysoft.kms.service.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Entity.customize.CustomizeMemberNotice;
import source.inysoft.kms.Repository.customize.CustomizeMemberNoticeRepository;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.Repository.customize.specification.CustomizeMemberNoticeSpecification;
import source.inysoft.kms.Repository.customize.specification.CustomizeMemberSpecification;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public abstract class AdminMemberNoticeService {


    @Autowired
    protected CustomizeMemberRepository memberRepository;


    @Autowired
    protected CustomizeMemberNoticeRepository memberNoticeRepository;

    /*
     *@ 알림 작성
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    @Transactional
    public void insertMemberNoticeWithArticle(HashMap<String, Object> params) {

        HashMap<String, Object> articleInfo = (HashMap<String, Object>) params.get("articleInfo");
        String bid = (String) articleInfo.get("bid");
        String notice = (String) articleInfo.get("notice");
        String userId = (String) articleInfo.get("userId");

        Boolean insertFlag = false;
        if (bid.equals("notice")) {
            insertFlag = true;
        } else if (notice != null && notice.equals("yes")) {
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

    // 알림글 저장
    private void actionMemberNotice(HashMap<String, String> params) {
        CustomizeMemberNotice memberNotice = CustomizeMemberNotice.builder()
                .uid(params.get("uid"))
                .bid(params.get("bid"))
                .gtype(params.get("gtype"))
                .gid(params.get("gid"))
                .parentId(params.get("parentId"))
                .view("no")
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
    public void deleteMemberNotice(HashMap<String, Object> params) {

        List<String> ids = (List<String>) params.get("ids");

        if (ids.size() > 0) {
            for (String id : ids) {
                memberNoticeRepository.deleteSQLByGid(id);
            }
        }
    }


}