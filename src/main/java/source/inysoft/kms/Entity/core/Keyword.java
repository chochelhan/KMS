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
        @Index(name = "index_keyword_name", columnList = "name"),
        @Index(name = "index_keyword_hit", columnList = "hit"),
        @Index(name = "index_keyword_createAt", columnList = "createAt")
})
public abstract class Keyword {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;

    @Column(nullable = false, length = 100)
    protected String name;

    protected int hit;

    protected LocalDateTime createAt;



}

