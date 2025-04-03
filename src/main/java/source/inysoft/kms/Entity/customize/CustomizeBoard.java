package source.inysoft.kms.Entity.customize;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.core.Board;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter
@DiscriminatorValue("CustomizeBoard")
@NoArgsConstructor
public class CustomizeBoard extends Board {

    @Builder
    public CustomizeBoard(String bid,
                          String bname,
                          String buse,
                          String categoryUse,
                          String impt,
                          String categoryList,
                          String wauth,
                          int brank,
                          String actType,
                          Long actId) {
        if(actType.equals("update")) {
            this.id = actId;
        }
        this.bid = bid;
        this.bname = bname;
        this.buse = buse;
        this.categoryUse = categoryUse;
        this.impt = impt;
        this.categoryList = categoryList;
        this.wauth = wauth;
        this.brank = brank;

    }

}
