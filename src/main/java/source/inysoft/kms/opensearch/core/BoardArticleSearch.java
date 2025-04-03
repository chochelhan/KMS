package source.inysoft.kms.opensearch.core;


import org.apache.http.HttpHost;
import org.apache.lucene.search.TotalHits;
import org.opensearch.action.delete.DeleteRequest;
import org.opensearch.action.delete.DeleteResponse;
import org.opensearch.action.get.GetRequest;
import org.opensearch.action.get.GetResponse;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.ScoreSortBuilder;
import org.opensearch.search.sort.SortOrder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opensearch.index.query.QueryBuilders.rangeQuery;
import static org.opensearch.index.query.QueryBuilders.termQuery;


public class BoardArticleSearch extends OpenSearch {

    /**
     * @ 검색어 query
     * params: {
     * }
     * return:
     **/
    private QueryBuilder queryKeyword(HashMap<String, Object> params, QueryBuilder qb) {

        String keywordCmd = (String) params.get("keywordCmd");
        String keyword = (String) params.get("keyword");
        String matchTypeString = (String) params.get("matchType");
        String matchType = "and";
        if (matchTypeString != null && !matchTypeString.isEmpty()) {
            matchType = matchTypeString;
        }
        if (keywordCmd != null && !keywordCmd.isEmpty() && keyword != null && !keyword.isEmpty()) {

            if (keywordCmd.equals("all")) {

                BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
                BoolQueryBuilder subjectQuery = QueryBuilders.boolQuery();
                BoolQueryBuilder contentQuery = QueryBuilders.boolQuery();
                switch (matchType) {
                    case "and":
                    case "or":
                        String[] keywords = keyword.split(" ");
                        for (String key : keywords) {
                            if (!key.isEmpty()) {
                                if (matchType.equals("and")) {
                                    ((BoolQueryBuilder) subjectQuery).must(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                                    ((BoolQueryBuilder) contentQuery).must(QueryBuilders.matchPhrasePrefixQuery("content", key));

                                } else {
                                    ((BoolQueryBuilder) subjectQuery).should(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                                    ((BoolQueryBuilder) contentQuery).should(QueryBuilders.matchPhrasePrefixQuery("content", key));


                                }
                            }
                        }
                        ((BoolQueryBuilder) keywordQuery).should(subjectQuery);
                        ((BoolQueryBuilder) keywordQuery).should(contentQuery);
                        ((BoolQueryBuilder) qb).must(keywordQuery);
                        break;
                    case "not":
                        String[] notKeywords = keyword.split("NOT");
                        if (!notKeywords[0].isEmpty()) {
                            String[] inkeywords = notKeywords[0].split(" ");
                            for (String key : inkeywords) {
                                if (!key.isEmpty()) {
                                    ((BoolQueryBuilder) subjectQuery).should(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                                    ((BoolQueryBuilder) contentQuery).should(QueryBuilders.matchPhrasePrefixQuery("content", key));
                                }
                            }

                        }
                        int k = 0;
                        for (String notKey : notKeywords) {
                            if (k > 0 && !notKey.isEmpty()) {
                                ((BoolQueryBuilder) subjectQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("subject", notKey));
                                ((BoolQueryBuilder) contentQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("content", notKey));

                            }
                            k++;
                        }
                        ((BoolQueryBuilder) keywordQuery).should(subjectQuery);
                        ((BoolQueryBuilder) keywordQuery).should(contentQuery);
                        ((BoolQueryBuilder) qb).must(keywordQuery);
                        break;
                }

                ((BoolQueryBuilder) qb).must(keywordQuery);
            } else {
                ((BoolQueryBuilder) qb).must(QueryBuilders.matchPhrasePrefixQuery(keywordCmd, keyword));
            }

        }
        return qb;
    }

    /**
     * @ 페이징 및 정렬처리
     * params: {
     * }
     * return:
     **/
    private SearchSourceBuilder pagingSort(HashMap<String, Object> params) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String pageString = (String) params.get("page");
        if (pageString == null || pageString.isEmpty()) {
            pageString = "1";
        }
        int page = Integer.parseInt(pageString);

        String limitString = (String) params.get("limit");
        if (limitString == null || limitString.isEmpty()) {
            limitString = "20";
        }
        int size = Integer.parseInt(limitString);
        page = (page - 1) * size;

        String orderByField = (String) params.get("orderByField");
        String orderBySort = (String) params.get("orderBySort");
        if (orderBySort == null || orderBySort.isEmpty()) {
            orderBySort = "desc";
        }
        sourceBuilder.from(page);
        sourceBuilder.size(size);
        if (orderByField == null || orderByField.isEmpty()) {
            sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        } else {
            if (orderBySort.equals("asc")) {
                sourceBuilder.sort(new FieldSortBuilder(orderByField).order(SortOrder.ASC));
            } else {
                sourceBuilder.sort(new FieldSortBuilder(orderByField).order(SortOrder.DESC));
            }
        }
        return sourceBuilder;
    }


    /**
     * @ 게시글 검색
     * params: {
     * orderByField: 정렬필드, orderBySort: 정렬방법
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * keywordCmd: 검색필드 ,keyword: 검색어
     * dateCmd:일자검색 필드 (updateAt,createAt) ,stdate: 검색시작 (등록일), endate: 검색 종료일(등록일)
     * bid: 게시판 아이디, category:카테고리
     * }
     * return:
     **/
    public HashMap<String, Object> search(HashMap<String, Object> params) throws IOException {


        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery();

        String bid = (String) params.get("bid");
        if (bid != null && !bid.isEmpty()) {
            ((BoolQueryBuilder) qb).must(termQuery("bid", bid));
            String category = (String) params.get("category");
            if (category != null && !category.isEmpty()) {
                ((BoolQueryBuilder) qb).must(termQuery("category", category));
            }

        } else if (params.get("boardIds") != null) {
            BoolQueryBuilder bidQuery = QueryBuilders.boolQuery();
            List<String> boardIds = (List<String>) params.get("boardIds");
            for (String boardId : boardIds) {
                if (params.get("categorys") != null) {
                    Boolean cateQueryFlag = false;
                    List<Map<String,Object>> categorys = (List<Map<String,Object>>) params.get("categorys");
                    for (Map<String,Object> cate : categorys) {
                        BoolQueryBuilder cateBidQuery = QueryBuilders.boolQuery();
                        String cateBid = (String) cate.get("bid");
                        String code = (String) cate.get("code");
                        if(boardId.equals(cateBid)) {
                            cateQueryFlag = true;
                            ((BoolQueryBuilder) cateBidQuery).must(termQuery("bid", cateBid));
                            ((BoolQueryBuilder) cateBidQuery).must(termQuery("category", code));
                            ((BoolQueryBuilder) bidQuery).should(cateBidQuery);
                        }

                    }
                    if(!cateQueryFlag) {
                        ((BoolQueryBuilder) bidQuery).should(termQuery("bid", boardId));
                    }

                } else {
                    ((BoolQueryBuilder) bidQuery).should(termQuery("bid", boardId));
                }
            }
            ((BoolQueryBuilder) qb).must(bidQuery);
        }


        String open = (String) params.get("open");
        if (open != null && !open.isEmpty()) {
            ((BoolQueryBuilder) qb).must(termQuery("open", open));
        }
        String notice = (String) params.get("notice");
        if (notice != null && !notice.isEmpty()) {
            ((BoolQueryBuilder) qb).must(termQuery("notice", notice));
        }
        String reply = (String) params.get("reply");
        if (reply != null && !reply.isEmpty()) {
            ((BoolQueryBuilder) qb).must(termQuery("replyUse", reply));
        }
        String userId = (String) params.get("userId");
        if (userId != null && !userId.isEmpty()) {
            ((BoolQueryBuilder) qb).must(termQuery("userId", userId));
        }

        qb = queryKeyword(params, qb);

        String stdate = (String) params.get("stdate");
        String endate = (String) params.get("endate");
        String dateCmd = (String) params.get("dateCmd");

        if (dateCmd != null && !dateCmd.isEmpty() && stdate != null && !stdate.isEmpty() && endate != null && !endate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String stdatetime = stdate + " 00:00:01.111";
            String endatetime = endate + " 23:59:59.111";

            LocalDateTime stDateTime = LocalDateTime.parse(stdatetime, formatter);
            LocalDateTime enDateTime = LocalDateTime.parse(endatetime, formatter);
            ((BoolQueryBuilder) qb).must(rangeQuery(dateCmd).gte(stDateTime));
            ((BoolQueryBuilder) qb).must(rangeQuery(dateCmd).lte(enDateTime));
        }

        SearchSourceBuilder sourceBuilder = pagingSort(params);
        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return getList(searchResponse);

    }

    /**
     * @ 게시글 공지사항 검색
     * params: {
     * orderByField: 정렬필드, orderBySort: 정렬방법
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * keywordCmd: 검색필드 ,keyword: 검색어
     * dateCmd:일자검색 필드 (updateAt,createAt) ,stdate: 검색시작 (등록일), endate: 검색 종료일(등록일)
     * bid: 게시판 아이디, category:카테고리
     * }
     * return:
     **/
    public HashMap<String, Object> noticeSearch(HashMap<String, Object> params) throws IOException {

        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(termQuery("open", "yes"))
                .must(termQuery("notice", "yes"));
        String bid = (String) params.get("bid");
        if (bid != null && !bid.isEmpty()) {
            ((BoolQueryBuilder) qb).must(termQuery("bid", bid));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.sort(new FieldSortBuilder("createAt").order(SortOrder.DESC));
        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        return getList(searchResponse);

    }


    /**
     * @ 게시글 정보
     * params: String id
     * return:
     **/
    public HashMap<String, Object> getData(String id) throws IOException {


        GetRequest getRequest = new GetRequest(boardArticleIndex, id);
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("data", response.getSourceAsMap());
        return result;

    }

    /**
     * @ 게시글 저장
     * params:
     * return:
     **/
    public HashMap<String, String> insert(HashMap<String, String> params) throws IOException {

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("bid", params.get("bid"));
        jsonMap.put("userName", params.get("userName"));
        jsonMap.put("userId", params.get("userId"));

        jsonMap.put("replyUse", params.get("replyUse"));
        jsonMap.put("userNotice", params.get("userNotice"));
        jsonMap.put("notice", params.get("notice"));
        jsonMap.put("open", params.get("open"));

        jsonMap.put("dfileNames", params.get("dfileNames"));
        jsonMap.put("dfileOrgNames", params.get("dfileOrgNames"));
        jsonMap.put("keywords", params.get("keywords"));
        jsonMap.put("imgs", params.get("imgs"));
        jsonMap.put("orgImgNames", params.get("orgImgNames"));
        jsonMap.put("searchDfileNames", params.get("searchDfileNames"));
        jsonMap.put("searchImgNames", params.get("searchImgNames"));


        jsonMap.put("userPassword", params.get("userPassword"));
        jsonMap.put("category", params.get("category"));
        jsonMap.put("subject", params.get("subject"));
        jsonMap.put("content", params.get("content"));
        jsonMap.put("commentCnt", 0);
        jsonMap.put("hit", 0);
        jsonMap.put("createAt", now);
        jsonMap.put("updateAt", now);

        IndexRequest request = new IndexRequest(boardArticleIndex)
                .source(jsonMap);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        params.put("gid", indexResponse.getId());

        return params;
    }


    /**
     * @ 게시글 수정
     * params:
     * return:
     **/
    public void update(HashMap<String, String> params, String id) throws IOException {

        GetRequest getRequest = new GetRequest(boardArticleIndex, id);
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> isData = response.getSourceAsMap();


        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("bid", isData.get("bid"));
        jsonMap.put("userId", isData.get("userId"));
        jsonMap.put("userPassword", params.get("userPassword"));
        jsonMap.put("userName", params.get("userName"));
        jsonMap.put("category", params.get("category"));
        jsonMap.put("subject", params.get("subject"));
        jsonMap.put("content", params.get("content"));
        jsonMap.put("commentCnt", isData.get("commentCnt"));
        jsonMap.put("hit", isData.get("hit"));
        jsonMap.put("updateAt", now);
        jsonMap.put("createAt", isData.get("createAt"));

        jsonMap.put("notice", params.get("notice"));
        jsonMap.put("replyUse", params.get("replyUse"));
        jsonMap.put("userNotice", params.get("userNotice"));
        jsonMap.put("open", params.get("open"));

        jsonMap.put("dfileNames", params.get("dfileNames"));
        jsonMap.put("dfileOrgNames", params.get("dfileOrgNames"));
        jsonMap.put("keywords", params.get("keywords"));
        jsonMap.put("imgs", params.get("imgs"));
        jsonMap.put("orgImgNames", params.get("orgImgNames"));
        jsonMap.put("searchDfileNames", params.get("searchDfileNames"));
        jsonMap.put("searchImgNames", params.get("searchImgNames"));


        IndexRequest request = new IndexRequest(boardArticleIndex)
                .id(id)
                .source(jsonMap);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

    }

    /**
     * @ 게시글 조회수,댓글수 변경
     * params:
     * return:
     **/
    public void updateWithHitOrCmt(String id, String type) throws IOException {

        GetRequest getRequest = new GetRequest(boardArticleIndex, id);
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> isData = response.getSourceAsMap();


        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("bid", isData.get("bid"));
        jsonMap.put("userId", isData.get("userId"));
        jsonMap.put("userPassword", isData.get("userPassword"));
        jsonMap.put("userName", isData.get("userName"));
        jsonMap.put("category", isData.get("category"));
        jsonMap.put("subject", isData.get("subject"));
        jsonMap.put("content", isData.get("content"));
        jsonMap.put("secret", isData.get("secret"));

        int CmtCnt = (int) isData.get("commentCnt");
        switch (type) {
            case "cmtPlus":
                CmtCnt = CmtCnt + 1;
                jsonMap.put("commentCnt", CmtCnt);
                jsonMap.put("hit", isData.get("hit"));
                break;
            case "cmtMinus":
                CmtCnt = CmtCnt - 1;
                if (CmtCnt < 1) CmtCnt = 0;
                jsonMap.put("commentCnt", CmtCnt);
                jsonMap.put("hit", isData.get("hit"));
                break;

            case "hit":

                jsonMap.put("commentCnt", isData.get("commentCnt"));
                int HitCnt = (int) isData.get("hit");
                HitCnt = HitCnt + 1;
                jsonMap.put("hit", HitCnt);
                break;


        }
        jsonMap.put("updateAt", isData.get("updateAt"));
        jsonMap.put("createAt", isData.get("createAt"));

        jsonMap.put("notice", isData.get("notice"));
        jsonMap.put("replyUse", isData.get("replyUse"));
        jsonMap.put("userNotice", isData.get("userNotice"));
        jsonMap.put("open", isData.get("open"));


        jsonMap.put("dfileNames", isData.get("dfileNames"));
        jsonMap.put("dfileOrgNames", isData.get("dfileOrgNames"));
        jsonMap.put("keywords", isData.get("keywords"));
        jsonMap.put("imgs", isData.get("imgs"));
        jsonMap.put("orgImgNames", isData.get("orgImgNames"));
        jsonMap.put("searchDfileNames", isData.get("searchDfileNames"));
        jsonMap.put("searchImgNames", isData.get("searchImgNames"));


        IndexRequest request = new IndexRequest(boardArticleIndex)
                .id(id)
                .source(jsonMap);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

    }

    /**
     * @ 게시글 삭제
     * params: String id
     * return:
     **/
    public void delete(String id) throws IOException {

        DeleteRequest deleteDocumentRequest = new DeleteRequest(boardArticleIndex, id); //Index name followed by the ID.
        DeleteResponse deleteResponse = client.delete(deleteDocumentRequest, RequestOptions.DEFAULT);

    }

    /**
     * @ 게시글 게시판아이디에 따른 글 총 갯수
     * params: bid
     * return:
     **/
    public Long getArticleTotalByBid(String bid) throws IOException {

        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(termQuery("bid", bid));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        long total = totalHits.value;
        return total;
    }

    /**
     * @ 게시글 전체 문서 검색
     * params: {
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * }
     * return:
     **/
    public HashMap<String, Object> articleSearch(HashMap<String, Object> params) throws IOException {

        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery();

        List<String> searchBids = (List<String>) params.get("searchBids");
        if (searchBids.size() > 0) {
            BoolQueryBuilder bidQuery = QueryBuilders.boolQuery();
            for (String bid : searchBids) {
                ((BoolQueryBuilder) bidQuery).should(termQuery("bid", bid));
            }
            ((BoolQueryBuilder) qb).must(bidQuery);
        }
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {

            BoolQueryBuilder subContQuery = QueryBuilders.boolQuery();

            BoolQueryBuilder subjectQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder contentQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();

            String[] keywords = keyword.split(" ");

            for (String key : keywords) {
                if (!key.isEmpty()) {
                    ((BoolQueryBuilder) subjectQuery).must(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                    ((BoolQueryBuilder) contentQuery).must(QueryBuilders.matchPhrasePrefixQuery("content", key));
                    ((BoolQueryBuilder) keywordQuery).must(QueryBuilders.matchPhrasePrefixQuery("keywords", key));
                }
            }
            ((BoolQueryBuilder) subContQuery).should(subjectQuery);
            ((BoolQueryBuilder) subContQuery).should(contentQuery);
            ((BoolQueryBuilder) subContQuery).should(keywordQuery);
            ((BoolQueryBuilder) qb).must(subContQuery);
        }
        SearchSourceBuilder sourceBuilder = pagingSort(params);
        sourceBuilder.query(qb);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("subject");
        highlightTitle.highlighterType("unified");
        highlightBuilder.field(highlightTitle);

        HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content");
        highlightBuilder.field(highlightContent);
        sourceBuilder.highlighter(highlightBuilder);

        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return getHighlightList(searchResponse);
    }


    /**
     * @ 게시글 전체 이미지 검색
     * params: {
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * }
     * return:
     **/
    public HashMap<String, Object> articleImageSearch(HashMap<String, Object> params) throws IOException {

        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery();

        List<String> searchBids = (List<String>) params.get("searchBids");
        if (searchBids.size() > 0) {
            BoolQueryBuilder bidQuery = QueryBuilders.boolQuery();
            for (String bid : searchBids) {
                ((BoolQueryBuilder) bidQuery).should(termQuery("bid", bid));
            }
            ((BoolQueryBuilder) qb).must(bidQuery);
        }
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {

            BoolQueryBuilder imgQuery = QueryBuilders.boolQuery();
            String[] keywords = keyword.split(" ");
            for (String key : keywords) {
                if (!key.isEmpty()) {
                    ((BoolQueryBuilder) imgQuery).must(QueryBuilders.matchPhrasePrefixQuery("searchImgNames", key));

                }
            }
            ((BoolQueryBuilder) qb).must(imgQuery);
        }
        SearchSourceBuilder sourceBuilder = pagingSort(params);
        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return getList(searchResponse);
    }

    /**
     * @ 게시글 전체 파일 검색
     * params: {
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * }
     * return:
     **/
    public HashMap<String, Object> articleFileSearch(HashMap<String, Object> params) throws IOException {

        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery();

        List<String> searchBids = (List<String>) params.get("searchBids");
        if (searchBids.size() > 0) {
            BoolQueryBuilder bidQuery = QueryBuilders.boolQuery();
            for (String bid : searchBids) {
                ((BoolQueryBuilder) bidQuery).should(termQuery("bid", bid));
            }
            ((BoolQueryBuilder) qb).must(bidQuery);
        }
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {

            BoolQueryBuilder fileQuery = QueryBuilders.boolQuery();
            String[] keywords = keyword.split(" ");
            for (String key : keywords) {
                if (!key.isEmpty()) {
                    ((BoolQueryBuilder) fileQuery).must(QueryBuilders.matchPhrasePrefixQuery("searchDfileNames", key));
                }
            }
            ((BoolQueryBuilder) qb).must(fileQuery);
        }
        SearchSourceBuilder sourceBuilder = pagingSort(params);
        sourceBuilder.query(qb);

        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        return getList(searchResponse);
    }
}
