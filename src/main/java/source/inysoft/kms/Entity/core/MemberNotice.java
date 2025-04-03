package source.inysoft.kms.Entity.core;
import lombok.*;
import source.inysoft.kms.Entity.common.Role;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "index_member_notice_uid", columnList = "uid"),
        @Index(name = "index_member_notice_createAt", columnList = "createAt")
})
public class MemberNotice {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;

    @Column(length = 20)
    protected String uid;

    @Column(length = 30)
    protected String userName;

    @Column(length = 20)
    protected String bid;

    @Column(length = 10)
    protected String gtype;

    @Column(length = 70)
    protected String gid;

    @Column(length = 70)
    protected String parentId;


    @Column(length = 3)
    protected String view;

    @Column(length = 100)
    protected String subject;

    protected LocalDateTime createAt;
}
