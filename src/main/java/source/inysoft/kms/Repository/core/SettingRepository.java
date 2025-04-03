package source.inysoft.kms.Repository.core;

import org.springframework.data.jpa.repository.JpaRepository;
import source.inysoft.kms.Entity.customize.CustomizeSetting;

import java.util.List;

public interface SettingRepository extends JpaRepository<CustomizeSetting, Long> {


    public CustomizeSetting getFindById(Long id);


}
