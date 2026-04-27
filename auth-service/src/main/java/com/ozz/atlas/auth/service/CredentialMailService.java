package com.ozz.atlas.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CredentialMailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public CredentialMailService(
            JavaMailSender mailSender,
            @Value("${atlas.mail.from}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    // 계정 생성 후 로그인 정보와 임시 비밀번호를 메일로 보냄
    public void sendTemporaryCredentialMail(
            String toEmail,
            String organizationName,
            String loginId,
            String temporaryPassword
    ) {
        // 메일 제목
        String subject = "[Atlas] 계정이 생성되었습니다.";

        // 메일 본문
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

        // 단순 텍스트 메일 객체를 만듬
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        // 실제 메일을 발송
        mailSender.send(message);
    }

    // 새 IP 로그인 시 이메일 인증 코드를 보냄
    public void sendLoginVerificationMail(
            String toEmail,
            String loginId,
            String verificationCode,
            int expireMinutes
    ) {
        // 메일 제목
        String subject = "[Atlas] 새 로그인 IP 인증 코드 안내";

        // 메일 본문
        String text = """
                안녕하세요.

                새로운 IP 에서 로그인 시도가 감지되었습니다.

                로그인 ID: %s
                인증 코드: %s

                이 코드는 %d분 동안만 유효합니다.
                본인이 요청한 것이 아니라면 이 메일을 무시해 주세요.
                """.formatted(
                loginId,
                verificationCode,
                expireMinutes
        );

        // 단순 텍스트 메일을 생성
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        // 메일을 실제로 발송
        mailSender.send(message);
    }

    // 비밀번호 변경 인증코드를 이메일로 보냄
    public void sendPasswordChangeVerificationMail(
            String toEmail,
            String loginId,
            String verificationCode,
            int expireMinutes
    ) {
        // 메일 제목
        String subject = "[Atlas] 비밀번호 변경 인증코드 안내";

        // 메일 본문
        String text = """
            안녕하세요.

            Atlas 비밀번호 변경 인증 요청이 접수되었습니다.

            로그인 ID: %s
            인증코드: %s

            이 인증코드는 %d분 동안만 유효합니다.
            본인이 요청한 것이 아니라면 이 메일을 무시해 주세요.
            """.formatted(
                loginId,
                verificationCode,
                expireMinutes
        );

        // 단순 텍스트 메일 객체를 만듬
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(text);

        // 실제 메일을 발송
        mailSender.send(message);
    }


}
