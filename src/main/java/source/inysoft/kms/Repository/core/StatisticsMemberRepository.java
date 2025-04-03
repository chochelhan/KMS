package source.inysoft.kms.Repository.core;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import source.inysoft.kms.Entity.customize.CustomizeStatisticsMember;

import java.util.List;
import java.util.Map;

public interface StatisticsMemberRepository extends JpaRepository<CustomizeStatisticsMember, Long> {



    public CustomizeStatisticsMember findBySessionId(String sessionId);

    /**
     * @ 일별 통계
     */

    @Query(value = "SELECT COUNT(m.id) AS total, SUBSTRING(m.start_at,1,10) AS date," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='indi' AND SUBSTRING(start_at,1,10) = date) AS indi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='busi' AND SUBSTRING(start_at,1,10) = date) AS busi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='nouser' AND SUBSTRING(start_at,1,10) = date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,10)>=:stDate AND SUBSTRING(m.start_at,1,10)<=:enDate GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getMemberInviteByDay(@Param(value = "stDate") String stDate,@Param(value = "enDate") String enDate);


    @Query(value = "SELECT SUM(TIMESTAMPDIFF(SECOND,m.start_at,m.end_at)) AS total, SUBSTRING(m.start_at,1,10) AS date," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='indi' AND SUBSTRING(start_at,1,10) = date) AS indi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='busi' AND SUBSTRING(start_at,1,10) = date) AS busi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='nouser' AND SUBSTRING(start_at,1,10) = date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,10)>=:stDate AND SUBSTRING(m.start_at,1,10)<=:enDate GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getMemberTimesByDay(@Param(value = "stDate") String stDate,@Param(value = "enDate") String enDate);

    /**
     * @ 주별 통계
     */

    @Query(value = "SELECT COUNT(m.id) AS total, DAYOFWEEK(m.start_at) AS date," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='indi' AND DAYOFWEEK(start_at) = date) AS indi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='busi' AND DAYOFWEEK(start_at) = date) AS busi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='nouser' AND DAYOFWEEK(start_at) = date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,10)>=:stDate AND SUBSTRING(m.start_at,1,10)<=:enDate " +
            " GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getMemberInviteByWeek(@Param(value = "stDate") String stDate,@Param(value = "enDate") String enDate);


    @Query(value = "SELECT SUM(TIMESTAMPDIFF(SECOND,m.start_at,m.end_at)) AS total, DAYOFWEEK(m.start_at) AS date," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='indi' AND DAYOFWEEK(start_at) = date) AS indi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='busi' AND DAYOFWEEK(start_at) = date) AS busi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='nouser' AND DAYOFWEEK(start_at) = date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,10)>=:stDate AND SUBSTRING(m.start_at,1,10)<=:enDate " +
            " GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getMemberTimesByWeek(@Param(value = "stDate") String stDate,@Param(value = "enDate") String enDate);


    /**
     * @ 월별 통계
     */

    @Query(value = "SELECT COUNT(m.id) AS total, SUBSTRING(m.start_at,6,2) AS date, SUBSTRING(m.start_at,1,7) AS viewDate," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='indi' AND SUBSTRING(start_at,6,2)=date) AS indi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='busi' AND SUBSTRING(start_at,6,2)=date) AS busi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='nouser' AND SUBSTRING(start_at,6,2)=date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,7) >= :stMonth AND SUBSTRING(m.start_at,1,7) <= :enMonth" +
            " GROUP BY date ASC", nativeQuery = true)
    public List<Map<String,Object>> getMemberInviteByMonth(@Param(value = "stMonth") String stMonth,@Param(value = "enMonth") String enMonth);


    @Query(value = "SELECT SUM(TIMESTAMPDIFF(SECOND,m.start_at,m.end_at)) AS total, " +
            " SUBSTRING(m.start_at,6,2) AS date, SUBSTRING(m.start_at,1,7) AS viewDate," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='indi' AND SUBSTRING(start_at,6,2) = date) AS indi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='busi' AND SUBSTRING(start_at,6,2) = date) AS busi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='nouser' AND SUBSTRING(start_at,6,2) = date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,7) >= :stMonth AND SUBSTRING(m.start_at,1,7) <= :enMonth" +
            " GROUP BY date ASC", nativeQuery = true)
    public List<Map<String,Object>> getMemberTimesByMonth(@Param(value = "stMonth") String stMonth,@Param(value = "enMonth") String enMonth);


    /**
     * @ 년도별 통계
     */
    @Query(value = "SELECT COUNT(m.id) AS total, SUBSTRING(m.start_at,1,4) AS date," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='indi' AND SUBSTRING(start_at,1,4)=date) AS indi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='busi' AND SUBSTRING(start_at,1,4)=date) AS busi," +
            "(SELECT COUNT(id) FROM statistics_member WHERE user_type='nouser' AND SUBSTRING(start_at,1,4)=date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,4) >= :stYear AND SUBSTRING(m.start_at,1,4) <= :enYear" +
            " GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getMemberInviteByYear(@Param(value = "stYear") String stYear,
                                                          @Param(value = "enYear") String enYear);


    @Query(value = "SELECT SUM(TIMESTAMPDIFF(SECOND,m.start_at,m.end_at)) AS total, SUBSTRING(m.start_at,1,4) AS date," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='indi' AND SUBSTRING(start_at,1,4)=date) AS indi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='busi' AND SUBSTRING(start_at,1,4)=date) AS busi," +
            "(SELECT SUM(TIMESTAMPDIFF(SECOND,start_at,end_at)) FROM statistics_member WHERE user_type='nouser' AND SUBSTRING(start_at,1,4)=date) AS nouser" +
            " FROM statistics_member AS m WHERE SUBSTRING(m.start_at,1,4) >= :stYear AND SUBSTRING(m.start_at,1,4) <= :enYear" +
            " GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getMemberTimesByYear(@Param(value = "stYear") String stYear,
                                                         @Param(value = "enYear") String enYear);
}
