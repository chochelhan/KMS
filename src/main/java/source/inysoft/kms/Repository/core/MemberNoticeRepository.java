package source.inysoft.kms.Repository.core;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMemberNotice;

import java.util.List;

public interface MemberNoticeRepository extends JpaRepository<CustomizeMemberNotice, Long>, JpaSpecificationExecutor<CustomizeMemberNotice> {


    public CustomizeMemberNotice getById(Long id);
    public List<CustomizeMemberNotice> findByUid(String uid);

    @Query(value = "SELECT * FROM member_notice WHERE (gid = :gid OR parent_id = :gid) AND uid = :uid", nativeQuery = true)
    public List<CustomizeMemberNotice> getSQLByGidAndUserId(String gid,String uid);

    @Query(value = "DELETE FROM member_notice WHERE (gid = :gid OR parent_id = :gid)", nativeQuery = true)
    @Modifying
    @Transactional
    public int deleteSQLByGid(@Param(value = "gid") String gid);

    @Query(value = "UPDATE  member_notice SET view='yes' WHERE id = :id", nativeQuery = true)
    @Modifying
    @Transactional
    public int updateSQLView(@Param(value = "id") Long id);


}
