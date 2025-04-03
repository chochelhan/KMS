package source.inysoft.kms.Repository.core.specification;

import org.springframework.data.jpa.domain.Specification;
import source.inysoft.kms.Entity.customize.CustomizeMemberNotice;

import java.time.LocalDateTime;

public class MemberNoticeSpecification {

    public static Specification<CustomizeMemberNotice> equalUid(String uid) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("uid"),uid);
    }
    public static Specification<CustomizeMemberNotice> equalView() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("view"),"no");
    }

    public static Specification<CustomizeMemberNotice> startCreateAt(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("createAt"),date);
    }
    public static Specification<CustomizeMemberNotice> endCreateAt(LocalDateTime date) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("createAt"),date);
    }


}
