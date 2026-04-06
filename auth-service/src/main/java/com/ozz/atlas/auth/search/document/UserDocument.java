package com.ozz.atlas.auth.search.document;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.UserListDto;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "users")
public class UserDocument {

    @Id
    private Long userId;

    private String publicId;
    private String organizationPublicId;
    private String loginId;
    private String firstName;
    private String middleName;
    private String lastName;
    private String email;
    private UserRole userRole;
    private Status status;
    private String phone;
    private String jobTitle;

    public static UserDocument fromEntity(User user) {
        return UserDocument.builder()
                .userId(user.getUserId())
                .publicId(user.getPublicId())
                .organizationPublicId(user.getOrganization().getPublicId())
                .loginId(user.getLoginId())
                .firstName(user.getFirstName())
                .middleName(user.getMiddleName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .userRole(user.getUserRole())
                .status(user.getStatus())
                .phone(user.getPhone())
                .jobTitle(user.getJobTitle())
                .build();
    }

}
