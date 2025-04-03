package source.inysoft.kms.Repository.core;


import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<CustomizeMember, Long>, JpaSpecificationExecutor<CustomizeMember> {


    public CustomizeMember getById(Long id);

    public CustomizeMember findByUidAndPasswdAndRole(String uid, String passwd,Role role);

    public CustomizeMember findByUidEncode(String code);
    public CustomizeMember findByEmail(String email);
    public CustomizeMember findByNickName(String nick);

    public CustomizeMember findByNickNameAndIdNot(String nick,Long id);
    public CustomizeMember findByEmailAndIdNot(String email,Long id);


    public List<CustomizeMember>  findByRole(Role role);

    @Query(value = "SELECT COUNT(id) AS total FROM member WHERE role=:role AND uout='no'", nativeQuery = true)
    public Map<String,Long> getMemberTotalByRole(@Param(value = "role") String role);

    @Query(value = "UPDATE member SET uid_encode=:uidEncode WHERE uid=:uid", nativeQuery = true)
    @Modifying
    @Transactional
    public void updateSQLUidEncode(@Param(value = "uidEncode") String uidEncode,@Param(value = "uid") String uid);

    Optional<CustomizeMember> findByUid(String uid);
    Optional<CustomizeMember> findByName(String name);
}
