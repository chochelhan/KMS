package source.inysoft.kms.Entity.customize;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.core.MemberNotice;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("CustomizeMemberNotice")
@NoArgsConstructor
public class CustomizeMemberNotice extends MemberNotice {


    @Builder
    public CustomizeMemberNotice(String uid,
                                 String bid,
                                 String gtype,
                                 String gid,
                                 String view,
                                 String userName,
                                 String parentId,
                                 String subject) {

        this.uid = uid;
        this.bid = bid;
        this.gtype = gtype;
        this.gid = gid;
        this.parentId = parentId;
        this.view = view;
        this.userName = userName;
        this.subject = subject;

        LocalDateTime now = LocalDateTime.now();
        this.createAt = now;

    }

}
