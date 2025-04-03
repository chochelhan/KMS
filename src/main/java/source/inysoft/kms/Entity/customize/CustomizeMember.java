package source.inysoft.kms.Entity.customize;

import lombok.*;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.core.Member;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("CustomizeMember")
@NoArgsConstructor
public class CustomizeMember extends Member {

    @Builder
    public CustomizeMember(String uid,
                           String uidEncode,
                           String auth,
                           String busiName,
                           String passwd,
                           Role role,
                           String name,
                           String nickName,
                           String email,
                           String emailSend,
                           String img,
                           String uout,
                           LocalDateTime createAt,
                           String actType,
                           Long actId) {

        this.name = name;
        this.email = email;
        this.uid = uid;
        this.uidEncode = uidEncode;
        this.passwd = passwd;
        this.role = role;
        this.busiName = busiName;
        this.nickName = nickName;
        this.emailSend = emailSend;
        this.img = img;
        this.uout = uout;
        this.auth = auth;

        LocalDateTime now = LocalDateTime.now();

        if(actType.equals("update")) {
            this.id = actId;
            this.updateAt = now;
            this.createAt = (createAt == null)?now:createAt;
        } else {
            this.updateAt = now;
            this.createAt = now;
        }

    }

}
