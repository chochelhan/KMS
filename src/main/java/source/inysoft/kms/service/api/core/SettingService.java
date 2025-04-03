package source.inysoft.kms.service.api.core;

import org.springframework.beans.factory.annotation.Autowired;
import source.inysoft.kms.Entity.customize.CustomizeSetting;
import source.inysoft.kms.Repository.customize.CustomizeSettingRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class SettingService {

    @Autowired
    CustomizeSettingRepository settingRepository;


    /**
     * @설정 정보
     */
    public HashMap<String, Object> getSetting()  {


        HashMap<String, Object> result = new HashMap<String, Object>();
        List<CustomizeSetting> settingList = settingRepository.findAll();
        if (settingList.size() > 0) {
            CustomizeSetting setting = settingRepository.getFindById(settingList.get(0).getId());
            result.put("setting", setting);
        }
        result.put("status", "success");

        return result;
    }


}
