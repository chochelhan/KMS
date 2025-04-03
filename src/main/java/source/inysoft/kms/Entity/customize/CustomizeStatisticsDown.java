package source.inysoft.kms.Entity.customize;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.core.StatisticsDown;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("CustomizeStatisticsDown")
@NoArgsConstructor
public class CustomizeStatisticsDown extends StatisticsDown {

    @Builder
    public CustomizeStatisticsDown(String bid,
                                   String articleId,
                                   String fileName,
                                   String orgFileName) {


        this.bid = bid;
        this.articleId = articleId;
        this.fileName = fileName;
        this.orgFileName = orgFileName;

        LocalDateTime now = LocalDateTime.now();
        this.createAt = now;

    }

}
