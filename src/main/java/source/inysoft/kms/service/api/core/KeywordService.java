package source.inysoft.kms.service.api.core;


import org.springframework.beans.factory.annotation.Autowired;

import source.inysoft.kms.Entity.customize.CustomizeKeyword;
import source.inysoft.kms.Repository.customize.CustomizeKeywordRepository;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class KeywordService {


    @Autowired
    protected CustomizeKeywordRepository keywordRepository;

    /*
     *@  나의 알림 목록
     * params :  session
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public List<Map<String,Object>> getKeywordLimit() {


        return keywordRepository.getSQLKeywordLimit();
    }

    /*
     *@  키워드 저장
     * params : name
     */
    @Transactional
    public void insertKeyword(String name) {

        CustomizeKeyword keyword = CustomizeKeyword.builder()
                .name(name)
                .build();
        keywordRepository.save(keyword);
    }




}