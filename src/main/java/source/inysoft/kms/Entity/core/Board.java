package source.inysoft.kms.Entity.core;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NoArgsConstructor
@AllArgsConstructor
@Table(indexes = {
        @Index(name = "index_board_bname", columnList = "bname"),
        @Index(name = "index_board_buse", columnList = "buse"),
        @Index(name = "index_board_impt", columnList = "impt"),
        @Index(name = "index_board_brank", columnList = "brank"),
})
public abstract class Board {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    protected long id;

    @Column(unique = true,nullable = false, length = 20)
    protected String bid;

    @Column(nullable = false, length = 50)
    protected String bname;

    @Column(nullable = false, length = 3)
    protected String buse;

    @Column(nullable = false, length = 3)
    protected String categoryUse;

    @Column(nullable = false, length = 3)
    protected String impt;

    @Column(nullable = true,columnDefinition = "json")
    protected String categoryList;

    @Column(nullable = false, length = 5)
    protected String wauth;

    @Column(nullable = true, length = 5)
    protected int brank;

}
