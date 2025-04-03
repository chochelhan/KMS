package source.inysoft.kms.opensearch.core;


import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;

import org.apache.lucene.search.TotalHits;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.action.support.master.AcknowledgedResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.CreateIndexResponse;
import org.opensearch.common.settings.Settings;
import org.opensearch.common.text.Text;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentFactory;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.fetch.subphase.highlight.HighlightField;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OpenSearch {


    private int port = 9200;
    private String protocol = "http";
    private String url = "localhost";
    private String userId = "admin";
    private String userPassword = "admin";

    protected String boardArticleIndex = "board_article";
    protected String commentIndex = "comment";
    protected RestHighLevelClient client;

    public OpenSearch() {


        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userId, userPassword));

        RestClientBuilder builder = RestClient.builder(new HttpHost(url, port, protocol))
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                        return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        client = new RestHighLevelClient(builder);

    }

    /**
     * @ 인덱스 생성
     **/
    public void createIndex() throws IOException {

       createBoardArticleIndex();
       createCommentIndex();
    }

    /*
    * @게시판 인덱스 생성
    *
     */
    private void createBoardArticleIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(boardArticleIndex);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 2)
                .put("analysis.analyzer.nori.tokenizer", "nori_tokenizer")
        );

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("bid");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();

                builder.startObject("userId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("userName");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();
                builder.startObject("userPassword");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("category");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("subject");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();
                builder.startObject("content");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();

                builder.startObject("dfileNames");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
                builder.startObject("searchDfileNames");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();
                builder.startObject("dfileOrgNames");
                {
                    builder.field("type", "text");
                }
                builder.endObject();

                builder.startObject("keywords");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();

                builder.startObject("imgs");
                {
                    builder.field("type", "text");
                }
                builder.endObject();
                builder.startObject("searchImgNames");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();
                builder.startObject("orgImgNames");
                {
                    builder.field("type", "text");
                }
                builder.endObject();

                builder.startObject("replyUse");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("open");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("userNotice");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("notice");
                {
                    builder.field("type", "keyword");
                }

                builder.endObject();
                builder.startObject("commentCnt");
                {
                    builder.field("type", "integer");
                }
                builder.endObject();
                builder.startObject("hit");
                {
                    builder.field("type", "integer");

                }
                builder.endObject();
                builder.startObject("updateAt");
                {
                    builder.field("type", "date");
                }
                builder.endObject();
                builder.startObject("createAt");
                {
                    builder.field("type", "date");
                }
                builder.endObject();

            }
            builder.endObject();
        }
        builder.endObject();
        request.mapping(builder);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("----------------");
        System.out.println(createIndexResponse);
        System.out.println("----------------");
    }


    /*
     * @게시판 인덱스 생성
     *
     */
    private void createCommentIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(commentIndex);
        request.settings(Settings.builder()
                .put("index.number_of_shards", 5)
                .put("index.number_of_replicas", 2)
                .put("analysis.analyzer.nori.tokenizer", "nori_tokenizer")
        );

        XContentBuilder builder = XContentFactory.jsonBuilder();
        builder.startObject();
        {
            builder.startObject("properties");
            {
                builder.startObject("parentType");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("parentId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("bid");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("depth");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("pid");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("userId");
                {
                    builder.field("type", "keyword");
                }
                builder.endObject();
                builder.startObject("userName");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();
                builder.startObject("content");
                {
                    builder.field("type", "text");
                    builder.field("analyzer", "nori");
                }
                builder.endObject();
                builder.startObject("hit");
                {
                    builder.field("type", "integer");

                }
                builder.endObject();
                builder.startObject("updateAt");
                {
                    builder.field("type", "date");
                }
                builder.endObject();
                builder.startObject("createAt");
                {
                    builder.field("type", "date");
                }
                builder.endObject();

            }
            builder.endObject();
        }
        builder.endObject();
        request.mapping(builder);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("----------------");
        System.out.println(createIndexResponse);
        System.out.println("----------------");
    }
    /**
     * @ 인덱스 삭제
     **/
    public void deleteIndex() throws IOException {

        DeleteIndexRequest boardRequest = new DeleteIndexRequest(boardArticleIndex);
        AcknowledgedResponse deleteIndexResponse = client.indices().delete(boardRequest, RequestOptions.DEFAULT);

        DeleteIndexRequest CommentRequest = new DeleteIndexRequest(commentIndex);
        AcknowledgedResponse deleteCommentResponse = client.indices().delete(CommentRequest, RequestOptions.DEFAULT);

    }
    /**
     * @ 검색 결과 리스트 파싱
     * params: SearchResponse searchResponse
     * return: {total:int,list:[]}
     **/

    protected HashMap<String, Object> getList(SearchResponse searchResponse) {
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
            articleList.add(data);
        }
        result.put("list", articleList);
        return result;
    }

    /**
     * @ 검색 결과 하이라이트(검색어 강조) 리스트 파싱
     * params: SearchResponse searchResponse
     * return: {total:int,list:[]}
     **/

    protected HashMap<String, Object> getHighlightList(SearchResponse searchResponse) {

        SearchHits hits = searchResponse.getHits();
        TotalHits totalHits = hits.getTotalHits();
        long total = totalHits.value;

        HashMap<String, Object> result = new HashMap<String, Object>();
        result.put("total", total);
        SearchHit[] searchHits = hits.getHits();
        List<Map<String, Object>> articleList = new ArrayList<Map<String, Object>>();
        for (SearchHit hit : searchHits) {

            Map<String, Object> data = hit.getSourceAsMap();

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();

            HighlightField subject = highlightFields.get("subject");
            if(subject!=null) {
                Text[] fragmentSubject = subject.fragments();
                data.put("subject", fragmentSubject[0].string());
            }
            HighlightField content = highlightFields.get("content");
            if(content!=null) {
                Text[] fragmentContent = content.fragments();
                data.put("content", fragmentContent[0].string());
            }

            data.put("id", hit.getId());
            data.put("score",hit.getScore());

            articleList.add(data);
        }
        result.put("list", articleList);
        return result;
    }

}