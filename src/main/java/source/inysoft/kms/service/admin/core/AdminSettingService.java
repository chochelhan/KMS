package source.inysoft.kms.service.admin.core;

import org.springframework.beans.factory.annotation.Autowired;
import source.inysoft.kms.Entity.customize.CustomizeBoard;
import source.inysoft.kms.Entity.customize.CustomizeSetting;
import source.inysoft.kms.Repository.customize.CustomizeBoardRepository;
import source.inysoft.kms.Repository.customize.CustomizeSettingRepository;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

public class AdminSettingService {

    @Autowired
    CustomizeBoardRepository boardRepository;

    @Autowired
    CustomizeSettingRepository settingRepository;


    /**
     * @설정 정보
     */
    public HashMap<String, Object> getSettingByType(String type) {

        HashMap<String, Object> result = new HashMap<String, Object>();
        if (type.equals("menu")) {
            List<CustomizeBoard> resultData = boardRepository.getFindByBuse("yes");
            result.put("boardList", resultData);

        }
        List<CustomizeSetting> settingList = settingRepository.findAll();
        if (settingList.size() > 0) {
            CustomizeSetting setting = settingRepository.getFindById(settingList.get(0).getId());
            result.put("setting", setting);
        }
        result.put("status", "success");

        return result;
    }


    /**
     * @설정 등록
     */
    @Transactional
    public HashMap<String, Object> checkUpdateSetting(HashMap<String, String> params, String type) {

        HashMap<String, Object> result = new HashMap<String, Object>();


        List<CustomizeSetting> settingList = settingRepository.findAll();
        String menu = "[]";
        String sns = "{}";
        String agree = "{}";
        String email = "{}";

        if (settingList.size() > 0) {
            CustomizeSetting isSetting = settingRepository.getFindById(settingList.get(0).getId());
            switch (type) {
                case "menu":
                    menu = params.get("menu");
                    sns = isSetting.getSns();
                    agree = isSetting.getAgree();
                    email = isSetting.getEmail();
                    break;
                case "sns":
                    sns = params.get("sns");
                    menu = isSetting.getMenu();
                    agree = isSetting.getAgree();
                    email = isSetting.getEmail();
                    break;
                case "agree":
                    menu = isSetting.getMenu();
                    sns = isSetting.getSns();
                    email = isSetting.getEmail();
                    agree = params.get("agree");
                    break;
                case "email":
                    menu = isSetting.getMenu();
                    sns = isSetting.getSns();
                    agree = isSetting.getAgree();
                    email = params.get("email");
                    break;

            }
            CustomizeSetting setting = CustomizeSetting.builder()
                    .sns(sns)
                    .menu(menu)
                    .agree(agree)
                    .email(email)
                    .actType("update")
                    .actId(settingList.get(0).getId())
                    .build();

            CustomizeSetting resultData = settingRepository.save(setting);
            result.put("data", resultData);
        } else {
            switch (type) {
                case "menu":
                    menu = params.get("menu");
                    break;
                case "sns":
                    sns = params.get("sns");
                    break;
                case "agree":
                    agree = params.get("agree");
                    break;
                case "email":
                    email = params.get("email");
                    break;

            }
            CustomizeSetting setting = CustomizeSetting.builder()
                    .sns(sns)
                    .menu(menu)
                    .agree(agree)
                    .email(email)
                    .actType("insert")
                    .build();
            CustomizeSetting resultData = settingRepository.save(setting);
            result.put("data", resultData);
        }


        result.put("status", "success");

        return result;
    }



}
