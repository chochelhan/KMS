package source.inysoft.kms.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.User;
import source.inysoft.kms.Entity.customize.CustomizeMember;
import source.inysoft.kms.Repository.customize.CustomizeMemberRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    CustomizeMemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<CustomizeMember> findMember = memberRepository.findByUid(username);
        if (!findMember.isPresent()) throw new UsernameNotFoundException("존재하지 않는 username 입니다.");

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        return new User(findMember.get().getUid(), encoder.encode(findMember.get().getPasswd()), AuthorityUtils.createAuthorityList());

    }


}


