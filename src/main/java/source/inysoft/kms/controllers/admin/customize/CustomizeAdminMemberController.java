package source.inysoft.kms.controllers.admin.customize;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import source.inysoft.kms.controllers.admin.core.AdminMemberController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.HashMap;


@RestController
@RequestMapping("/api/admin/controller/member/")
public class CustomizeAdminMemberController extends AdminMemberController {

}
