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

        @Index(name = "index_member_uidEncode", columnList = "uidEncode"),
        @Index(name = "index_member_nickName", columnList = "nickName"),
        @Index(name = "index_member_role", columnList = "role"),
        @Index(name = "index_member_auth", columnList = "auth"),
        @Index(name = "index_member_uout", columnList = "uout"),
        @Index(name = "index_member_name", columnList = "name"),
        @Index(name = "index_member_emailSend", columnList = "emailSend"),
        @Index(name = "index_member_createAt", columnList = "createAt")
})
public abstract class Member {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;

    @Column(unique = true,nullable = false, length = 20)
    protected String uid;

    @Column(nullable = true, length = 100)
    protected String uidEncode;

    @Column(nullable = false, length = 3)
    protected String auth;

    @Column(nullable = true, length = 50)
    protected String busiName;

    @Column(nullable = false, length = 100)
    protected String passwd;

    @Enumerated(EnumType.STRING)
    protected Role role;

    @Column(nullable = false, length = 30)
    protected String name;

    @Column(length = 30)
    protected String nickName;

    @Column(unique = true,nullable = false, length = 60)
    protected String email;

    @Column(length = 3)
    protected String emailSend;

    @Column(length = 100)
    protected String img;

    @Column(length = 3)
    protected String uout;

    protected LocalDateTime createAt;

    protected LocalDateTime updateAt;



}

