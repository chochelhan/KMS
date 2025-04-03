package source.inysoft.kms.controllers.api.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Entity.customize.CustomizeKeyword;
import source.inysoft.kms.service.api.customize.CustomizeKeywordService;
import source.inysoft.kms.service.api.customize.CustomizeSearchService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;


public abstract class SearchController {

    @Autowired
    protected CustomizeSearchService searchService;

    @Autowired
    protected CustomizeKeywordService keywordService;



    /**
     * 전체(댓글,문서,파일,이미지) 검색
     * @param params
     * @return
     */

    @PostMapping("searchAll")
    public ResponseEntity<HashMap<String, Object>> searchAll(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> resultMap = searchService.searchAll(params);
        String keyword = (String) params.get("keyword");
        keywordService.insertKeyword(keyword);
        resultMap.put("keywordList",keywordService.getKeywordLimit());

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }


    /**
     * 전체 검색 중 구분검색(댓글,문서,파일,이미지)
     * @param params
     * @return
     */

    @PostMapping("searchPart")
    public ResponseEntity<HashMap<String, Object>> searchPart(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> resultMap = searchService.searchPart(params);


        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }
    /**
     * 레이어 팝업에서 통합검색
     * @param params
     * @return
     */

    @PostMapping("searchData")
    public ResponseEntity<HashMap<String, Object>> searchData(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> resultMap = searchService.searchData(params);
        String keyword = (String) params.get("keyword");
        keywordService.insertKeyword(keyword);
        resultMap.put("keywordList",keywordService.getKeywordLimit());

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }

    /**
     * 검색
     * @param params
     * @return
     */

    @PostMapping("searchBidData")
    public ResponseEntity<HashMap<String, Object>> searchBidData(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> resultMap = searchService.searchBid(params);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }


    /**
     * 검색
     * @param params
     * @return
     */

    @PostMapping("searchTotalData")
    public ResponseEntity<HashMap<String, Object>> searchTotalData(@RequestBody HashMap<String, Object> params) throws IOException {

        HashMap<String, Object> resultMap = searchService.searchTotal(params);

        return ResponseEntity.status(HttpStatus.OK).body(resultMap);
    }
}



