package source.inysoft.kms.Repository.core.specification;

import org.springframework.data.jpa.domain.Specification;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;

import java.time.LocalDateTime;

public class MemberSpecification {

    public static Specification<CustomizeMember> likeName(String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("name"),"%"+name+"%");
    }
    public static Specification<CustomizeMember> likeUid(String uid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("uid"),"%"+uid+"%");
    }
    public static Specification<CustomizeMember> likeEmail(String email) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("email"),"%"+email+"%");
    }
    public static Specification<CustomizeMember> likeNickName(String nickName) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("nickName"),"%"+nickName+"%");
    }
    public static Specification<CustomizeMember> equalEmailSend(String emailSend) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("emailSend"),emailSend);
    }

    public static Specification<CustomizeMember> equalRole(Role role) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"),role);
    }

    public static Specification<CustomizeMember> equalAuth(String auth) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("auth"),auth);
    }

    public static Specification<CustomizeMember> equalNotRole(Role role) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("role"),role);
    }
    public static Specification<CustomizeMember> equalNotUout(String uout) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("uout"),uout);
    }

    public static Specification<CustomizeMember> startCreateAt(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createAt"),date);
    }
    public static Specification<CustomizeMember> endCreateAt(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createAt"),date);
    }


}
