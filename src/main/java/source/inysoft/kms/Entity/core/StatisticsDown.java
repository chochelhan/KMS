package source.inysoft.kms.Entity.core;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "index_statistics_down_bid", columnList = "bid"),
        @Index(name = "index_statistics_down_articleId", columnList = "articleId"),
        @Index(name = "index_statistics_down_fileName", columnList = "fileName"),
        @Index(name = "index_statistics_down_orgFileName", columnList = "orgFileName"),
        @Index(name = "index_statistics_down_createAt", columnList = "createAt"),
})
public abstract class StatisticsDown {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;

    @Column(nullable = false, length = 20)
    protected String bid;

    @Column(nullable = false, length = 60)
    protected String articleId;

    @Column(nullable = false, length = 100)
    protected String fileName;

    @Column(nullable = false, length = 100)
    protected String orgFileName;


    protected LocalDateTime createAt;
}
