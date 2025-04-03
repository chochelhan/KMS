package source.inysoft.kms.Entity.customize;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.core.Setting;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter
@DiscriminatorValue("CustomizeSetting")
@NoArgsConstructor
public class CustomizeSetting extends Setting {
    @Builder
    public CustomizeSetting(String menu,
                            String sns,
                            String agree,
                            String email,
                            String actType,
                            Long actId) {
        if (actType.equals("update")) {
            this.id = actId;
        }
        this.menu = menu;
        this.sns = sns;
        this.agree = agree;
        this.email = email;

    }


}
