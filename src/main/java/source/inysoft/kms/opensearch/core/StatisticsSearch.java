package source.inysoft.kms.opensearch.core;


import org.apache.lucene.search.TotalHits;
import org.opensearch.action.search.MultiSearchRequest;
import org.opensearch.action.search.MultiSearchResponse;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opensearch.index.query.QueryBuilders.rangeQuery;
import static org.opensearch.index.query.QueryBuilders.termQuery;


public class StatisticsSearch extends OpenSearch {


    /**
     * @ 계시판별 활동 통계
     * return:
     **/
    public Map<String, Long> getArticleStatistics(HashMap<String, Object> params) throws IOException {

        MultiSearchRequest request = new MultiSearchRequest();
        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);

        String bid = (String) params.get("bid");
        QueryBuilder qb = QueryBuilders.boolQuery().must(termQuery("bid", bid));

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        String stdate = (String) params.get("stdate");
        String endate = (String) params.get("endate");
        if (stdate != null && !stdate.isEmpty() && endate != null && !endate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
            String stdatetime = stdate + " 00:00:01.111";
            String endatetime = endate + " 23:59:59.111";

            LocalDateTime stDateTime = LocalDateTime.parse(stdatetime, formatter);
            LocalDateTime enDateTime = LocalDateTime.parse(endatetime, formatter);
            ((BoolQueryBuilder) qb).must(rangeQuery("createAt").gte(stDateTime));
            ((BoolQueryBuilder) qb).must(rangeQuery("createAt").lte(enDateTime));
        }

        SearchRequest commentRequest = new SearchRequest(commentIndex);

        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);
        request.add(searchRequest);

        commentRequest.source(sourceBuilder);
        request.add(commentRequest);

        MultiSearchResponse response = client.msearch(request, RequestOptions.DEFAULT);
        MultiSearchResponse.Item firstResponse = response.getResponses()[0];
        SearchResponse articleResponse = firstResponse.getResponse();

        MultiSearchResponse.Item secondResponse = response.getResponses()[1];
        SearchResponse commentResponse = secondResponse.getResponse();


        Map<String, Long> result = new HashMap<String, Long>();
        result.put("article", articleResponse.getHits().getTotalHits().value);
        result.put("comment", commentResponse.getHits().getTotalHits().value);

        return result;
    }


    /**
     * @ 계시판 총 글수
     * return:
     **/
    public Long getArticleTotal() throws IOException {

        SearchRequest searchRequest = new SearchRequest(boardArticleIndex);
        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);


        return searchResponse.getHits().getTotalHits().value;
    }


}