package com.ozz.atlas.control.chat.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InviteParticipantsDto {
    private String inviterPublicId;
    private List<String> targetUserPublicIds;
}
