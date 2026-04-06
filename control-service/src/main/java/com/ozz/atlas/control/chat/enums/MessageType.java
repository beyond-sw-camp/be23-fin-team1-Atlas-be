package com.ozz.atlas.control.chat.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessageType {
    TEXT("텍스트"),
    SYSTEM("시스템"),
    REFERENCE("참조카드"),
    FILE("파일"),
    IMAGE("이미지");

    private final String description;
}
