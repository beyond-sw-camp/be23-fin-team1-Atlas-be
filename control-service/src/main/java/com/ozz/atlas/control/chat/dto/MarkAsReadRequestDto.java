package com.ozz.atlas.control.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MarkAsReadRequestDto {
    private String lastReadMessagePublicId;
}