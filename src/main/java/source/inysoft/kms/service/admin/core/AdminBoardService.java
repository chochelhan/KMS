package source.inysoft.kms.service.admin.core;

import org.springframework.beans.factory.annotation.Autowired;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Repository.customize.CustomizeBoardRepository;
import source.inysoft.kms.opensearch.customize.CustomizeBoardArticleSearch;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class AdminBoardService {

    @Autowired
    CustomizeBoardRepository boardRepository;

    @Autowired
    protected CustomizeBoardArticleSearch customizeBoardArticleSearch;

    /**
     *
     * @게시판 목록
     */
    public HashMap<String, Object> getBoardListAll() {

        HashMap<String, Object> result = new HashMap<String, Object>();
        List<CustomizeBoard> resultData = boardRepository.findAll();
        result.put("status", "success");
        result.put("data", resultData);
        return result;
    }

    /**
     *
     * @게시판 등록
     */
    @Transactional
    public HashMap<String, Object> insertBoard(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();

        String bid = (String) params.get("bid");
        CustomizeBoard isBoard = boardRepository.getFindByBid(bid);
        if (isBoard != null) {
            result.put("status", "message");
            result.put("data", "isBid");
            return result;
        }

        String bname = (String) params.get("bname");
        String buse = (String) params.get("buse");
        String categoryUse = (String) params.get("categoryUse");
        String categoryList = (String) params.get("categoryList");
        String wauth = (String) params.get("wauth");
        String impt = (String) params.get("impt");

        int brank;
        List<CustomizeBoard> boardList = boardRepository.findAll();
        if (boardList.size()>0) {
            CustomizeBoard rankData = boardRepository.findTop1ByOrderByBrankDesc();
            brank = rankData.getBrank() + 1;
        } else {
            brank = 1;
        }

        CustomizeBoard board = CustomizeBoard.builder()
                .bid(bid)
                .bname(bname)
                .buse(buse)
                .categoryUse(categoryUse)
                .impt(impt)
                .categoryList(categoryList)
                .wauth(wauth)
                .brank(brank)
                .actType("insert")
                .build();

        CustomizeBoard resultData = boardRepository.save(board);

        result.put("status", "success");
        result.put("data", resultData);
        return result;
    }

    /**
     *
     * @게시판 수정
     */
    @Transactional
    public HashMap<String, Object> updateBoard(HashMap<String, Object> params) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String bid = (String) params.get("bid");
        CustomizeBoard isBoard = boardRepository.getFindByBid(bid);
        if (isBoard == null) {
            result.put("status", "fail");
            return result;
        }
        String bname = (String) params.get("bname");
        String buse = (String) params.get("buse");
        String categoryUse = (String) params.get("categoryUse");
        String categoryList = (String) params.get("categoryList");
        String wauth = (String) params.get("wauth");
        String impt = (String) params.get("impt");

        CustomizeBoard board = CustomizeBoard.builder()
                .bid(isBoard.getBid())
                .bname(bname)
                .buse(buse)
                .categoryUse(categoryUse)
                .impt(impt)
                .categoryList(categoryList)
                .wauth(wauth)
                .brank(isBoard.getBrank())
                .actType("update")
                .actId(isBoard.getId())
                .build();

        CustomizeBoard resultData = boardRepository.save(board);

        result.put("status", "success");
        result.put("data", resultData);
        return result;
    }

    /**
     *
     * @게시판 삭제
     */
    @Transactional
    public HashMap<String, Object> deleteBoard(HashMap<String, String> params) throws IOException {

        HashMap<String, Object> result = new HashMap<String, Object>();
        String idString = (String) params.get("id");
        Long id =  Long.parseLong(idString);
        CustomizeBoard isBoard = boardRepository.getFindById(id);
        if(isBoard == null) {
            result.put("status", "fail");
            return result;
        }

        Long total = customizeBoardArticleSearch.getArticleTotalByBid(isBoard.getBid());
        if(total>0) {
            result.put("status", "message");

        } else {
            boardRepository.deleteById(id);
            result.put("status", "success");
        }
        return result;
    }
}
