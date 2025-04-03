package source.inysoft.kms.Repository.core;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import source.inysoft.kms.Entity.customize.CustomizeBoard;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BoardRepository extends JpaRepository<CustomizeBoard, Long> {


    public CustomizeBoard getFindByBid(String bid);

    public CustomizeBoard getFindById(Long id);

    public CustomizeBoard findTop1ByOrderByBrankDesc();

    public List<CustomizeBoard> getFindByBuse(String buse);


    public List<CustomizeBoard> getFindByBuseAndImpt(String buse,String impt);

}
