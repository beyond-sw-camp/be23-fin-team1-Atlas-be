package com.ozz.atlas.auth.service;

import com.ozz.atlas.auth.common.config.AuthPrincipal;
import com.ozz.atlas.auth.common.token.JwtTokenProvider;
import com.ozz.atlas.auth.domain.Department;
import com.ozz.atlas.auth.domain.Organization;
import com.ozz.atlas.auth.domain.User;
import com.ozz.atlas.auth.domain.UserRole;
import com.ozz.atlas.auth.dtos.user.*;
import com.ozz.atlas.auth.repository.DepartmentRepository;
import com.ozz.atlas.auth.repository.OrganizationRepository;
import com.ozz.atlas.auth.repository.UserRepository;
import com.ozz.atlas.auth.search.dtos.UserSearchDto;
import com.ozz.atlas.auth.search.service.UserSearchService;
import com.ozz.atlas.common.excel.ExcelUtils;
import com.ozz.atlas.common.jpa.Status;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.ozz.atlas.auth.dtos.user.UserDetailDto;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserSearchService userSearchService;
    private final SecurityHistoryService securityHistoryService;
    // 계정 생성 후 로그인 정보 메일을 보내는 서비스
    private final CredentialMailService credentialMailService;

    @Autowired
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            OrganizationRepository organizationRepository,
            DepartmentRepository departmentRepository,
            JwtTokenProvider jwtTokenProvider,
            UserSearchService userSearchService,
            SecurityHistoryService securityHistoryService,
            CredentialMailService credentialMailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.organizationRepository = organizationRepository;
        this.departmentRepository = departmentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userSearchService = userSearchService;
        this.securityHistoryService = securityHistoryService;
        this.credentialMailService = credentialMailService;
    }

    // 관리자가 조직의 최초 ORG_ADMIN 계정을 생성
    // 임시 비밀번호는 서버가 생성해서 응답으로 내려줌
    public ProvisionedUserResponseDto createInitialOrgAdmin(
            String organizationPublicId,
            OrganizationUserCreateDto dto
    ) {
        // 조직이 없으면 대표자 계정을 만들 수 없음
        Organization organization = organizationRepository.findByPublicId(organizationPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));
        // 조직 영문명을 기준으로 최초 ORG_ADMIN 로그인 ID를 자동 생성합니다.
        String generatedLoginId = generateInitialOrgAdminLoginId(organization);

        // 해당 조직에 활성 상태 ORG_ADMIN 이 이미 있으면 최초 관리자 생성을 막음
        boolean orgAdminExists = userRepository.existsByOrganization_PublicIdAndUserRoleAndStatus(
                organizationPublicId,
                UserRole.ORG_ADMIN,
                Status.ACTIVE
        );

        if (orgAdminExists) {
            throw new IllegalArgumentException("해당 조직에는 이미 대표자 계정이 존재합니다.");
        }

        // 최초 로그인용 임시 비밀번호를 생성
        String temporaryPassword = createTemporaryPassword();

        // DB에는 암호화된 비밀번호를 저장
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        // 최초 대표자 계정을 생성
        User user = User.builder()
                .organization(organization)
                .loginId(generatedLoginId)
                .password(encodedPassword)
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .userRole(UserRole.ORG_ADMIN)
                .status(Status.ACTIVE)
                .passwordChangeRequired(true)
                .build();

        User savedUser = userRepository.save(user);
        userSearchService.saveUserDocument(savedUser);

        // 방금 생성한 대표자 계정 정보를 이메일로 보냄
        credentialMailService.sendTemporaryCredentialMail(
                savedUser.getEmail(),
                organization.getOrganizationName(),
                savedUser.getLoginId(),
                temporaryPassword
        );

        return ProvisionedUserResponseDto.builder()
                .userPublicId(savedUser.getPublicId())
                .organizationPublicId(organization.getPublicId())
                .loginId(savedUser.getLoginId())
                .temporaryPassword(temporaryPassword)
                .passwordChangeRequired(true)
                .build();
    }

    // 조직 관리자가 자기 조직의 일반 직원 계정을 생성
    // 조직 정보는 로그인한 ORG_ADMIN 의 토큰에서 가져옴
    public ProvisionedUserResponseDto createOrganizationUser(
            OrganizationUserCreateDto dto,
            AuthPrincipal principal
    ) {
        // ORG_ADMIN 만 직원 계정을 만들 수 있음
        if (principal.role() != UserRole.ORG_ADMIN) {
            throw new IllegalArgumentException("직원 계정 생성 권한이 없습니다.");
        }

        // 현재 로그인한 대표자의 조직을 기준으로 직원을 생성
        Organization organization = organizationRepository.findByPublicId(principal.organizationPublicId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 조직입니다."));
        Department department = resolveDepartment(dto.getDepartmentPublicId(), true);

        // 조직 영문명을 기준으로 일반 직원 로그인 ID를 자동 생성합니다.
        String generatedLoginId = generateOrganizationUserLoginId(organization);

        // 직원 첫 로그인용 임시 비밀번호를 생성
        String temporaryPassword = createTemporaryPassword();

        // DB에는 암호화된 비밀번호를 저장
        String encodedPassword = passwordEncoder.encode(temporaryPassword);

        // 일반 직원(USER) 계정을 생성
        User user = User.builder()
                .organization(organization)
                .department(department)
                .loginId(generatedLoginId)
                .password(encodedPassword)
                .firstName(dto.getFirstName())
                .middleName(dto.getMiddleName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .jobTitle(dto.getJobTitle())
                .userRole(UserRole.USER)
                .status(Status.ACTIVE)
                .passwordChangeRequired(true)
                .build();

        User savedUser = userRepository.save(user);
        userSearchService.saveUserDocument(savedUser);

        // 방금 생성한 직원 계정 정보를 이메일로 보냅니다.
        credentialMailService.sendTemporaryCredentialMail(
                savedUser.getEmail(),
                organization.getOrganizationName(),
                savedUser.getLoginId(),
                temporaryPassword
        );

        return ProvisionedUserResponseDto.builder()
                .userPublicId(savedUser.getPublicId())
                .organizationPublicId(organization.getPublicId())
                .loginId(savedUser.getLoginId())
                .temporaryPassword(temporaryPassword)
                .passwordChangeRequired(true)
                .build();
    }

    // 사용자 정보 조회 (내정보조회)
    public MyInfoDto getMyInfo(String userPublicId, String organizationPublicId, UserRole role) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return MyInfoDto.builder()
                .userPublicId(userPublicId)
                .organizationPublicId(organizationPublicId)
                .role(role)
                .profileAttachmentPublicId(user.getProfileAttachmentPublicId())
                .profileImageThumbPath(user.getProfileImageThumbPath())
                .build();
    }

    // 사용자 목록 조회
    public Page<UserListDto> userList(Pageable pageable, UserSearchDto searchDto) {
        // 검색 조건이 하나라도 있으면 Elasticsearch 통합검색을 사용
        // 조건이 전혀 없을 때만 기본 DB 목록 조회로 내려감
        if (hasSearchCondition(searchDto)) {
            return userSearchService.search(pageable, searchDto);
        }

        return userRepository.findAll(pageable)
                .map(UserListDto::fromEntity);
    }

    // 사용자 상세 조회
    public UserDetailDto userDetail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return UserDetailDto.fromEntity(user);
    }

    // 사용자 정보 수정
    public UserDetailDto userUpdate(Long userId, UserUpdateDto dto, AuthPrincipal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        if (!principal.userId().equals(userId) && principal.role() != UserRole.ADMIN) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        user.updateUser(dto);
        if (dto.getDepartmentPublicId() != null) {
            if (principal.role() != UserRole.ADMIN) {
                throw new IllegalArgumentException("부서는 관리자만 변경할 수 있습니다.");
            }
            user.updateDepartment(resolveDepartment(dto.getDepartmentPublicId(), true));
        }
        userSearchService.saveUserDocument(user);

        return UserDetailDto.fromEntity(user);
    }


    // 사용자 권한 변경
    // 사용자의 상태를 ACTIVE, DEACTIVE, DELETE 중 하나로 변경합니다.
    public UserDetailDto userStatusUpdate(Long userId, UserStatusUpdateDto dto, AuthPrincipal principal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        boolean isAdmin = principal.role() == UserRole.ADMIN;
        boolean isSelf = principal.userId().equals(userId);
        Status targetStatus = dto.getStatus();

        if (!isAdmin && !isSelf) {
            throw new IllegalArgumentException("사용자 상태 변경 권한이 없습니다.");
        }

        // 자기 자신의 계정은 비활성화나 삭제만 할 수 있고, 다시 활성화하는 것은 관리자만 허용합니다.
        if (!isAdmin && targetStatus == Status.ACTIVE) {
            throw new IllegalArgumentException("사용자 활성화는 관리자만 할 수 있습니다.");
        }

        // 소속 조직이 비활성화 또는 삭제된 상태라면 사용자만 단독으로 활성화할 수 없습니다.
        if (targetStatus == Status.ACTIVE && user.getOrganization().getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("비활성화 또는 삭제된 조직 소속 사용자는 활성화할 수 없습니다.");
        }

        user.changeStatus(targetStatus);
        userSearchService.saveUserDocument(user);

        return UserDetailDto.fromEntity(user);
    }

    public UserDetailDto userRoleUpdate(Long userId, UserRoleUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        user.updateUserRole(dto.getUserRole());
        userSearchService.saveUserDocument(user);

        return UserDetailDto.fromEntity(user);
    }

    // 검색 DTO에 실제 검색 조건이 하나라도 들어왔는지 확인
    // keyword 뿐 아니라 조직, 권한, 이름, 아이디, 상태도 모두 통합검색 진입 조건으로 봄
    private boolean hasSearchCondition(UserSearchDto searchDto) {
        if (searchDto == null) {
            return false;
        }

        return hasText(searchDto.getOrganizationPublicId())
                || searchDto.getUserRole() != null
                || hasText(searchDto.getLoginId())
                || hasText(searchDto.getFirstName())
                || hasText(searchDto.getMiddleName())
                || hasText(searchDto.getLastName())
                || searchDto.getStatus() != null
                || hasText(searchDto.getKeyword());
    }

    private Department resolveDepartment(String departmentPublicId, boolean required) {
        if (!hasText(departmentPublicId)) {
            if (required) {
                throw new IllegalArgumentException("부서 선택은 필수입니다.");
            }
            return null;
        }

        return departmentRepository.findByPublicId(departmentPublicId)
                .filter(department -> department.getStatus() == Status.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 비활성화된 부서입니다."));
    }

    // 문자열 값이 null 이거나 공백인지 확인하는 공통 메서드
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // userPublicId 기준 사용자 상세 조회
    public UserDetailDto userDetailByPublicId(String userPublicId) {
        User user = userRepository.findByPublicId(userPublicId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (user.getStatus() != Status.ACTIVE) {
            throw new IllegalArgumentException("존재하지 않는 사용자입니다.");
        }

        return UserDetailDto.fromEntity(user);
    }

    @Transactional(readOnly = true)
    public List<UserRecipientDto> getNotificationRecipients(String organizationPublicId, String departmentCode) {
        if (!hasText(organizationPublicId)) {
            return List.of();
        }

        if (hasText(departmentCode)) {
            List<User> departmentUsers = userRepository
                    .findAllByOrganization_PublicIdAndDepartment_DepartmentCodeAndStatus(
                            organizationPublicId,
                            departmentCode,
                            Status.ACTIVE
                    );
            if (!departmentUsers.isEmpty()) {
                return departmentUsers.stream()
                        .map(UserRecipientDto::fromEntity)
                        .toList();
            }
        }

        return userRepository.findAllByOrganization_PublicIdAndUserRoleAndStatus(
                        organizationPublicId,
                        UserRole.ORG_ADMIN,
                        Status.ACTIVE
                )
                .stream()
                .map(UserRecipientDto::fromEntity)
                .toList();
    }

    // 임시 비밀번호를 간단히 생성
    // 나중에 규칙이 필요하면 별도 유틸로 분리할 수 있음
    private String createTemporaryPassword() {
        return "Atlas!" + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    // 조직 영문명을 로그인 ID에 쓸 수 있는 slug 형태로 정리
    // 예: "Hanwha Aerospace" -> "hanwha-aerospace"
    private String toOrganizationSlug(String organizationEnglishName) {
        // 값이 없으면 기본값으로 org를 씀
        if (organizationEnglishName == null || organizationEnglishName.isBlank()) {
            return "org";
        }

        // 영문명에 섞인 특수문자를 정리하기 위해 정규화
        String normalized = Normalizer.normalize(organizationEnglishName, Normalizer.Form.NFKC);

        // 영문/숫자/공백/하이픈만 남기고 나머지는 제거
        String slug = normalized
                .replaceAll("[^A-Za-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .toLowerCase();

        // 정리 후 비어 있으면 기본값
        return slug.isBlank() ? "org" : slug;
    }

    // 최초 ORG_ADMIN 로그인 ID를 자동 생성
    // 1순위는 admin@{orgSlug} 이고, 이미 있으면 admin001@{orgSlug} 로 올라감
    private String generateInitialOrgAdminLoginId(Organization organization) {
        // 조직 영문명을 slug로 바꿈
        String orgSlug = toOrganizationSlug(organization.getOrganizationEnglishName());

        // 가장 먼저 시도할 기본 로그인 ID
        String baseLoginId = "admin@" + orgSlug;

        // 기본 아이디가 비어 있으면 바로 사용
        if (!userRepository.existsByLoginId(baseLoginId)) {
            return baseLoginId;
        }

        // 이미 있으면 001부터 하나씩 올려가며 빈 값을 찾음
        int sequence = 1;

        while (true) {
            String candidate = String.format("admin%03d@%s", sequence, orgSlug);

            if (!userRepository.existsByLoginId(candidate)) {
                return candidate;
            }

            sequence++;
        }
    }

    // 일반 직원 로그인 ID를 자동 생성
    // 지금은 부서 없이 user001@{orgSlug} 형식으로 생성
    private String generateOrganizationUserLoginId(Organization organization) {
        // 조직 영문명을 slug로 바꿈
        String orgSlug = toOrganizationSlug(organization.getOrganizationEnglishName());

        // 001부터 시작해서 빈 값을 찾을 때까지 반복
        int sequence = 1;

        while (true) {
            String candidate = String.format("user%03d@%s", sequence, orgSlug);

            if (!userRepository.existsByLoginId(candidate)) {
                return candidate;
            }

            sequence++;
        }
    }

    // 사용자 엔티티를 그대로 조회
    // 보안 이력 저장 시 재사용
    @Transactional(readOnly = true)
    public User getUserEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    }

    // 사용자 수정 시 어떤 항목이 바뀌었는지 한 줄 요약을 만듬
    public String buildProfileUpdateSummary(User user, UserUpdateDto dto) {
        List<String> changedFields = new ArrayList<>();

        // 이름 변경 여부를 확인
        if (!Objects.equals(user.getFirstName(), dto.getFirstName())
                || !Objects.equals(user.getMiddleName(), dto.getMiddleName())
                || !Objects.equals(user.getLastName(), dto.getLastName())) {
            changedFields.add("이름");
        }

        // 이메일 변경 여부를 확인
        if (!Objects.equals(user.getEmail(), dto.getEmail())) {
            changedFields.add("이메일");
        }

        // 연락처 변경 여부를 확인
        if (!Objects.equals(user.getPhone(), dto.getPhone())) {
            changedFields.add("연락처");
        }

        // 직책 변경 여부를 확인
        if (!Objects.equals(user.getJobTitle(), dto.getJobTitle())) {
            changedFields.add("직책");
        }

        if (dto.getDepartmentPublicId() != null) {
            String currentDepartmentPublicId = user.getDepartment() != null ? user.getDepartment().getPublicId() : null;
            if (!Objects.equals(currentDepartmentPublicId, dto.getDepartmentPublicId())) {
                changedFields.add("부서");
            }
        }

        if (!Objects.equals(user.getProfileAttachmentPublicId(), dto.getProfileAttachmentPublicId())
                || !Objects.equals(user.getProfileImageThumbPath(), dto.getProfileImageThumbPath())) {
            changedFields.add("프로필 이미지");
        }

        // 바뀐 값이 없으면 일반 문구를 반환
        if (changedFields.isEmpty()) {
            return "프로필 정보 수정";
        }

        // 변경된 항목들을 이어 붙여서 반환
        return String.join(", ", changedFields) + " 변경";
    }

    // 조직 대표자가 엑셀 파일로 자기 조직 사원을 일괄 등록
    public OrganizationUserExcelUploadResponseDto uploadOrganizationUsers(
            MultipartFile file,
            AuthPrincipal principal
    ) {
        // 조직 대표자만 업로드할 수 있음
        if (principal.role() != UserRole.ORG_ADMIN) {
            throw new IllegalArgumentException("사원 일괄 등록 권한이 없습니다.");
        }

        // 파일이 없거나 비어 있으면 바로 막음
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 비어 있습니다.");
        }

        // 줄별 처리 결과를 담을 리스트
        List<OrganizationUserExcelUploadResponseDto.RowResult> results = new ArrayList<>();

        // 셀 값을 문자열로 안정적으로 읽기 위한 도구
        DataFormatter formatter = new DataFormatter();

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            // 첫 번째 시트만 사용
            Sheet sheet = ExcelUtils.getFirstSheetOrThrow(workbook);

            // 시트가 비어 있으면 바로 막음
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new IllegalArgumentException("엑셀 시트가 비어 있습니다.");
            }

            // 0번 줄은 헤더라고 가정하고 1번 줄부터 실제 데이터로 처리
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                // 완전히 빈 줄은 그냥 건너뜀
                if (ExcelUtils.isRowEmpty(row, 6, formatter)) {
                    continue;
                }

                try {
                    // 엑셀 컬럼 순서:
                    // 0 firstName
                    // 1 middleName
                    // 2 lastName
                    // 3 email
                    // 4 phone
                    // 5 jobTitle
                    // 6 departmentPublicId

                    String firstName = ExcelUtils.getCellValue(row, 0, formatter);
                    String middleName = ExcelUtils.getCellValue(row, 1, formatter);
                    String lastName = ExcelUtils.getCellValue(row, 2, formatter);
                    String email = ExcelUtils.getCellValue(row, 3, formatter);
                    String phone = ExcelUtils.getCellValue(row, 4, formatter);
                    String jobTitle = ExcelUtils.getCellValue(row, 5, formatter);
                    String departmentPublicId = ExcelUtils.getCellValue(row, 6, formatter);

                    // 필수값은 여기서 한 번 더 확인
                    if (firstName.isBlank() || lastName.isBlank() || email.isBlank()
                            || phone.isBlank() || departmentPublicId.isBlank()) {
                        throw new IllegalArgumentException("필수값이 비어 있습니다.");
                    }

                    // 기존 사원 생성 DTO로 그대로 변환
                    OrganizationUserCreateDto dto = OrganizationUserCreateDto.builder()
                            .firstName(firstName)
                            .middleName(middleName.isBlank() ? null : middleName)
                            .lastName(lastName)
                            .email(email)
                            .phone(phone)
                            .jobTitle(jobTitle.isBlank() ? null : jobTitle)
                            .departmentPublicId(departmentPublicId)
                            .build();

                    // 기존 단건 생성 로직을 그대로 재사용
                    ProvisionedUserResponseDto createdUser =
                            createOrganizationUser(dto, principal);

                    // 성공 결과를 담음
                    results.add(OrganizationUserExcelUploadResponseDto.RowResult.builder()
                            .rowNumber(rowIndex + 1)
                            .success(true)
                            .userPublicId(createdUser.getUserPublicId())
                            .loginId(createdUser.getLoginId())
                            .temporaryPassword(createdUser.getTemporaryPassword())
                            .message("생성 완료")
                            .build());
                } catch (Exception rowError) {
                    // 한 줄 실패해도 전체 업로드는 계속 진행
                    results.add(OrganizationUserExcelUploadResponseDto.RowResult.builder()
                            .rowNumber(rowIndex + 1)
                            .success(false)
                            .message(rowError.getMessage())
                            .build());
                }
            }

        } catch (IOException e) {
            throw new IllegalArgumentException("엑셀 파일을 읽는 중 오류가 발생했습니다.");
        }

        // 성공/실패 개수를 계산
        int totalCount = results.size();
        int successCount = (int) results.stream().filter(OrganizationUserExcelUploadResponseDto.RowResult::isSuccess).count();
        int failCount = totalCount - successCount;

        // 최종 업로드 결과를 반환
        return OrganizationUserExcelUploadResponseDto.builder()
                .totalCount(totalCount)
                .successCount(successCount)
                .failCount(failCount)
                .results(results)
                .build();
    }
}
