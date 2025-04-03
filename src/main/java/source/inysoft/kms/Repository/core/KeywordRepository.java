package source.inysoft.kms.Repository.core;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import source.inysoft.kms.Entity.customize.CustomizeKeyword;

import java.util.List;
import java.util.Map;

public interface KeywordRepository extends JpaRepository<CustomizeKeyword, Long> {


    public CustomizeKeyword getById(Long id);

    public List<CustomizeKeyword> findByName(String name);

    @Query(value = "SELECT COUNT(hit) AS hits,name FROM keyword GROUP BY name ORDER BY hits DESC LIMIT 10", nativeQuery = true)
    public List<Map<String,Object>> getSQLKeywordLimit();



    @Query(value = "SELECT COUNT(hit) AS hits,name FROM keyword WHERE SUBSTRING(create_at,1,10)>=:stDate AND SUBSTRING(create_at,1,10)<=:enDate GROUP BY name ORDER BY hits DESC LIMIT 15", nativeQuery = true)
    public List<Map<String,Object>> getSQLKeywordByDate(@Param(value = "stDate") String stDate,
                                                        @Param(value = "enDate") String enDate);

}
