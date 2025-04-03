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
import org.opensearch.action.support.master.AcknowledgedResponse;
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


public class CommentSearch extends OpenSearch {


    /**
     * @ 댓글 목록
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


        String parentType = (String) params.get("parentType");
        String parentId = (String) params.get("parentId");

        SearchRequest searchRequest = new SearchRequest(commentIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(termQuery("parentId", parentId))
                .must(termQuery("depth", 1))
                .must(termQuery("parentType", parentType));

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

        sourceBuilder.from(page);
        sourceBuilder.size(size);
        sourceBuilder.sort(new FieldSortBuilder("createAt").order(SortOrder.DESC));
        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        long total = totalHits.value;

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("total", total);
        SearchHit[] searchHits = hits.getHits();
        List<Map<String, Object>> articleList = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : searchHits) {
            Map<String, Object> data = hit.getSourceAsMap();
            data.put("id", hit.getId());

                SearchRequest subRequest = new SearchRequest(commentIndex);
                QueryBuilder subQb = QueryBuilders
                        .boolQuery()
                        .must(termQuery("parentId", parentId))
                        .must(termQuery("depth", 2))
                        .must(termQuery("pid", hit.getId()))
                        .must(termQuery("parentType", parentType));

                SearchSourceBuilder subSourceBuilder = new SearchSourceBuilder();
                subSourceBuilder.sort(new FieldSortBuilder("createAt").order(SortOrder.DESC));
                subSourceBuilder.query(subQb);
                subRequest.source(subSourceBuilder);
                SearchResponse subResponse = client.search(subRequest, RequestOptions.DEFAULT);
                SearchHits subHits = subResponse.getHits();
                SearchHit[] subHitList = subHits.getHits();
                List<Map<String, Object>> subList = new ArrayList<Map<String, Object>>();
                for (SearchHit subHit : subHitList) {
                    Map<String, Object> subData = subHit.getSourceAsMap();
                    subData.put("id", subHit.getId());
                    subList.add(subData);
                }
                data.put("subList",subList);
            articleList.add(data);
        }
        result.put("list", articleList);
        return result;
    }


    /**
     * @ 댓글 전체 검색
     * params: {
     * orderByField: 정렬필드, orderBySort: 정렬방법
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * keywordCmd: 검색필드 ,keyword: 검색어
     * dateCmd:일자검색 필드 (updateAt,createAt) ,stdate: 검색시작 (등록일), endate: 검색 종료일(등록일)
     * bid: 게시판 아이디, category:카테고리
     * }
     * return:
     **/
    public HashMap<String, Object> allSearch(HashMap<String, Object> params) throws IOException {


        SearchRequest searchRequest = new SearchRequest(commentIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery();

       if(params.get("searchBids")!=null) {
           List<String> searchBids = (List<String>) params.get("searchBids");
           if (searchBids.size() > 0) {
               BoolQueryBuilder bidQuery = QueryBuilders.boolQuery();
               for (String bid : searchBids) {
                   ((BoolQueryBuilder) bidQuery).should(termQuery("bid", bid));
               }
               ((BoolQueryBuilder) qb).must(bidQuery);
           }
       }
        String keyword = (String) params.get("keyword");
        if (keyword != null && !keyword.isEmpty()) {

            BoolQueryBuilder subContQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder contentQuery = QueryBuilders.boolQuery();

            String[] keywords = keyword.split(" ");
            for (String key : keywords) {
                if (!key.isEmpty()) {
                    ((BoolQueryBuilder) contentQuery).must(QueryBuilders.matchPhrasePrefixQuery("content", key));
                }
            }
            ((BoolQueryBuilder) subContQuery).should(contentQuery);
            ((BoolQueryBuilder) qb).must(subContQuery);
        }

        if (params.get("depth") != null) {
            int depth = (int) params.get("depth");
            ((BoolQueryBuilder) qb).must(QueryBuilders.termQuery("depth",depth));
        }
        if (params.get("parentId") != null) {
            String parentId = (String) params.get("parentId");
            ((BoolQueryBuilder) qb).must(QueryBuilders.termQuery("parentId",parentId));
        }
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();


        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content");
        highlightBuilder.field(highlightContent);
        sourceBuilder.highlighter(highlightBuilder);

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


        sourceBuilder.from(page);
        sourceBuilder.size(size);

        if (params.get("orderByField") != null) {
            String orderByField = (String) params.get("orderByField");
            sourceBuilder.sort(new FieldSortBuilder(orderByField).order(SortOrder.DESC));
        } else {
            sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        }

        sourceBuilder.query(qb);




        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        return getHighlightList(searchResponse);

    }
    /**
     * @ 댓글 정보
     * params: String id
     * return:
     **/
    public HashMap<String, Object> getData(String id) throws IOException {


        GetRequest getRequest = new GetRequest(commentIndex, id);
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("data", response.getSourceAsMap());
        return result;

    }

    /**
     * @ 댓글 저장
     * params:
     * return:
     **/
    public HashMap<String, Object>  insert(HashMap<String, Object> params) throws IOException {

        LocalDateTime now = LocalDateTime.now();
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("parentType", params.get("parentType"));
        jsonMap.put("parentId", params.get("parentId"));
        jsonMap.put("userName", params.get("userName"));
        jsonMap.put("userId", params.get("userId"));
        jsonMap.put("content", params.get("content"));
        jsonMap.put("depth", params.get("depth"));
        jsonMap.put("pid", params.get("pid"));
        jsonMap.put("bid", params.get("bid"));
        jsonMap.put("hit", 0);
        jsonMap.put("createAt", now);
        jsonMap.put("updateAt", now);

        IndexRequest request = new IndexRequest(commentIndex)
                .source(jsonMap);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
        params.put("gid",indexResponse.getId());
        return params;
    }


    /**
     * @ 댓글 수정
     * params:
     * return:
     **/
    public void update(HashMap<String, Object> params, String id) throws IOException {

        GetRequest getRequest = new GetRequest(commentIndex, id);
        GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
        Map<String, Object> isData = response.getSourceAsMap();


        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("parentType", isData.get("parentType"));
        jsonMap.put("parentId", isData.get("parentId"));
        jsonMap.put("userId", isData.get("userId"));
        jsonMap.put("bid",isData.get("bid"));
        jsonMap.put("userName", isData.get("userName"));
        jsonMap.put("depth", isData.get("depth"));
        jsonMap.put("pid", isData.get("pid"));
        jsonMap.put("hit", 0);
        jsonMap.put("content", params.get("content"));
        jsonMap.put("updateAt", now);
        jsonMap.put("createAt", isData.get("createAt"));

        IndexRequest request = new IndexRequest(commentIndex)
                .id(id)
                .source(jsonMap);

        IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);

    }

    /**
     * @ 댓글 삭제
     * params: String id
     * return:
     **/
    public void delete(String id) throws IOException {

        DeleteRequest deleteDocumentRequest = new DeleteRequest(commentIndex, id); //Index name followed by the ID.
        DeleteResponse deleteResponse = client.delete(deleteDocumentRequest, RequestOptions.DEFAULT);

    }

    /**
     * @ parentId 로 삭제시 댓글 목록
     **/
    public void deleteCommentListByParentId(String parentId) throws IOException {


        SearchRequest searchRequest = new SearchRequest(commentIndex);
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(termQuery("parentId", parentId));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.from(0);
        sourceBuilder.size(300);

        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        SearchHits hits = searchResponse.getHits();

        SearchHit[] searchHits = hits.getHits();
        if(searchHits.length > 0) {
            for (SearchHit hit : searchHits) {
                Map<String, Object> data = hit.getSourceAsMap();
                String commentId = (String) hit.getId();

                DeleteRequest deleteDocumentRequest = new DeleteRequest(commentIndex, commentId); //Index name followed by the ID.
                DeleteResponse deleteResponse = client.delete(deleteDocumentRequest, RequestOptions.DEFAULT);
            }
        }
    }
}
