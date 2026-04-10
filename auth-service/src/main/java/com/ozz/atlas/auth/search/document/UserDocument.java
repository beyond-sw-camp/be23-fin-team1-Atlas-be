package com.ozz.atlas.auth.search.document;

import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.common.jpa.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.annotations.Setting;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(indexName = "users")
@Setting(settingPath = "/elasticsearch/user-settings.json")
public class UserDocument {

    @Id
    private Long userId;

    // 외부에 노출하는 사용자 publicId
    private String publicId;

    // 조직 구분 필터링에 사용하는 조직 publicId
    private String organizationPublicId;

    // 로그인 아이디는 정확 검색과 부분검색을 함께 지원한다.
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
    private String loginId;

    // 이름 검색은 일부 문자열만 입력해도 찾을 수 있어야 하므로 ngram 필드를 둔다.
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
    private String firstName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
    private String middleName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
    private String lastName;

    // 이메일도 일부 문자열로 찾을 수 있게 부분검색 필드를 둔다.
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
    private String email;

    // 권한/상태는 정확한 값 필터링이 주 목적이라 기본 매핑으로 둔다.
    private UserRole userRole;
    private Status status;

    // 전화번호와 직책도 통합검색 후보라 부분검색 필드를 둔다.
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
    private String phone;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "user_ngram_analyzer",
                            searchAnalyzer = "user_search_analyzer"
                    )
            }
    )
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
