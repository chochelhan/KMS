package source.inysoft.kms.Repository.core;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import source.inysoft.kms.Entity.customize.CustomizeStatisticsDown;

import javax.persistence.Column;
import java.util.List;
import java.util.Map;

public interface StatisticsDownRepository extends JpaRepository<CustomizeStatisticsDown, Long> {



    /**
     * @ 일별 통계
     */


    @Query(value = "SELECT SUBSTRING(m.create_at,1,10) AS date, m.file_name, COUNT(m.id) AS total," +
            " (SELECT  org_file_name FROM statistics_down WHERE file_name=m.file_name LIMIT 1) as org_file_name" +
            " FROM statistics_down AS m WHERE SUBSTRING(m.create_at,1,10)>=:stDate AND SUBSTRING(m.create_at,1,10)<=:enDate" +
            " AND m.file_name=:fileName GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getDownByDay(@Param(value = "stDate") String stDate,
                                                 @Param(value = "enDate") String enDate,
                                                 @Param(value = "fileName") String fileName);


    @Query(value = "SELECT file_name, COUNT(id) AS total" +
            " FROM statistics_down WHERE SUBSTRING(create_at,1,10)>=:stDate AND SUBSTRING(create_at,1,10)<=:enDate" +
            " GROUP BY file_name ORDER BY total DESC LIMIT 7", nativeQuery = true)
    public List<Map<String,Object>> getDownTotalByDay(@Param(value = "stDate") String stDate,@Param(value = "enDate") String enDate);

    /**
     * @ 주별 통계
     */

    @Query(value = "SELECT DAYOFWEEK(m.create_at) AS date,m.file_name, COUNT(m.id) AS total," +
            " (SELECT  org_file_name FROM statistics_down WHERE file_name=m.file_name LIMIT 1) as org_file_name" +
            " FROM statistics_down AS m WHERE SUBSTRING(m.create_at,1,10)>=:stDate AND SUBSTRING(m.create_at,1,10)<=:enDate" +
            " AND m.file_name=:fileName GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getDownByWeek(@Param(value = "stDate") String stDate,
                                                  @Param(value = "enDate") String enDate,
                                                  @Param(value = "fileName") String fileName);



    /**
     * @ 월별 통계
     */

    @Query(value = "SELECT SUBSTRING(m.create_at,6,2) AS date,SUBSTRING(m.create_at,1,7) AS viewDate,m.file_name," +
            " COUNT(m.id) AS total," +
            " (SELECT  org_file_name FROM statistics_down WHERE file_name=m.file_name LIMIT 1) as org_file_name" +
            " FROM statistics_down AS m WHERE SUBSTRING(m.create_at,1,7) >= :stMonth AND" +
            " SUBSTRING(m.create_at,1,7) <= :enMonth AND m.file_name=:fileName" +
            " GROUP BY date ASC", nativeQuery = true)
    public List<Map<String,Object>> getDownByMonth(@Param(value = "stMonth") String stMonth,
                                                   @Param(value = "enMonth") String enMonth,
                                                   @Param(value = "fileName") String fileName);


    @Query(value = "SELECT file_name, COUNT(id) AS total" +
            " FROM statistics_down WHERE SUBSTRING(create_at,1,7) >= :stMonth AND SUBSTRING(create_at,1,7) <= :enMonth" +
            " GROUP BY file_name ORDER BY total DESC LIMIT 7", nativeQuery = true)
    public List<Map<String,Object>> getDownTotalByMonth(@Param(value = "stMonth") String stMonth,@Param(value = "enMonth") String enMonth);


    /**
     * @ 년도별 통계
     */

    @Query(value = "SELECT SUBSTRING(m.create_at,1,4) AS date,m.file_name,COUNT(m.id) AS total," +
            " (SELECT  org_file_name FROM statistics_down WHERE file_name=m.file_name LIMIT 1) as org_file_name" +
            " FROM statistics_down AS m WHERE SUBSTRING(m.create_at,1,4) >= :stYear AND SUBSTRING(m.create_at,1,4) <= :enYear" +
            " AND m.file_name=:fileName GROUP BY date DESC", nativeQuery = true)
    public List<Map<String,Object>> getDownByYear(@Param(value = "stYear") String stYear,
                                                  @Param(value = "enYear") String enYear,
                                                  @Param(value = "fileName") String fileName);


    @Query(value = "SELECT file_name, COUNT(id) AS total" +
            " FROM statistics_down WHERE SUBSTRING(create_at,1,4) >= :stYear AND SUBSTRING(create_at,1,4) <= :enYear " +
            " GROUP BY file_name ORDER BY total DESC LIMIT 7", nativeQuery = true)
    public List<Map<String,Object>> getDownTotalByYear(@Param(value = "stYear") String stYear,@Param(value = "enYear") String enYear);


    /**
     * @ 총 갯수
     */
    @Query(value = "SELECT COUNT(id) AS total FROM statistics_down", nativeQuery = true)
    public Map<String,Long> getDownTotal();
}
