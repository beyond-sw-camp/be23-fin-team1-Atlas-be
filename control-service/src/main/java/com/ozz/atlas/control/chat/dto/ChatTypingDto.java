package com.ozz.atlas.control.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatTypingDto {
    private String roomPublicId;
    private String userPublicId;
    private boolean isTyping;
}
