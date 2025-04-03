package source.inysoft.kms.service.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Repository.customize.CustomizeBoardRepository;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;
import source.inysoft.kms.opensearch.customize.CustomizeCommentSearch;
import source.inysoft.kms.opensearch.customize.CustomizeTotalSearch;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public abstract class SearchService {

    @Autowired
    protected CustomizeTotalSearch totalSearch;

    @Autowired
    protected CustomizeBoardArticleSearch boardArticleSearch;


    @Autowired
    protected CustomizeBoardRepository boardRepository;

    @Autowired
    protected CustomizeCommentSearch commentSearch;

    /*
     *@ 전체검색
     * params :  keyword
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> searchAll(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> articleData = boardArticleSearch.articleSearch(params);
        result.put("articleData", articleData);

        HashMap<String, Object> articleImageData = boardArticleSearch.articleImageSearch(params);
        result.put("articleImageData", articleImageData);

        HashMap<String, Object> articleFileData = boardArticleSearch.articleFileSearch(params);
        result.put("articleFileData", articleFileData);

        HashMap<String, Object> commentData = commentSearch.allSearch(params);
        result.put("commentData", commentData);


        result.put("status", "success");
        return result;
    }

    /*
     *@ 전체검색 중 부분 검색
     * params :  keyword
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> searchPart(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<>();

        String code = (String) params.get("code");
        switch (code) {
            case "article":
                result.put("resultData", boardArticleSearch.articleSearch(params));
                break;
            case "comment":
                result.put("resultData", commentSearch.allSearch(params));
                break;
            case "img":
                result.put("resultData", boardArticleSearch.articleImageSearch(params));
                break;
            case "file":
                result.put("resultData", boardArticleSearch.articleFileSearch(params));
                break;
        }
        result.put("status", "success");
        return result;
    }

    /*
     *@ 레이어 팝업 통합검색 (초기 전체검색)
     * params :  keyword
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> searchData(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> totalData = totalSearch.totalSearch(params);

        /// 키워드 통계를 위한 부분
        List<String> searchBids = (List<String>) params.get("searchBids");
        HashMap<String, Long> bidCount = new HashMap<>();
        if (searchBids.size() > 0) {
            for (String bid : searchBids) {
                params.put("bid", bid);
                Long total = totalSearch.searchTotal(params);
                bidCount.put(bid, total);
            }
        }
        List<CustomizeBoard> boardList = boardRepository.getFindByBuseAndImpt("yes", "yes");
        if(params.get("getCategory").equals("yes")) {
            List<CustomizeBoard> categoryList = boardRepository.getFindByBuse("yes");
            result.put("categoryList",categoryList);
        }


        result.put("status", "success");
        result.put("totalData", totalData);
        result.put("boardList",boardList);
        result.put("bidCount",bidCount);


        return result;
    }

    /*
     *@ 레이어 팝업 통합검색 (게시판별 검색)
     * params :  keyword
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> searchBid(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> articleData = totalSearch.search(params);

        result.put("status", "success");
        result.put("articleData", articleData);

        return result;
    }

    /*
     *@ 레이어 팝업 통합검색 (전체 검색)
     * params :  keyword
     * return : {status:(message,success,fail),boardList:List<CustomizeBoard>,info:(CustomizeBoard,null)}
     */
    public HashMap<String, Object> searchTotal(HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> result = new HashMap<>();

        HashMap<String, Object> totalData = totalSearch.totalSearch(params);

        result.put("status", "success");
        result.put("totalData", totalData);
        return result;
    }
}
