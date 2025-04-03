package source.inysoft.kms.service.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Entity.customize.CustomizeStatisticsDown;
import source.inysoft.kms.Entity.customize.CustomizeStatisticsMember;

import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;
import source.inysoft.kms.Repository.customize.CustomizeStatisticsDownRepository;
import source.inysoft.kms.Repository.customize.CustomizeStatisticsMemberRepository;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StatisticsService {


    @Autowired
    protected CustomizeStatisticsDownRepository statisticsDownRepository;

    @Autowired
    protected CustomizeStatisticsMemberRepository statisticsMemberRepository;

    @Autowired
    protected CustomizeMemberRepository memberRepository;

    /*
     *@  접속 통계 접근 시작
     * params : name
     */
    @Transactional
    public void setMemberStart(String sessionId) {
        CustomizeStatisticsMember statisticsMember = statisticsMemberRepository.findBySessionId(sessionId);
        if (statisticsMember == null) {
            CustomizeStatisticsMember inMember = CustomizeStatisticsMember.builder()
                    .userType("nouser")
                    .sessionId(sessionId)
                    .actType("insert")
                    .build();
            statisticsMemberRepository.save(inMember);
        }
    }

    /*
     *@  회원 로그인시에
     */
    @Transactional
    public void setMemberLogin(String sessionId, HashMap<String, Object> resultMap) {
        CustomizeStatisticsMember statisticsMember = statisticsMemberRepository.findBySessionId(sessionId);
        if (statisticsMember != null) {
            CustomizeMember isMember = (CustomizeMember) resultMap.get("memberInfo");
            Role role = isMember.getRole();
            String userType = "";
            if (role.equals(Role.ROLE_MEMBER)) {
                userType = "indi";
            } else if (role.equals(Role.ROLE_MANAGER)) {
                userType = "busi";
            } else {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            CustomizeStatisticsMember inMember = CustomizeStatisticsMember.builder()
                    .userType(userType)
                    .sessionId(sessionId)
                    .startAt(now)
                    .actType("update")
                    .actId(statisticsMember.getId())
                    .build();
            statisticsMemberRepository.save(inMember);

        }
    }

    /*
     *@  회원 로그아웃
     */
    @Transactional
    public void setMemberLogout(String sessionId) {
        CustomizeStatisticsMember statisticsMember = statisticsMemberRepository.findBySessionId(sessionId);
        if (statisticsMember != null) {
            CustomizeStatisticsMember inMember = CustomizeStatisticsMember.builder()
                    .userType(statisticsMember.getUserType())
                    .sessionId(sessionId)
                    .startAt(statisticsMember.getStartAt())
                    .actType("update")
                    .actId(statisticsMember.getId())
                    .build();
            statisticsMemberRepository.save(inMember);

        }
    }
    /*
     *@  사이트 떠나거나 새로 고침 될때
     */
    @Transactional
    public void setMemberOut(HttpSession session, String sessionId) {

        CustomizeStatisticsMember statisticsMember = statisticsMemberRepository.findBySessionId(sessionId);
        if (statisticsMember != null) {
            String userType = statisticsMember.getUserType();
            Boolean updateFlag = true;
            if (session.getAttribute("user_id") == null) { // 로그인 안된경우 (로그인 안했거나 , 로그아웃 한경우)
                if(!userType.equals("nouser")) { // 로그아웃 한 경우
                    updateFlag = false;
                }
            }
            if(updateFlag) {
                CustomizeStatisticsMember inMember = CustomizeStatisticsMember.builder()
                        .userType(userType)
                        .sessionId(sessionId)
                        .startAt(statisticsMember.getStartAt())
                        .actType("update")
                        .actId(statisticsMember.getId())
                        .build();
                statisticsMemberRepository.save(inMember);
            }

        }
    }
}