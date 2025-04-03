package source.inysoft.kms.service.api.core;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import source.inysoft.kms.Entity.common.Role;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;;
import source.inysoft.kms.handler.MailHandler;

import java.util.List;


@Service
@AllArgsConstructor
public class MailService {

    @Autowired
    CustomizeMemberRepository memberRepository;

    private JavaMailSender mailSender;

    public void mailSend(String to, String subject, String content) {

        List<CustomizeMember> adminMmember = memberRepository.findByRole(Role.ROLE_ADMIN);
        String adminEmail = "";
        if(adminMmember.size()>0) {
            adminEmail = adminMmember.get(0).getEmail();
            try {
                MailHandler mailHandler = new MailHandler(mailSender);

                // 받는 사람
                mailHandler.setTo(to);
                // 보내는 사람
                mailHandler.setFrom(adminEmail);
                // 제목
                mailHandler.setSubject(subject);
                // HTML Layout
                mailHandler.setText(content, true);
                // 첨부 파일
                //mailHandler.setAttach("newTest.txt", "static/originTest.txt");
                // 이미지 삽입
                //mailHandler.setInline("sample-img", "static/sample1.jpg");
                mailHandler.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


}
