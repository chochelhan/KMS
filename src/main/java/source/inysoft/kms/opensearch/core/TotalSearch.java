package source.inysoft.kms.opensearch.core;


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


public class TotalSearch extends OpenSearch {

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

            BoolQueryBuilder query = QueryBuilders.boolQuery();
            BoolQueryBuilder keywordQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder subjectQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder contentQuery = QueryBuilders.boolQuery();
           // BoolQueryBuilder imgQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder fileQuery = QueryBuilders.boolQuery();

            switch (matchType) {
                case "and":
                case "or":
                    String[] keywords = keyword.split(" ");
                    for (String key : keywords) {
                        if (!key.isEmpty()) {
                            if (matchType.equals("and")) {
                                if (keywordCmd.equals("all") || keywordCmd.equals("subject")) {
                                    ((BoolQueryBuilder) subjectQuery).must(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("content")  || keywordCmd.equals("comment")) {
                                    ((BoolQueryBuilder) contentQuery).must(QueryBuilders.matchPhrasePrefixQuery("content", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("subject") || keywordCmd.equals("content")) {
                                    ((BoolQueryBuilder) keywordQuery).must(QueryBuilders.matchPhrasePrefixQuery("keywords", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("file")) {
                                    //((BoolQueryBuilder) imgQuery).must(QueryBuilders.matchPhrasePrefixQuery("searchImgNames", key));
                                    ((BoolQueryBuilder) fileQuery).must(QueryBuilders.matchPhrasePrefixQuery("searchDfileNames", key));
                                }

                            } else {
                                if (keywordCmd.equals("all") || keywordCmd.equals("subject")) {
                                    ((BoolQueryBuilder) subjectQuery).should(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("content")  || keywordCmd.equals("comment")) {
                                    ((BoolQueryBuilder) contentQuery).should(QueryBuilders.matchPhrasePrefixQuery("content", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("subject") || keywordCmd.equals("content")) {
                                    ((BoolQueryBuilder) keywordQuery).should(QueryBuilders.matchPhrasePrefixQuery("keywords", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("file")) {
                                   // ((BoolQueryBuilder) imgQuery).should(QueryBuilders.matchPhrasePrefixQuery("searchImgNames", key));
                                    ((BoolQueryBuilder) fileQuery).should(QueryBuilders.matchPhrasePrefixQuery("searchDfileNames", key));
                                }

                            }
                        }
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("subject")) {
                        ((BoolQueryBuilder) query).should(subjectQuery);
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("content")  || keywordCmd.equals("comment")) {
                        ((BoolQueryBuilder) query).should(contentQuery);
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("subject") || keywordCmd.equals("content")) {
                        ((BoolQueryBuilder) query).should(keywordQuery);
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("file")) {
                       // ((BoolQueryBuilder) query).should(imgQuery);
                        ((BoolQueryBuilder) query).should(fileQuery);
                    }
                    ((BoolQueryBuilder) qb).must(query);
                    break;
                case "not":
                    String[] notKeywords = keyword.split("NOT");
                    if (!notKeywords[0].isEmpty()) {
                        String[] inkeywords = notKeywords[0].split(" ");
                        for (String key : inkeywords) {
                            if (!key.isEmpty()) {
                                if (keywordCmd.equals("all") || keywordCmd.equals("subject")) {
                                    ((BoolQueryBuilder) subjectQuery).should(QueryBuilders.matchPhrasePrefixQuery("subject", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("content")  || keywordCmd.equals("comment")) {
                                    ((BoolQueryBuilder) contentQuery).should(QueryBuilders.matchPhrasePrefixQuery("content", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("subject") || keywordCmd.equals("content")) {
                                    ((BoolQueryBuilder) keywordQuery).should(QueryBuilders.matchPhrasePrefixQuery("keywords", key));
                                }
                                if (keywordCmd.equals("all") || keywordCmd.equals("file")) {
                                   // ((BoolQueryBuilder) imgQuery).should(QueryBuilders.matchPhrasePrefixQuery("searchImgNames", key));
                                    ((BoolQueryBuilder) fileQuery).should(QueryBuilders.matchPhrasePrefixQuery("searchDfileNames", key));
                                }
                            }
                        }

                    }
                    int k = 0;
                    for (String notKey : notKeywords) {
                        if (k > 0 && !notKey.isEmpty()) {
                            if (keywordCmd.equals("all") || keywordCmd.equals("subject")) {
                                ((BoolQueryBuilder) subjectQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("subject", notKey));
                            }
                            if (keywordCmd.equals("all") || keywordCmd.equals("content")  || keywordCmd.equals("comment")) {
                                ((BoolQueryBuilder) contentQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("content", notKey));
                            }
                            if (keywordCmd.equals("all") || keywordCmd.equals("subject") || keywordCmd.equals("content")) {
                                ((BoolQueryBuilder) keywordQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("keywords", notKey));
                            }
                            if (keywordCmd.equals("all") || keywordCmd.equals("file")) {
                                //((BoolQueryBuilder) imgQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("searchImgNames", notKey));
                                ((BoolQueryBuilder) fileQuery).mustNot(QueryBuilders.matchPhrasePrefixQuery("searchDfileNames", notKey));
                            }
                        }
                        k++;
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("subject")) {
                        ((BoolQueryBuilder) query).should(subjectQuery);
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("content")  || keywordCmd.equals("comment")) {
                        ((BoolQueryBuilder) query).should(contentQuery);
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("subject") || keywordCmd.equals("content")) {
                        ((BoolQueryBuilder) query).should(keywordQuery);
                    }
                    if (keywordCmd.equals("all") || keywordCmd.equals("file")) {
                       // ((BoolQueryBuilder) query).should(imgQuery);
                        ((BoolQueryBuilder) query).should(fileQuery);
                    }
                    ((BoolQueryBuilder) qb).must(query);
                    break;
            }
            ((BoolQueryBuilder) qb).must(query);
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

        if (orderByField == null || orderByField.isEmpty()) {
            orderByField = "createAt";
        }
        if (orderBySort == null || orderBySort.isEmpty()) {
            orderBySort = "desc";
        }

        sourceBuilder.from(page);
        sourceBuilder.size(size);
        if(!orderByField.equals("default")) {
            if (orderBySort.equals("asc")) {
                sourceBuilder.sort(new FieldSortBuilder(orderByField).order(SortOrder.ASC));
            } else {
                sourceBuilder.sort(new FieldSortBuilder(orderByField).order(SortOrder.DESC));
            }
        } else {
            sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
        }

        return sourceBuilder;
    }

    /**
     * @ 게시글,댓글 검색
     * params: {
     * orderByField: 정렬필드, orderBySort: 정렬방법
     * limit: 한페이지당 목록수 ,page: 시작 페이지
     * keywordCmd: 검색필드 ,keyword: 검색어
     * dateCmd:일자검색 필드 (updateAt,createAt) ,stdate: 검색시작 (등록일), endate: 검색 종료일(등록일)
     * bid: 게시판 아이디, category:카테고리
     * }
     * return:
     **/
    public HashMap<String, Object> totalSearch(HashMap<String, Object> params) throws IOException {

        String keywordCmd = (String) params.get("keywordCmd");
        SearchRequest searchRequest = new SearchRequest(boardArticleIndex, commentIndex);
        if(keywordCmd.equals("comment")) {
            searchRequest = new SearchRequest(commentIndex);
        }
        QueryBuilder qb = QueryBuilders.boolQuery();

        List<String> searchBids = (List<String>) params.get("searchBids");
        if (searchBids.size() > 0) {
            BoolQueryBuilder bidQuery = QueryBuilders.boolQuery();
            for (String bid : searchBids) {
                ((BoolQueryBuilder) bidQuery).should(termQuery("bid", bid));
            }
            ((BoolQueryBuilder) qb).must(bidQuery);
        }
        qb = queryKeyword(params, qb);
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
     * @ 게시글,댓글 중 게시판 1개에서 검색
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

        String bid = (String) params.get("bid");
        String keywordCmd = (String) params.get("keywordCmd");
        if(keywordCmd==null || keywordCmd.isEmpty()) {
            keywordCmd = "all";
        }
        SearchRequest searchRequest = new SearchRequest(boardArticleIndex, commentIndex);
        if(keywordCmd.equals("comment")) {
            searchRequest = new SearchRequest(commentIndex);
        }

        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(termQuery("bid", bid));

        if(!keywordCmd.equals("comment")) {
            String category = (String) params.get("category");
            if (category != null && !category.isEmpty()) {
                ((BoolQueryBuilder) qb).must(termQuery("category", category));
            }
        }
        qb = queryKeyword(params, qb);

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
     * @ 게시글,댓글 중 게시판 1개에서 검색시 토탈 갯수(키워드 통계를 위한 부분)
     * params:
     * return:
     **/
    public Long searchTotal(HashMap<String, Object> params) throws IOException {

        String bid = (String) params.get("bid");
        String keywordCmd = (String) params.get("keywordCmd");
        List<String> bidList = (List<String>)  params.get("bidList");
        if(keywordCmd==null || keywordCmd.isEmpty()) {
            keywordCmd = "all";
        }
        SearchRequest searchRequest = new SearchRequest(boardArticleIndex, commentIndex);
        if(keywordCmd.equals("comment")) {
            searchRequest = new SearchRequest(commentIndex);
        }
        QueryBuilder qb = QueryBuilders
                .boolQuery()
                .must(termQuery("bid", bid));

        if(!keywordCmd.equals("comment")) {
            String category = (String) params.get("category");
            if (category != null && !category.isEmpty()) {
                ((BoolQueryBuilder) qb).must(termQuery("category", category));
            }
        }
        qb = queryKeyword(params, qb);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(qb);
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        long total = totalHits.value;
        return total;
    }
}