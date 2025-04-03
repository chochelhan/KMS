package source.inysoft.kms.Entity.customize;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.core.Keyword;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.time.LocalDateTime;

@Entity
@Getter
@DiscriminatorValue("CustomizeKeyword")
@NoArgsConstructor
public class CustomizeKeyword extends Keyword {

    @Builder
    public CustomizeKeyword(String name) {
        this.name = name;
        this.hit = 1;
        LocalDateTime now = LocalDateTime.now();
        this.createAt = now;

    }

}
