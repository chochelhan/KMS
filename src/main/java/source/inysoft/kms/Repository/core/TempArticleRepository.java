package source.inysoft.kms.Repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import source.inysoft.kms.Entity.customize.CustomizeTempArticle;

public interface TempArticleRepository extends JpaRepository<CustomizeTempArticle, Long> {


    public CustomizeTempArticle getFindByUidAndTempType(String uid,String TempType);

    public CustomizeTempArticle getFindByUidAndTempTypeAndArticleId(String uid,String TempType,String articleId);


}
