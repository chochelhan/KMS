package source.inysoft.kms.Entity.core;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "index_statistics_member_userType", columnList = "userType"),
        @Index(name = "index_statistics_member_sessionId", columnList = "sessionId"),
        @Index(name = "index_statistics_member_startAt", columnList = "startAt"),
        @Index(name = "index_statistics_member_endAt", columnList = "endAt"),
})
public abstract class StatisticsMember {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;

    @Column(nullable = false, length = 10)
    protected String userType;

    @Column(nullable = false, length = 150)
    protected String sessionId;

    protected LocalDateTime startAt;

    protected LocalDateTime endAt;

}
