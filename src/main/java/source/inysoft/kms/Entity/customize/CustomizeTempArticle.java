package source.inysoft.kms.Entity.customize;


import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import source.inysoft.kms.Entity.core.TempArticle;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@Getter
@DiscriminatorValue("CustomizeTempArticle")
@NoArgsConstructor
public class CustomizeTempArticle extends TempArticle {

    @Builder
    public CustomizeTempArticle(String bid,
                                String articleId,
                                String tempType,
                                String uid,
                                String userName,
                                String userPassword,
                                String category,
                                String subject,
                                String content,
                                String dfileNames,
                                String searchDfileNames,
                                String dfileOrgNames,
                                String keywords,
                                String imgs,
                                String searchImgNames,
                                String orgImgNames,
                                String replyUse,
                                String open,
                                String userNotice,
                                String notice) {
        this.bid = bid;
        this.articleId = articleId;
        this.tempType = tempType;
        this.uid = uid;
        this.userName = userName;
        this.userPassword = userPassword;
        this.category = category;
        this.subject = subject;
        this.content = content;
        this.dfileNames = dfileNames;
        this.searchDfileNames = searchDfileNames;
        this.dfileOrgNames = dfileOrgNames;
        this.keywords = keywords;
        this.imgs = imgs;
        this.searchImgNames = searchImgNames;
        this.orgImgNames = orgImgNames;
        this.replyUse = replyUse;
        this.open = open;
        this.userNotice = userNotice;
        this.notice = notice;
    }

}
