#!/usr/bin/env python3
import json
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple


ROOT = Path(__file__).resolve().parents[1]
OUTPUT = ROOT / "docs/openapi/atlas-backend-openapi.json"
SERVICES = ["api-gateway", "auth-service", "supply-service", "control-service", "file-service"]
HTTP_METHODS = {"get", "post", "put", "patch", "delete"}
CONTROLLER_LABELS = {
    "AuthController": "인증",
    "UserController": "사용자",
    "OrganizationController": "조직",
    "DepartmentController": "부서",
    "PurchaseOrderController": "발주",
    "SubPurchaseOrderController": "하위 발주",
    "SettlementController": "정산",
    "SupplierController": "협력사",
    "SupplierItemCapabilityController": "협력사 품목 역량",
    "SupplierCertificateController": "협력사 인증서",
    "CertificateTypeController": "인증서 유형",
    "SupplierRelationController": "협력사 관계",
    "SupplyItemController": "품목",
    "SupplyItemCategoryController": "품목 카테고리",
    "ItemInventoryController": "품목 재고",
    "LogisticsNodeController": "물류 노드",
    "LotController": "LOT",
    "LotLineMappingController": "LOT 생산라인 매핑",
    "ProductionLineController": "생산 라인",
    "ShipmentController": "출하",
    "DeliveryExceptionController": "배송 예외",
    "ShipmentLotMappingController": "출하-LOT 매핑",
    "ReturnController": "반품",
    "IntegratedSearchController": "통합 검색",
    "SupplyBatchController": "배치 작업",
    "KafkaMonitoringController": "Kafka 모니터링",
    "ChatRoomController": "채팅방",
    "ChatMessageController": "채팅 메시지",
    "NotificationController": "알림",
    "FileController": "첨부 파일",
    "SupplyEsgAssessmentController": "공급 ESG 평가",
    "SupplySidebarBadgeController": "공급 사이드바 배지",
}
PRIMITIVES = {
    "String": {"type": "string"},
    "Long": {"type": "integer", "format": "int64"},
    "long": {"type": "integer", "format": "int64"},
    "Integer": {"type": "integer", "format": "int32"},
    "int": {"type": "integer", "format": "int32"},
    "Short": {"type": "integer", "format": "int32"},
    "short": {"type": "integer", "format": "int32"},
    "Double": {"type": "number", "format": "double"},
    "double": {"type": "number", "format": "double"},
    "Float": {"type": "number", "format": "float"},
    "float": {"type": "number", "format": "float"},
    "BigDecimal": {"type": "number"},
    "Boolean": {"type": "boolean"},
    "boolean": {"type": "boolean"},
    "UUID": {"type": "string", "format": "uuid"},
    "LocalDate": {"type": "string", "format": "date"},
    "LocalDateTime": {"type": "string", "format": "date-time"},
    "OffsetDateTime": {"type": "string", "format": "date-time"},
    "Instant": {"type": "string", "format": "date-time"},
    "MultipartFile": {"type": "string", "format": "binary"},
    "byte[]": {"type": "string", "format": "byte"},
    "Object": {"type": "object"},
    "Void": {"type": "object"},
    "void": {"type": "object"},
}
IGNORED_FIELD_PREFIXES = ("serialVersionUID",)


@dataclass
class JavaFile:
    package: str
    name: str
    kind: str
    content: str
    path: Path


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    return re.sub(r"//.*", "", text)


def split_top_level(text: str, delimiter: str = ",") -> List[str]:
    items = []
    current = []
    depth_angle = depth_paren = depth_brace = depth_bracket = 0
    in_string = False
    escape = False
    for ch in text:
        if in_string:
            current.append(ch)
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
            current.append(ch)
            continue
        if ch == "<":
            depth_angle += 1
        elif ch == ">":
            depth_angle = max(0, depth_angle - 1)
        elif ch == "(":
            depth_paren += 1
        elif ch == ")":
            depth_paren = max(0, depth_paren - 1)
        elif ch == "{":
            depth_brace += 1
        elif ch == "}":
            depth_brace = max(0, depth_brace - 1)
        elif ch == "[":
            depth_bracket += 1
        elif ch == "]":
            depth_bracket = max(0, depth_bracket - 1)
        if ch == delimiter and not any([depth_angle, depth_paren, depth_brace, depth_bracket]):
            items.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
    tail = "".join(current).strip()
    if tail:
        items.append(tail)
    return items


def find_matching(text: str, start: int, open_char: str = "(", close_char: str = ")") -> int:
    depth = 0
    in_string = False
    escape = False
    for idx in range(start, len(text)):
        ch = text[idx]
        if in_string:
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
            continue
        if ch == open_char:
            depth += 1
        elif ch == close_char:
            depth -= 1
            if depth == 0:
                return idx
    return -1


def extract_annotation_arg(annotation_text: str, key: str) -> Optional[str]:
    match = re.search(rf"{re.escape(key)}\s*=\s*", annotation_text)
    if not match:
        return None
    pos = match.end()
    while pos < len(annotation_text) and annotation_text[pos].isspace():
        pos += 1
    if pos >= len(annotation_text):
        return None
    if annotation_text[pos] == '"':
        end = pos + 1
        escaped = False
        while end < len(annotation_text):
            if escaped:
                escaped = False
            elif annotation_text[end] == "\\":
                escaped = True
            elif annotation_text[end] == '"':
                break
            end += 1
        return annotation_text[pos + 1:end]
    if annotation_text[pos] == "{":
        end = find_matching(annotation_text, pos, "{", "}")
        return annotation_text[pos + 1:end]
    end = pos
    depth = 0
    while end < len(annotation_text):
        ch = annotation_text[end]
        if ch in "({[":
            depth += 1
        elif ch in ")}]":
            if depth == 0:
                break
            depth -= 1
        elif ch == "," and depth == 0:
            break
        end += 1
    return annotation_text[pos:end].strip()


def extract_annotation_values(annotation_text: str) -> List[str]:
    values: List[str] = []
    for key in ("value", "path"):
        value = extract_annotation_arg(annotation_text, key)
        if value:
            values.extend(re.findall(r'"([^"]+)"', value))
    if values:
        return values
    direct = re.findall(r'"([^"]+)"', annotation_text)
    return direct[:1]


def normalize_path(base: str, extra: str) -> str:
    if extra.startswith("/api/"):
        return extra
    base = base or ""
    extra = extra or ""
    if not base:
        return extra or "/"
    if not extra:
        return base
    return f"{base.rstrip('/')}/{extra.lstrip('/')}"


def clean_type_name(type_name: str) -> str:
    type_name = re.sub(r"@\w+(?:\([^)]*\))?\s*", "", type_name)
    type_name = re.sub(r"\bfinal\s+", "", type_name)
    type_name = re.sub(r"\s+", " ", type_name).strip()
    return type_name


def simple_name(type_name: str) -> str:
    cleaned = clean_type_name(type_name)
    cleaned = re.sub(r"<.*>", "", cleaned).strip()
    if "." in cleaned:
        cleaned = cleaned.split(".")[-1]
    return cleaned


def humanize_camel(name: str) -> str:
    text = re.sub(r"([a-z0-9])([A-Z])", r"\1 \2", name).replace("_", " ")
    return re.sub(r"\s+", " ", text).strip()


def guess_example(name: str, schema: dict):
    if schema.get("example") is not None:
        return schema["example"]
    if schema.get("enum"):
        return schema["enum"][0]
    lowered = name.lower()
    schema_type = schema.get("type")
    schema_format = schema.get("format")
    if lowered.endswith("publicid") or lowered == "publicid":
        return "sample_public_id"
    if lowered.endswith("id"):
        return 1 if schema_type == "integer" else "sample_id"
    if "email" in lowered:
        return "user@atlas.com"
    if "phone" in lowered or "tel" in lowered:
        return "010-1234-5678"
    if "date" in lowered and schema_format == "date":
        return "2026-04-17"
    if lowered.endswith("at") or "time" in lowered or schema_format == "date-time":
        return "2026-04-17T09:00:00"
    if "number" in lowered or lowered.endswith("no"):
        return "NO-2026-0001"
    if "code" in lowered:
        return "CODE-001"
    if "name" in lowered or "title" in lowered:
        return "샘플 이름"
    if "keyword" in lowered:
        return "샘플 검색어"
    if "memo" in lowered:
        return "샘플 메모"
    if "reason" in lowered:
        return "샘플 사유"
    if "message" in lowered or "body" in lowered or "content" in lowered or "description" in lowered:
        return "샘플 내용"
    if "path" in lowered or "url" in lowered:
        return "https://example.com/resource"
    if "quantity" in lowered or "amount" in lowered or "price" in lowered:
        return 100
    if "count" in lowered or "order" in lowered or "size" in lowered:
        return 1
    if schema_type == "boolean":
        return True
    if schema_type == "integer":
        return 1
    if schema_type == "number":
        return 100
    if schema_type == "array":
        item_schema = schema.get("items", {})
        item_example = guess_example(name, item_schema)
        return [] if item_example is None else [item_example]
    if schema_type == "string":
        return "sample"
    if schema_type == "object":
        return "sample"
    return None


def guess_property_description(name: str) -> str:
    lowered = name.lower()
    if lowered.endswith("publicid") or lowered == "publicid":
        return "공개 식별자"
    if lowered.endswith("id"):
        return "내부 식별자"
    if "email" in lowered:
        return "이메일 주소"
    if "phone" in lowered or "tel" in lowered:
        return "연락처"
    if "date" in lowered:
        return "날짜 값"
    if lowered.endswith("at") or "time" in lowered:
        return "일시 값"
    if "number" in lowered or lowered.endswith("no"):
        return "업무 번호"
    if "code" in lowered:
        return "코드 값"
    if "name" in lowered:
        return "이름"
    if "title" in lowered:
        return "제목"
    if "memo" in lowered:
        return "메모"
    if "reason" in lowered:
        return "사유"
    if "message" in lowered or "body" in lowered or "content" in lowered:
        return "본문 내용"
    if "path" in lowered or "url" in lowered:
        return "경로 또는 URL"
    if "status" in lowered:
        return "상태 값"
    if "type" in lowered:
        return "유형 값"
    if "role" in lowered:
        return "권한 값"
    if "quantity" in lowered:
        return "수량"
    if "amount" in lowered or "price" in lowered:
        return "금액"
    if "count" in lowered:
        return "개수"
    return f"{humanize_camel(name)} 값"


def infer_schema_description(name: str) -> str:
    lowered = name.lower()
    if lowered.endswith("requestdto") or lowered.endswith("request"):
        return f"{humanize_camel(name.replace('Dto', '').replace('Request', ''))} 요청 모델"
    if lowered.endswith("responsedto") or lowered.endswith("response"):
        return f"{humanize_camel(name.replace('Dto', '').replace('Response', ''))} 응답 모델"
    if lowered.endswith("searchdto"):
        return f"{humanize_camel(name.replace('Dto', ''))} 검색 조건 모델"
    if lowered.endswith("dto"):
        return f"{humanize_camel(name.replace('Dto', ''))} 데이터 모델"
    return f"{humanize_camel(name)} 모델"


def infer_parameter_description(param: dict) -> str:
    name = param["name"]
    if param["in"] == "header":
        if name == "X-Organization-Public-Id":
            return "요청 주체 조직의 공개 식별자"
        if name == "X-User-Public-Id":
            return "요청 사용자 공개 식별자"
        if name == "X-Organization-Type":
            return "요청 조직 유형"
        return f"{name} 헤더 값"
    if param["in"] == "path":
        return f"{guess_property_description(name)} 경로 변수"
    if param["in"] == "query":
        return f"{guess_property_description(name)} 조회 조건"
    return guess_property_description(name)


def apply_parameter_example(parameter: dict, components: Dict[str, dict]) -> None:
    schema = parameter.get("schema", {})
    if schema.get("example") is not None:
        return
    if schema.get("enum"):
        schema["example"] = schema["enum"][0]
        return
    ref = schema.get("$ref")
    if ref:
        schema_name = ref.rsplit("/", 1)[-1]
        enum_values = components.get(schema_name, {}).get("enum")
        if enum_values:
            schema["example"] = enum_values[0]
            return
    example = guess_example(parameter["name"], schema)
    if example is not None:
        schema["example"] = example


def infer_action(method_name: str, http_method: str, path: str) -> str:
    lowered = method_name.lower()
    if lowered == "login":
        return "로그인"
    if lowered == "logout":
        return "로그아웃"
    if lowered == "refresh":
        return "토큰 재발급"
    if lowered.startswith("signup"):
        return "회원가입"
    if "myinfo" in lowered:
        return "내 정보 조회"
    if "unreadcount" in lowered:
        return "안 읽은 개수 조회"
    if "readall" in lowered:
        return "전체 읽음 처리"
    if "markasread" in lowered or lowered.startswith("read"):
        return "읽음 처리"
    if "search" in lowered:
        return "검색"
    if "history" in lowered or "histories" in lowered:
        return "이력 조회"
    if "projection" in lowered:
        return "예측 조회"
    if lowered.endswith("eta") or "eta" in lowered:
        return "ETA 조회"
    if "appendfiles" in lowered:
        return "파일 추가 업로드"
    if "updatefileorder" in lowered:
        return "순서 변경"
    if "invite" in lowered:
        return "참여자 초대"
    if lowered.startswith("leave"):
        return "나가기"
    if lowered.startswith("approve"):
        return "승인"
    if lowered.startswith("reject"):
        if "certificate" in lowered:
            return "반려"
        return "거절"
    if lowered.startswith("accept"):
        return "수락"
    if lowered.startswith("confirm"):
        return "확정"
    if lowered.startswith("cancel"):
        return "취소"
    if lowered.startswith("delete") or lowered.startswith("remove"):
        return "삭제"
    if lowered.startswith("change") or lowered.endswith("status") or "qualitystatus" in lowered:
        if "quality" in lowered:
            return "품질 상태 변경"
        return "상태 변경"
    if lowered.startswith("update"):
        if "password" in lowered:
            return "비밀번호 변경"
        if "role" in lowered:
            return "권한 변경"
        return "수정"
    if lowered.startswith("create") or lowered.startswith("add") or lowered.startswith("run"):
        return "생성" if not lowered.startswith("run") else "실행"
    if lowered.startswith("get") or lowered.startswith("find"):
        if "list" in lowered or "all" in lowered or path.endswith("/received"):
            return "목록 조회"
        return "상세 조회" if re.search(r"/\{[^}]+\}$", path) else "조회"
    if http_method == "post":
        return "생성"
    if http_method == "get":
        return "조회"
    if http_method in {"put", "patch"}:
        return "수정"
    if http_method == "delete":
        return "삭제"
    return "처리"


def infer_summary(controller_name: str, method_name: str, http_method: str, path: str) -> str:
    domain = CONTROLLER_LABELS.get(controller_name, humanize_camel(controller_name.replace("Controller", "")))
    action = infer_action(method_name, http_method, path)
    return f"{domain} {action}"


def infer_description(summary: str, operation: dict) -> str:
    parts = [f"{summary} API입니다."]
    parameters = operation.get("parameters", [])
    headers = [param["name"] for param in parameters if param.get("in") == "header"]
    queries = [param["name"] for param in parameters if param.get("in") == "query"]
    paths = [param["name"] for param in parameters if param.get("in") == "path"]
    if paths:
        parts.append(f"경로 변수 {', '.join(paths)} 로 대상을 식별합니다.")
    if queries:
        parts.append(f"조회 조건으로 {', '.join(queries)} 를 사용할 수 있습니다.")
    if headers:
        parts.append(f"헤더 {', '.join(headers)} 값을 함께 전달해야 할 수 있습니다.")
    if operation.get("requestBody"):
        parts.append("요청 본문 데이터를 기반으로 처리합니다.")
    return " ".join(parts)


def infer_success_response(method_body: str) -> Tuple[str, str]:
    if "ResponseEntity.noContent()" in method_body:
        return "204", "No Content"
    if "HttpStatus.CREATED" in method_body or "ResponseEntity.created(" in method_body:
        return "201", "Created"
    return "200", "OK"


def enrich_property_schema(prop_name: str, prop_schema: dict) -> None:
    if "$ref" in prop_schema:
        if not prop_schema.get("description"):
            prop_schema["description"] = guess_property_description(prop_name)
        return
    if prop_schema.get("type") == "array":
        if not prop_schema.get("description"):
            prop_schema["description"] = guess_property_description(prop_name)
        if prop_schema.get("example") is None:
            example = guess_example(prop_name, prop_schema)
            if example is not None:
                prop_schema["example"] = example
        enrich_property_schema(prop_name, prop_schema.get("items", {}))
        return
    if prop_schema.get("type") == "object" and "properties" in prop_schema:
        if not prop_schema.get("description"):
            prop_schema["description"] = guess_property_description(prop_name)
        for child_name, child_schema in prop_schema.get("properties", {}).items():
            enrich_property_schema(child_name, child_schema)
        return
    if not prop_schema.get("description"):
        prop_schema["description"] = guess_property_description(prop_name)
    if prop_schema.get("example") is None:
        example = guess_example(prop_name, prop_schema)
        if example is not None:
            prop_schema["example"] = example


def extract_generics(type_name: str) -> List[str]:
    cleaned = clean_type_name(type_name)
    start = cleaned.find("<")
    end = cleaned.rfind(">")
    if start == -1 or end == -1 or end <= start:
        return []
    return split_top_level(cleaned[start + 1:end])


def default_schema_for_type(type_name: str, registry: Dict[str, JavaFile], seen: Optional[set] = None) -> dict:
    cleaned = clean_type_name(type_name)
    if cleaned.endswith("[]"):
        return {"type": "array", "items": default_schema_for_type(cleaned[:-2], registry, seen)}
    if cleaned in PRIMITIVES:
        return dict(PRIMITIVES[cleaned])
    base = simple_name(cleaned)
    generics = extract_generics(cleaned)

    if base in {"ResponseEntity", "HttpEntity", "Optional", "Mono"}:
        return default_schema_for_type(generics[0] if generics else "Object", registry, seen)
    if base in {"List", "Set", "Collection", "Iterable", "Flux"}:
        inner = generics[0] if generics else "Object"
        return {"type": "array", "items": default_schema_for_type(inner, registry, seen)}
    if base == "Page":
        inner = generics[0] if generics else "Object"
        return {
            "type": "object",
            "properties": {
                "content": {"type": "array", "items": default_schema_for_type(inner, registry, seen)},
                "totalElements": {"type": "integer", "format": "int64"},
                "totalPages": {"type": "integer", "format": "int32"},
                "size": {"type": "integer", "format": "int32"},
                "number": {"type": "integer", "format": "int32"},
            },
        }
    if base == "Map":
        value_type = generics[1] if len(generics) > 1 else "Object"
        return {"type": "object", "additionalProperties": default_schema_for_type(value_type, registry, seen)}
    if base in registry:
        return {"$ref": f"#/components/schemas/{base}"}
    return {"type": "object"}


def parse_java_file(path: Path) -> Optional[JavaFile]:
    text = path.read_text(encoding="utf-8")
    text = strip_comments(text)
    package_match = re.search(r"package\s+([\w\.]+);", text)
    class_match = re.search(r"\b(class|enum|record)\s+(\w+)", text)
    if not package_match or not class_match:
        return None
    return JavaFile(
        package=package_match.group(1),
        name=class_match.group(2),
        kind=class_match.group(1),
        content=text,
        path=path,
    )


def build_registry() -> Dict[str, JavaFile]:
    registry: Dict[str, JavaFile] = {}
    for service in SERVICES:
        base = ROOT / service / "src/main/java"
        for path in base.rglob("*.java"):
            parsed = parse_java_file(path)
            if parsed:
                registry.setdefault(parsed.name, parsed)
    return registry


def parse_field_annotations(block: str) -> dict:
    meta: Dict[str, str] = {}
    schema_blocks = re.findall(r"@Schema\s*\((.*?)\)", block, flags=re.S)
    for schema_block in schema_blocks:
        description = extract_annotation_arg(schema_block, "description")
        example = extract_annotation_arg(schema_block, "example")
        if description:
            meta["description"] = description
        if example:
            meta["example"] = example
    if re.search(r"@(NotNull|NotBlank|NotEmpty)\b", block):
        meta["required"] = "true"
    json_prop = re.findall(r'@JsonProperty\s*\(\s*"([^"]+)"\s*\)', block)
    if json_prop:
        meta["json_name"] = json_prop[-1]
    return meta


def parse_properties(java_file: JavaFile, registry: Dict[str, JavaFile], seen: set) -> dict:
    if java_file.kind == "enum":
        constants_block = java_file.content.split("{", 1)[1].rsplit("}", 1)[0]
        enum_values = []
        for item in split_top_level(constants_block, ","):
            token = item.strip()
            if not token or "(" in token or "=" in token or " " in token:
                continue
            enum_values.append(token)
        return {"type": "string", "enum": enum_values}

    schema = {"type": "object", "properties": {}}
    required: List[str] = []
    body = java_file.content.split("{", 1)[1].rsplit("}", 1)[0]
    field_pattern = re.compile(
        r"((?:\s*@[\w\.]+(?:\([^)]*\))?\s*)*)\s*(private|protected|public)\s+(?!static\b)([\w<>\.\[\], ?]+?)\s+(\w+)\s*(?:=[^;]*)?;",
        re.S,
    )
    for match in field_pattern.finditer(body):
        annotations, _, type_name, field_name = match.groups()
        if field_name.startswith(IGNORED_FIELD_PREFIXES):
            continue
        meta = parse_field_annotations(annotations)
        json_name = meta.get("json_name", field_name)
        prop_schema = default_schema_for_type(type_name.strip(), registry, seen)
        if "description" in meta:
            prop_schema["description"] = meta["description"]
        if "example" in meta:
            prop_schema["example"] = meta["example"]
        enrich_property_schema(json_name, prop_schema)
        schema["properties"][json_name] = prop_schema
        if meta.get("required") == "true":
            required.append(json_name)
    if required:
        schema["required"] = sorted(set(required))
    if not schema.get("description"):
        schema["description"] = infer_schema_description(java_file.name)
    return schema


def ensure_schema(type_name: str, registry: Dict[str, JavaFile], components: Dict[str, dict], seen: Optional[set] = None) -> None:
    seen = seen or set()
    cleaned = clean_type_name(type_name)
    base = simple_name(cleaned)
    generics = extract_generics(cleaned)
    if base in {"ResponseEntity", "HttpEntity", "Optional", "Mono", "Flux", "List", "Set", "Collection", "Iterable", "Page", "Map"}:
        for generic in generics:
            ensure_schema(generic, registry, components, seen)
        return
    if base in PRIMITIVES or base not in registry or base in components or base in seen:
        return
    seen.add(base)
    java_file = registry[base]
    schema = parse_properties(java_file, registry, seen)
    components[base] = schema
    for prop in schema.get("properties", {}).values():
        ref = prop.get("$ref")
        if ref:
            ensure_schema(ref.split("/")[-1], registry, components, seen)
        if prop.get("type") == "array":
            items = prop.get("items", {})
            if "$ref" in items:
                ensure_schema(items["$ref"].split("/")[-1], registry, components, seen)
        if prop.get("additionalProperties", {}).get("$ref"):
            ensure_schema(prop["additionalProperties"]["$ref"].split("/")[-1], registry, components, seen)


def annotation_blocks_before(text: str, pos: int) -> List[str]:
    prefix = text[:pos]
    blocks = re.findall(r"(@[\w\.]+(?:\s*\([^)]*\))?\s*)", prefix.split("\n\n")[-1], flags=re.S)
    return blocks


def iter_controller_methods(content: str):
    lines = content.splitlines()
    index = 0
    while index < len(lines):
        line = lines[index].strip()
        if not line.startswith("@"):
            index += 1
            continue

        annotation_parts = []
        while index < len(lines):
            stripped = lines[index].strip()
            if not stripped:
                index += 1
                continue
            if not stripped.startswith("@"):
                break
            current = [lines[index]]
            balance = lines[index].count("(") - lines[index].count(")")
            index += 1
            while index < len(lines):
                next_line = lines[index]
                next_stripped = next_line.strip()
                if balance <= 0 and next_stripped.startswith("@"):
                    break
                if balance <= 0 and next_stripped.startswith("public "):
                    break
                current.append(next_line)
                balance += next_line.count("(") - next_line.count(")")
                index += 1
            annotation_parts.append("\n".join(current))

        while index < len(lines) and not lines[index].strip():
            index += 1
        if index >= len(lines) or not lines[index].strip().startswith("public "):
            continue

        signature_lines = [lines[index].strip()]
        index += 1
        while index < len(lines) and "{" not in signature_lines[-1]:
            signature_lines.append(lines[index].strip())
            index += 1
        signature = " ".join(signature_lines)
        method_match = re.match(r"public\s+([^\(\{=]+?)\s+(\w+)\s*\((.*)\)\s*\{", signature, re.S)
        if method_match:
            body_lines = []
            brace_depth = signature.count("{") - signature.count("}")
            while index < len(lines) and brace_depth > 0:
                body_lines.append(lines[index])
                brace_depth += lines[index].count("{") - lines[index].count("}")
                index += 1
            yield (
                "\n".join(annotation_parts),
                method_match.group(1),
                method_match.group(2),
                method_match.group(3),
                "\n".join(body_lines),
            )


def build_method_return_index(registry: Dict[str, JavaFile]) -> Dict[str, Dict[str, str]]:
    index: Dict[str, Dict[str, str]] = {}
    for java_file in registry.values():
        body = java_file.content
        returns: Dict[str, str] = {}
        for match in re.finditer(
            r"\bpublic\s+([^\(\{=]+?)\s+(\w+)\s*\([^)]*\)\s*\{",
            body,
            re.S,
        ):
            returns[match.group(2)] = match.group(1).strip()
        if returns:
            index[java_file.name] = returns
    return index


def infer_service_return_type(method_body: str, method_return_index: Dict[str, Dict[str, str]]) -> Optional[str]:
    response_call_pattern = re.compile(
        r"ResponseEntity\.(?:ok|status\s*\([^)]*\)\s*\.body)\s*\(\s*(\w+Service)\.(\w+)\s*\(",
        re.S,
    )
    for bean_name, method_name in response_call_pattern.findall(method_body):
        service_class = bean_name[0].upper() + bean_name[1:]
        return_type = method_return_index.get(service_class, {}).get(method_name)
        if return_type and return_type not in {"void", "Void"}:
            return return_type
    for bean_name, method_name in re.findall(r"\b(\w+Service)\.(\w+)\s*\(", method_body):
        service_class = bean_name[0].upper() + bean_name[1:]
        return_type = method_return_index.get(service_class, {}).get(method_name)
        if return_type and return_type not in {"void", "Void"}:
            return return_type
    return None


def parse_controller_methods(java_file: JavaFile, registry: Dict[str, JavaFile], components: Dict[str, dict]) -> Tuple[dict, List[dict]]:
    content = java_file.content
    method_return_index = build_method_return_index(registry)
    class_prefix = content.split("class", 1)[0]
    class_request_match = re.search(r"@RequestMapping\s*\((.*?)\)", class_prefix, re.S)
    class_paths = extract_annotation_values(class_request_match.group(1)) if class_request_match else [""]
    tag_match = re.search(r'@Tag\s*\(\s*name\s*=\s*"([^"]+)"', class_prefix)
    tag_name = tag_match.group(1) if tag_match else CONTROLLER_LABELS.get(java_file.name, java_file.name.replace("Controller", ""))

    operations = {}
    tags_meta = [{"name": tag_name}] if tag_name else []
    for annotation_block, return_type, method_name, params_block, method_body in iter_controller_methods(content):
        if "@MessageMapping" in annotation_block:
            continue
        http_method = None
        mapping_args = None
        for name in HTTP_METHODS:
            mapping_match = re.search(rf"@{name.capitalize()}Mapping\s*(?:\((.*?)\))?", annotation_block, re.S)
            if mapping_match:
                http_method = name
                mapping_args = mapping_match.group(1) or ""
                break
        if http_method is None:
            req_map = re.search(r"@RequestMapping\s*\((.*?)\)", annotation_block, re.S)
            if req_map:
                mapping_args = req_map.group(1)
                method_value = extract_annotation_arg(mapping_args, "method") or ""
                method_match = re.search(r"RequestMethod\.(GET|POST|PUT|PATCH|DELETE)", method_value)
                if method_match:
                    http_method = method_match.group(1).lower()
        if http_method is None:
            continue

        method_paths = extract_annotation_values(mapping_args or "") or [""]
        consumes = extract_annotation_arg(mapping_args or "", "consumes")
        for class_path in class_paths:
            for method_path in method_paths:
                full_path = normalize_path(class_path, method_path)
                op = {
                    "operationId": method_name,
                    "tags": [tag_name],
                    "responses": {
                        infer_success_response(method_body)[0]: {
                            "description": infer_success_response(method_body)[1],
                        }
                    },
                }
                operation_anno = re.search(r"@Operation\s*\((.*?)\)\s*", annotation_block, re.S)
                if operation_anno:
                    summary = extract_annotation_arg(operation_anno.group(1), "summary")
                    description = extract_annotation_arg(operation_anno.group(1), "description")
                    if summary:
                        op["summary"] = summary
                    if description:
                        op["description"] = description

                parameters = []
                request_body = None
                for raw_param in split_top_level(params_block):
                    param = raw_param.strip()
                    if not param:
                        continue
                    normalized = re.sub(r"\s+", " ", param)
                    signature_part = re.sub(r"@\w+(?:\([^)]*\))?\s*", "", normalized).strip()
                    type_match = re.search(r"([\w<>\.\[\], ?]+)\s+(\w+)$", signature_part)
                    if not type_match:
                        continue
                    param_type = type_match.group(1).strip()
                    param_name = type_match.group(2).strip()
                    ensure_schema(param_type, registry, components)
                    schema = default_schema_for_type(param_type, registry)
                    if "@RequestBody" in normalized:
                        request_body = {
                            "required": "required = false" not in normalized,
                            "content": {
                                "application/json": {
                                    "schema": schema,
                                }
                            },
                        }
                        continue
                    if "@RequestPart" in normalized:
                        if request_body is None:
                            request_body = {"required": True, "content": {"multipart/form-data": {"schema": {"type": "object", "properties": {}}}}}
                        multipart_schema = request_body["content"].setdefault("multipart/form-data", {"schema": {"type": "object", "properties": {}}})["schema"]
                        multipart_schema.setdefault("properties", {})[param_name] = schema
                        continue
                    location = None
                    explicit_name = None
                    required = True
                    if "@PathVariable" in normalized:
                        location = "path"
                        name_match = re.search(r'@PathVariable\s*(?:\(\s*"([^"]+)"|\(\s*value\s*=\s*"([^"]+)")', normalized)
                        explicit_name = (name_match.group(1) or name_match.group(2)) if name_match else None
                    elif "@RequestParam" in normalized:
                        location = "query"
                        required = "required = false" not in normalized
                        name_match = re.search(r'@RequestParam\s*(?:\(\s*"([^"]+)"|\(\s*value\s*=\s*"([^"]+)")', normalized)
                        explicit_name = (name_match.group(1) or name_match.group(2)) if name_match else None
                    elif "@RequestHeader" in normalized:
                        location = "header"
                        required = "required = false" not in normalized
                        name_match = re.search(r'@RequestHeader\s*(?:\(\s*"([^"]+)"|\(\s*value\s*=\s*"([^"]+)")', normalized)
                        explicit_name = (name_match.group(1) or name_match.group(2)) if name_match else None
                    if location:
                        parameters.append(
                            {
                                "name": explicit_name or param_name,
                                "in": location,
                                "required": True if location == "path" else required,
                                "schema": schema,
                            }
                        )
                if parameters:
                    for parameter in parameters:
                        if not parameter.get("description"):
                            parameter["description"] = infer_parameter_description(parameter)
                        apply_parameter_example(parameter, components)
                    op["parameters"] = parameters
                if request_body:
                    if consumes and "MULTIPART_FORM_DATA" in consumes:
                        if "multipart/form-data" not in request_body["content"]:
                            request_body["content"] = {"multipart/form-data": request_body["content"].pop("application/json")}
                    op["requestBody"] = request_body

                response_type = return_type
                if "?" in return_type:
                    inferred_return_type = infer_service_return_type(method_body, method_return_index)
                    if inferred_return_type:
                        response_type = inferred_return_type
                ensure_schema(response_type, registry, components)
                response_schema = default_schema_for_type(response_type, registry)
                if response_schema != {"type": "object"} or simple_name(response_type) in registry or simple_name(response_type) == "Page":
                    success_code = next(iter(op["responses"]))
                    if success_code != "204":
                        op["responses"][success_code]["content"] = {"application/json": {"schema": response_schema}}
                if not op.get("summary"):
                    op["summary"] = infer_summary(java_file.name, method_name, http_method, full_path)
                if not op.get("description"):
                    op["description"] = infer_description(op["summary"], op)
                operations.setdefault(full_path, {})[http_method] = op
    return operations, tags_meta


def deep_merge(base, overlay):
    if isinstance(base, dict) and isinstance(overlay, dict):
        merged = dict(base)
        for key, value in overlay.items():
            if key in merged:
                merged[key] = deep_merge(merged[key], value)
            else:
                merged[key] = value
        return merged
    return overlay


def merge_path_overlays(generated_paths: dict, existing_paths: dict) -> dict:
    merged = json.loads(json.dumps(generated_paths))
    overlay_keys = {"summary", "description", "security", "requestBody", "responses"}
    for path, path_item in existing_paths.items():
        if path not in merged:
            continue
        for method, operation in path_item.items():
            if method not in HTTP_METHODS:
                merged[path][method] = operation
                continue
            if method not in merged[path]:
                merged[path][method] = operation
                continue
            for key in overlay_keys:
                if key in operation:
                    if key in {"summary", "description"}:
                        if merged[path][method].get(key):
                            continue
                        existing_text = operation[key]
                        operation_id = operation.get("operationId", "")
                        if not existing_text:
                            continue
                        if isinstance(existing_text, str):
                            if existing_text == operation_id or re.fullmatch(r"[a-z][A-Za-z0-9]*", existing_text):
                                continue
                    if key == "responses":
                        generated_responses = merged[path][method].get("responses", {})
                        overlay_responses = operation.get("responses", {})
                        if "200" not in generated_responses and "200" in overlay_responses:
                            overlay_responses = {
                                code: response
                                for code, response in overlay_responses.items()
                                if code != "200"
                            }
                        if generated_responses.get("200", {}).get("content"):
                            continue
                        if not overlay_responses:
                            continue
                        operation = {**operation, "responses": overlay_responses}
                    merged[path][method][key] = deep_merge(merged[path][method].get(key), operation[key])
    return merged


def find_schema_refs(value) -> set:
    refs = set()
    if isinstance(value, dict):
        ref = value.get("$ref")
        if isinstance(ref, str) and ref.startswith("#/components/schemas/"):
            refs.add(ref.rsplit("/", 1)[-1])
        for child in value.values():
            refs.update(find_schema_refs(child))
    elif isinstance(value, list):
        for child in value:
            refs.update(find_schema_refs(child))
    return refs


def prune_unused_schemas(spec: dict) -> None:
    schemas = spec.get("components", {}).get("schemas", {})
    used = find_schema_refs(spec.get("paths", {}))
    pending = list(used)
    while pending:
        schema_name = pending.pop()
        schema = schemas.get(schema_name)
        if not schema:
            continue
        for ref_name in find_schema_refs(schema):
            if ref_name not in used:
                used.add(ref_name)
                pending.append(ref_name)
    spec.setdefault("components", {})["schemas"] = {
        name: schema for name, schema in schemas.items() if name in used
    }


def prune_superseded_success_responses(spec: dict) -> None:
    for path_item in spec.get("paths", {}).values():
        if not isinstance(path_item, dict):
            continue
        for method, operation in path_item.items():
            if method not in HTTP_METHODS or not isinstance(operation, dict):
                continue
            responses = operation.get("responses", {})
            if "200" not in responses or not ({"201", "204"} & set(responses)):
                continue
            ok_response = responses.get("200", {})
            if ok_response.get("description") == "OK":
                responses.pop("200", None)


def load_existing_spec() -> dict:
    if OUTPUT.exists():
        return json.loads(OUTPUT.read_text(encoding="utf-8"))
    return {
        "openapi": "3.0.3",
        "info": {
            "title": "Atlas Backend API",
            "version": "0.1.0",
            "description": (
                "Atlas 백엔드 서비스 API 문서입니다. "
                "컨트롤러와 DTO를 기준으로 자동 생성한 REST OpenAPI 명세입니다."
            ),
        },
        "servers": [
            {
                "url": "http://localhost:8080",
                "description": "Local gateway or single service runtime",
            },
            {
                "url": "https://api.example.com",
                "description": "Production placeholder",
            },
        ],
        "components": {
            "schemas": {},
            "securitySchemes": {
                "bearerAuth": {
                    "type": "http",
                    "scheme": "bearer",
                    "bearerFormat": "JWT",
                },
            },
        },
        "security": [
            {
                "bearerAuth": [],
            }
        ],
    }


def main():
    existing = load_existing_spec()
    registry = build_registry()
    components = dict(existing.get("components", {}).get("schemas", {}))

    generated_paths: Dict[str, dict] = {}
    tags = []
    seen_tags = set()
    for service in SERVICES:
        base = ROOT / service / "src/main/java"
        for path in sorted(base.rglob("*Controller.java")):
            java_file = parse_java_file(path)
            if not java_file:
                continue
            controller_paths, controller_tags = parse_controller_methods(java_file, registry, components)
            generated_paths = deep_merge(generated_paths, controller_paths)
            for tag in controller_tags:
                if tag["name"] not in seen_tags:
                    tags.append(tag)
                    seen_tags.add(tag["name"])

    spec = {
        "openapi": existing.get("openapi", "3.0.1"),
        "info": existing.get("info", {"title": "Atlas Backend API", "version": "0.1.0"}),
        "servers": existing.get("servers", []),
        "tags": sorted(tags, key=lambda item: item["name"]),
        "paths": generated_paths,
        "components": deep_merge({"schemas": components}, existing.get("components", {})),
    }
    spec = deep_merge(spec, {k: v for k, v in existing.items() if k not in {"paths", "components", "tags"}})
    spec["paths"] = merge_path_overlays(spec["paths"], existing.get("paths", {}))
    spec["components"] = deep_merge(spec["components"], existing.get("components", {}))
    prune_superseded_success_responses(spec)
    prune_unused_schemas(spec)
    for schema_name, schema in spec.get("components", {}).get("schemas", {}).items():
        if not schema.get("description"):
            schema["description"] = infer_schema_description(schema_name)
        for prop_name, prop_schema in schema.get("properties", {}).items():
            enrich_property_schema(prop_name, prop_schema)
    OUTPUT.write_text(json.dumps(spec, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
