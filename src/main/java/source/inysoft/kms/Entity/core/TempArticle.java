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
        @Index(name = "index_temp_article_uid", columnList = "uid"),
        @Index(name = "index_temp_article_articleId", columnList = "articleId"),
        @Index(name = "index_temp_article_tempType", columnList = "tempType"),
})
public abstract class TempArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;

    @Column(nullable = true, length = 20)
    protected String bid;

    @Column(nullable = true, length = 100)
    protected String articleId;

    @Column(nullable = false, length = 8)
    protected String tempType;

    @Column(nullable = true, length = 50)
    protected String uid;

    @Column(nullable = true, length = 50)
    protected String userName;

    @Column(nullable = true, length = 50)
    protected String userPassword;

    @Column(nullable = true, length = 100)
    protected String subject;

    @Column(nullable = true, length = 60)
    protected String category;

    @Column(columnDefinition="TEXT",nullable = true)
    protected String content;

    @Column(nullable = true, length = 255)
    protected String dfileNames;

    @Column(nullable = true, length = 255)
    protected String searchDfileNames;

    @Column(nullable = true, length = 255)
    protected String dfileOrgNames;

    @Column(nullable = true, length = 255)
    protected String keywords;

    @Column(nullable = true, length = 255)
    protected String imgs;

    @Column(nullable = true, length = 255)
    protected String searchImgNames;

    @Column(nullable = true, length = 255)
    protected String orgImgNames;

    @Column(nullable = true, length = 3)
    protected String replyUse;

    @Column(nullable = true, length = 3)
    protected String open;

    @Column(nullable = true, length = 3)
    protected String userNotice;

    @Column(nullable = true, length = 3)
    protected String notice;

}
