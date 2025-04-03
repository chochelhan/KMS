package source.inysoft.kms.Entity.common;

import lombok.Getter;

@Getter
public enum Role {

    ROLE_ADMIN("admin"), ROLE_MANAGER("busi"), ROLE_MEMBER("indi");

    private String description;

    Role(String description) {
        this.description = description;
    }
}