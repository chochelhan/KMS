package source.inysoft.kms.service.admin.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Entity.customize.CustomizeStatisticsDown;
import source.inysoft.kms.Repository.customize.*;
import source.inysoft.kms.opensearch.customize.CustomizeStatisticsSearch;
import source.inysoft.kms.opensearch.customize.CustomizeTotalSearch;

import java.io.IOException;
import java.util.*;

public abstract class AdminStatisticsService {

    @Autowired
    protected CustomizeStatisticsSearch customizeStatisticsSearch;

    @Autowired
    protected CustomizeTotalSearch customizeTotalSearch;

    @Autowired
    protected CustomizeKeywordRepository keywordRepository;

    @Autowired
    protected CustomizeStatisticsMemberRepository statisticsMemberRepository;

    @Autowired
    protected CustomizeStatisticsDownRepository statisticsDownRepository;

    @Autowired
    protected CustomizeMemberRepository memberRepository;

    @Autowired
    protected CustomizeBoardRepository boardRepository;

    /*
     *@  관리자 메인 페이지 정보
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getAdminMainInfo(HashMap<String, String> params) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        Map<String, Long> busiInfo = memberRepository.getMemberTotalByRole("ROLE_MANAGER");
        Map<String, Long> indiInfo = memberRepository.getMemberTotalByRole("ROLE_MEMBER");
        result.put("busi", busiInfo.get("total"));
        result.put("indi", indiInfo.get("total"));

        Long articleTotal = customizeStatisticsSearch.getArticleTotal();
        result.put("articleTotal", articleTotal);

        Map<String, Long> downInfo = statisticsDownRepository.getDownTotal();
        result.put("down", downInfo.get("total"));

        List<Map<String, Object>> keywordData = keywordRepository.getSQLKeywordLimit();
        result.put("keywordData", keywordData);

        List<Map<String, Object>> downFileData = new ArrayList<>();
        List<Map<String, Object>> downTotal = new ArrayList<>();
        Map<String, Object> downData = new HashMap<String, Object>();

        String stdate = params.get("stdate");
        String endate = params.get("endate");
        downTotal = statisticsDownRepository.getDownTotalByDay(stdate, endate);
        for (Map<String, Object> data : downTotal) {
            String fileName = (String) data.get("file_name");
            downFileData = statisticsDownRepository.getDownByDay(stdate, endate, fileName);
            downData.put(fileName, downFileData);
        }
        result.put("downData", downData);
        List<Map<String, Object>> memberData = statisticsMemberRepository.getMemberInviteByDay(stdate, endate);
        result.put("memberData", memberData);

        HashMap<String, Object> bidParams = new HashMap<>();
        List<CustomizeBoard> boardList = boardRepository.getFindByBuseAndImpt("yes", "yes");

        HashMap<String, Object> bidCount = new HashMap<>();
        if (boardList.size() > 0) {
            for (CustomizeBoard board : boardList) {
                bidParams.put("bid", board.getBid());
                Map<String, Long> articleData = customizeStatisticsSearch.getArticleStatistics(bidParams);
                bidCount.put(board.getBid(), articleData);
            }
        }
        result.put("articleData", bidCount);
        result.put("status", "success");
        return result;
    }

    /*
     *@  컨텐츠 통계
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getContent() throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        List<CustomizeBoard> boardList = boardRepository.getFindByBuseAndImpt("yes", "yes");

        HashMap<String, Object> bidCount = new HashMap<>();
        if (boardList.size() > 0) {
            for (CustomizeBoard board : boardList) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("bid", board.getBid());
                Map<String, Long> articleData = customizeStatisticsSearch.getArticleStatistics(params);
                bidCount.put(board.getBid(),articleData);

            }

        }
        List<Map<String, Object>> keywordData = keywordRepository.getSQLKeywordLimit();
        List<Map<String, Object>> keywordResult = new ArrayList<>();

        for(Map<String,Object> keyItem : keywordData) {
            Map<String, Object> keywordItem = new HashMap<>();
            keywordItem.put("name",keyItem.get("name"));
            keywordItem.put("kms_riverflow_keyword_count",keyItem.get("hits"));
            if (boardList.size() > 0) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("keyword",(String) keyItem.get("name"));
                params.put("keywordCmd","all");
                for (CustomizeBoard board : boardList) {
                    params.put("bid", board.getBid());
                    Long keywordCount = customizeTotalSearch.searchTotal(params);
                    keywordItem.put(board.getBid(),keywordCount);
                }
            }
            keywordResult.add(keywordItem);
        }

        result.put("boardList", boardList);
        result.put("articleData", bidCount);
        result.put("keywordData", keywordResult);

        result.put("status", "success");
        return result;
    }


    /*
     *@  회원 접속 통계
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> getMember(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String dateType = (String) params.get("dateType");
        String styear = (String) params.get("styear");
        String enyear = (String) params.get("enyear");
        String stmonth = (String) params.get("stmonth");
        String enmonth = (String) params.get("enmonth");
        String stdate = (String) params.get("stdate");
        String endate = (String) params.get("endate");
        List<Map<String, Object>> memberData = new ArrayList<>();
        List<Map<String, Object>> memberTimes = new ArrayList<>();

        switch (dateType) {
            case "day":
                memberData = statisticsMemberRepository.getMemberInviteByDay(stdate, endate);
                memberTimes = statisticsMemberRepository.getMemberTimesByDay(stdate, endate);
                break;
            case "week":
                memberData = statisticsMemberRepository.getMemberInviteByWeek(stdate, endate);
                memberTimes = statisticsMemberRepository.getMemberTimesByWeek(stdate, endate);
                break;
            case "month":
                memberData = statisticsMemberRepository.getMemberInviteByMonth(stmonth, enmonth);
                memberTimes = statisticsMemberRepository.getMemberTimesByMonth(stmonth, enmonth);
                break;
            case "year":
                memberData = statisticsMemberRepository.getMemberInviteByYear(styear, enyear);
                memberTimes = statisticsMemberRepository.getMemberTimesByYear(styear, enyear);
                break;
        }

        result.put("memberData", memberData);
        result.put("memberTimes", memberTimes);

        result.put("status", "success");
        return result;
    }


    /*
     *@  다운로드 접속 통계
     * params :
     * return : {status:(message,success,fail)}
     */
    public HashMap<String, Object> getDownload(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String dateType = (String) params.get("dateType");
        String styear = (String) params.get("styear");
        String enyear = (String) params.get("enyear");
        String stmonth = (String) params.get("stmonth");
        String enmonth = (String) params.get("enmonth");
        String stdate = (String) params.get("stdate");
        String endate = (String) params.get("endate");

        Map<String, Object> downData = new HashMap<String, Object>();

        List<Map<String, Object>> downFileData = new ArrayList<>();
        List<Map<String, Object>> downTotal = new ArrayList<>();

        switch (dateType) {
            case "day":
                downTotal = statisticsDownRepository.getDownTotalByDay(stdate, endate);
                for (Map<String, Object> data : downTotal) {
                    String fileName = (String) data.get("file_name");
                    downFileData = statisticsDownRepository.getDownByDay(stdate, endate, fileName);
                    downData.put(fileName, downFileData);
                }
                break;

            case "week":
                downTotal = statisticsDownRepository.getDownTotalByDay(stdate, endate);
                for (Map<String, Object> data : downTotal) {
                    String fileName = (String) data.get("file_name");
                    downFileData = statisticsDownRepository.getDownByWeek(stdate, endate, fileName);
                    downData.put(fileName, downFileData);
                }

                break;
            case "month":
                downTotal = statisticsDownRepository.getDownTotalByMonth(stmonth, enmonth);
                for (Map<String, Object> data : downTotal) {
                    String fileName = (String) data.get("file_name");
                    downFileData = statisticsDownRepository.getDownByMonth(stmonth, enmonth, fileName);
                    downData.put(fileName, downFileData);
                }
                break;
            case "year":
                downTotal = statisticsDownRepository.getDownTotalByYear(styear, enyear);
                for (Map<String, Object> data : downTotal) {
                    String fileName = (String) data.get("file_name");
                    downFileData = statisticsDownRepository.getDownByYear(styear, enyear, fileName);
                    downData.put(fileName, downFileData);
                }

                break;
        }

        result.put("downData", downData);
        result.put("downTotalData", downTotal);

        result.put("status", "success");
        return result;
    }

    /*
     *@  키워드 통계
     * params :
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> getKeyword(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String stdate = (String) params.get("stdate");
        String endate = (String) params.get("endate");

        List<Map<String, Object>> keywordData = keywordRepository.getSQLKeywordByDate(stdate, endate);

        result.put("keywordData", keywordData);
        result.put("status", "success");
        return result;
    }
}
