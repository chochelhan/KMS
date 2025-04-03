package source.inysoft.kms.Entity.customize;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.core.StatisticsMember;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("CustomizeStatisticsMember")
@NoArgsConstructor
public class CustomizeStatisticsMember extends StatisticsMember {

    @Builder
    public CustomizeStatisticsMember(String userType,
                                     String sessionId,
                                     LocalDateTime startAt,
                                     String actType,
                                     Long actId) {


        this.userType = userType;
        this.sessionId = sessionId;
        LocalDateTime now = LocalDateTime.now();

        if(actType.equals("update")) {
            this.id = actId;
            this.endAt = now;
            this.startAt = startAt;
        } else {
            this.endAt = now;
            this.startAt = now;
        }

    }

}
