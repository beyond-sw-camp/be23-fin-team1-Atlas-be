package com.ozz.atlas.auth.search.document;

import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.OrganizationType;
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
@Document(indexName = "organizations")
@Setting(settingPath = "/elasticsearch/organization-settings.json")
public class OrganizationDocument {

    @Id
    private Long organizationId;

    // 외부에 노출하는 조직 publicId
    private String publicId;

    // 조직 유형은 정확한 값 필터링이 주 목적이라 기본 매핑
    private OrganizationType organizationType;

    // 조직명은 통합검색과 부분검색을 모두 지원해야 하므로 ngram 필드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String organizationName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String organizationEnglishName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String organizationAlias;

    // 사업자번호도 일부 숫자만으로 찾을 수 있게 부분검색 필드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String businessNo;

    // 담당자 이름 계열도 일부 문자열만 입력해도 찾을 수 있게 ngram 필드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String contactFirstName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String contactMiddleName;

    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String contactLastName;

    // 담당자 이메일도 일부 문자열 검색이 가능해야 해서 ngram 필드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String contactEmail;

    // 담당자 전화번호도 일부 숫자 검색을 위해 ngram 필드
    @MultiField(
            mainField = @Field(type = FieldType.Text),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword),
                    @InnerField(
                            suffix = "ngram",
                            type = FieldType.Text,
                            analyzer = "organization_ngram_analyzer",
                            searchAnalyzer = "organization_search_analyzer"
                    )
            }
    )
    private String contactPhone;

    // 상태는 ACTIVE, DELETE 같은 정확한 값 필터링이 목적
    private Status status;


    public static OrganizationDocument fromEntity(Organization organization) {
        return OrganizationDocument.builder()
                .organizationId(organization.getOrganizationId())
                .publicId(organization.getPublicId())
                .organizationType(organization.getOrganizationType())
                .organizationName(organization.getOrganizationName())
                .organizationEnglishName(organization.getOrganizationEnglishName())
                .organizationAlias(organization.getOrganizationAlias())
                .businessNo(organization.getBusinessNo())
                .contactFirstName(organization.getContactFirstName())
                .contactMiddleName(organization.getContactMiddleName())
                .contactLastName(organization.getContactLastName())
                .contactEmail(organization.getContactEmail())
                .contactPhone(organization.getContactPhone())
                .status(organization.getStatus())
                .build();
    }
}
