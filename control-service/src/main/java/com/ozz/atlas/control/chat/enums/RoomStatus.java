package com.ozz.atlas.control.chat.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoomStatus {
    OPEN("활성"),
    CLOSED("비활성");

    private final String description;
}
