package com.ozz.atlas.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CredentialMailService {

    // 스프링이 제공하는 메일 발송 객체입니다.
    private final JavaMailSender mailSender;

    // 설정 파일에 적어둔 발신자 주소입니다.
    private final String fromAddress;

    public CredentialMailService(
            JavaMailSender mailSender,
            @Value("${atlas.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    // 계정 생성 후 로그인 정보와 임시 비밀번호를 메일로 보냅니다.
    public void sendTemporaryCredentialMail(
            String toEmail,
            String organizationName,
            String loginId,
            String temporaryPassword
    ) {
        // 메일 제목입니다.
        String subject = "[Atlas] 계정이 생성되었습니다.";

        // 메일 본문입니다.
        String text = """
                안녕하세요.

                Atlas 계정이 생성되었습니다.

                조직명: %s
                로그인 ID: %s
                임시 비밀번호: %s

                첫 로그인 후 반드시 비밀번호를 변경해 주세요.
                """.formatted(
                organizationName,
                loginId,
                temporaryPassword
        );

        // 단순 텍스트 메일 객체를 만듭니다.
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        // 실제 메일을 발송합니다.
        mailSender.send(message);
    }
}
