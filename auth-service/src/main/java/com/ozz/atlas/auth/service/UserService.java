package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.dtos.UserSignUpDto;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, OrganizationRepository organizationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
    }
//    사용자 회원가입
    public String signup(UserSignUpDto dto){
        if (userRepository.existsByLoginId(dto.getLoginId())){
            throw new IllegalArgumentException("이미 사용중인 아이디 에용");
        }
        Organization organization = organizationRepository.findByPublicId(dto.getOrganizationPublicId()).
                orElseThrow(()-> new IllegalArgumentException("존재하지 않는 조직"));

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(organization, encodedPassword);

        User savedUser = userRepository.save(user);
        return savedUser.getPublicId();
    }
}
