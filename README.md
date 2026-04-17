# 공급망 리스크 오케스트레이션 및 협력사 거버넌스 시스템

공급망 리스크 발생 시 영향도를 계산하고, 대응 권고안을 생성하며, 의사결정·실행·이행·사후평가까지 관리하는 운영형 워크플로우 시스템이다.

플랫폼 구조는 범용 공급망을 지향하지만, MVP 검증 시나리오는 식자재 공급망으로 설정한다. 이를 통해 메인 발주사, 1차 협력사, 2차 협력사, 3차 협력사 구조를 공통으로 다루면서도 실제 데모에서는 품목, 인증, 원산지, 유통기한, 보관 조건 등 구체적인 리스크를 명확하게 보여준다.

<details>
<summary>프로젝트 한눈에 보기</summary>

| 항목 | 내용 |
| --- | --- |
| 캠프 주제 대응 | `3. 공급망 관리를 위한 주문 관리 시스템` |
| 플랫폼 성격 | 범용 공급망 운영 플랫폼 |
| MVP 검증 도메인 | 식자재 공급망 |
| 핵심 가치 | 리스크 감지, 권고안 생성, 실행 추적, 사후평가, 협력사 거버넌스 |

## 해결하려는 문제

| 문제 | 설명 |
| --- | --- |
| 병목 원인 파악 지연 | 메인 발주사는 1차 협력사 지연만 보게 되고 실제 병목 원인이 2차, 3차에 있어도 빠르게 파악하기 어렵다. |
| 대체 공급처 판단 지연 | 공급 차질이 발생했을 때 어떤 대체 공급처를 연결해야 하는지 판단이 늦어진다. |
| 정보 분산 | 협력사 간 관계, 납기 성과, 품질 상태, ESG 상태, 인증 정보가 흩어져 있어 대응 속도가 느리다. |
| 거버넌스 약화 | 권고안을 제시하더라도 실제 수용 여부와 결과를 추적하지 않으면 공급망 거버넌스가 약해진다. |

## 핵심 기능

| 기능 | 설명 |
| --- | --- |
| 공급 차질 대응 오케스트레이션 | 공급 차질 이벤트를 수집하고 영향 PO와 납기 리스크를 계산한다. 이슈 tier에 따라 대체 2차 또는 대체 1차 권고안을 생성한다. |
| 품질/인증 이슈 추적 및 대체 공급처 검토 | lot 기반 추적, 영향 범위 식별, 격리 여부 판단을 지원한다. 필요 시 대체 공급처를 다시 탐색한다. |
| 권고안 이행 및 사후평가 거버넌스 | 권고안 수용/미수용과 미수용 사유를 기록한다. 실행 상태, 납기 회복 여부, 품질 결과, 비용 영향, ESG 영향을 사후평가에 반영한다. |

## 시나리오

| 구분 | 설명 |
| --- | --- |
| 메인 시나리오 1 | 공급 차질 발생 시 대체 공급처 권고안 오케스트레이션 |
| 메인 시나리오 2 | 품질 또는 인증 이슈 기반 lot 추적 및 대체 공급처 검토 |
| 메인 시나리오 3 | 권고안 이행 및 사후평가 거버넌스 |
| 보조 시나리오 | 물류 지연, ESG 기준 미달 |
| 확장 시나리오 | Capacity 부족 / 분산 발주 |

<details>
<summary>상세 시나리오 보기</summary>

### 메인 시나리오 1: 공급 차질 발생 시 대체 공급처 권고안 오케스트레이션

1. 메인 발주사가 1차 협력사 A에 특정 품목 발주를 넣는다.
2. 공급망에서 병목 또는 직접 이슈가 발생한다.
3. 시스템은 이벤트 유형을 분류하고 영향을 받는 PO와 납기 리스크를 계산한다.
4. 이슈 위치에 따라 권고안 대상을 다르게 잡는다.
5. 2차 병목이면 1차 협력사에게 대체 2차 후보를 제시한다.
6. 1차 직접 이슈이면 메인 발주사에게 대체 1차 후보를 제시한다.
7. 운영자는 권고안 점수, 사유, 예상 회복 납기를 확인하고 다음 의사결정을 시작한다.

#### 메인 시나리오 1 흐름도

```mermaid
flowchart TB
A["공급 차질 또는<br/>직접 이슈 발생"] --> B["Kafka<br/>이벤트 수집"]
A --> C["이슈 위치 / 유형<br/>분류"]
B --> D["영향 PO 및<br/>납기 리스크 계산"]
C --> D
D --> E{"어느 tier가<br/>문제인가"}
E -->|"2차 병목"| F["1차 대상<br/>대체 2차 후보 탐색"]
E -->|"1차 직접 이슈"| G["메인 발주사 대상<br/>대체 1차 후보 탐색"]
F --> H["권고안 점수화 /<br/>회복 납기 비교"]
G --> H
H --> I["권고안 확인 및<br/>의사결정 시작"]
```

#### 메인 시나리오 1

```mermaid
sequenceDiagram
participant MB as "메인 발주사 운영자"
participant S1 as "1차 협력사 A"
participant S2 as "2차 협력사 B"
participant K as "Kafka"
participant CT as "Control Tower"
participant RE as "Recommendation Engine"

MB->>S1: "PO 발주"
alt 2차 병목 발생
    S1->>S2: "핵심 원재료 공급 요청"
    S2-->>K: "supplier_issue / shipment_delayed"
    K->>CT: "2차 병목 이벤트 전달"
    CT->>CT: "영향 PO 및 1차 영향 계산"
    CT->>RE: "대체 2차 후보 탐색 요청"
    RE-->>S1: "대체 2차 후보 C, D, E 반환"
    CT-->>MB: "영향 주문 / 회복 예상 납기 표시"
else 1차 직접 이슈 발생
    S1-->>K: "supplier_issue / quality_issue / esg_alert"
    K->>CT: "1차 직접 이슈 전달"
    CT->>CT: "영향 PO 및 납기 리스크 계산"
    CT->>RE: "대체 1차 후보 탐색 요청"
    RE-->>CT: "대체 1차 후보 X, Y, Z 반환"
    CT-->>MB: "우회 발주 검토 화면 표시"
end
```

### 메인 시나리오 2: 품질 또는 인증 이슈 기반 lot 추적 및 대체 공급처 검토

1. 특정 lot에서 품질 이슈가 발생하거나 인증서가 만료된다.
2. 시스템은 해당 lot와 연결된 주문 및 협력사 체인을 추적한다.
3. 시스템은 영향 범위를 계산하고 격리 필요 lot를 식별한다.
4. 운영자는 대체 공급처 검토 또는 lot 격리 조치를 판단한다.
5. 필요하면 추천 엔진이 대체 가능한 1차 또는 2차 후보를 다시 탐색한다.

#### 메인 시나리오 2 흐름도

```mermaid
flowchart TB
A["품질 이슈 또는<br/>인증 만료"] --> B["lot / 공급망 체인<br/>추적"]
A --> C["인증 상태 /<br/>문서 확인"]
B --> D["영향 주문 및<br/>협력사 범위 식별"]
C --> E["lot 격리 여부<br/>판단"]
D --> F["대체 공급처<br/>검토"]
E --> F
F --> G["격리 / 전환 / 유지<br/>의사결정"]
```

#### 메인 시나리오 2

```mermaid
sequenceDiagram
participant QA as "품질 / 인증 시스템"
participant K as "Kafka"
participant CT as "Control Tower"
participant TR as "Traceability Engine"
participant RE as "Recommendation Engine"
participant MB as "메인 발주사 운영자"

QA-->>K: "quality_issue / certificate_expired"
K->>CT: "품질 또는 인증 이슈 전달"
CT->>TR: "lot / 공급망 체인 추적 요청"
TR-->>CT: "영향 주문 / 협력사 범위 반환"
CT->>RE: "대체 공급처 재탐색 요청"
RE-->>CT: "대체 후보 반환"
CT-->>MB: "격리 범위 / 대체 공급처 검토 지원"
```

### 메인 시나리오 3: 권고안 이행 및 사후평가 거버넌스

1. 시스템이 리스크 대응 권고안을 생성한다.
2. 권고안 수신자는 수용 또는 미수용 여부를 결정한다.
3. 미수용 시 사유를 입력하고, 필요하면 운영자가 재검토나 에스컬레이션을 진행한다.
4. 수용 시 시스템은 우회 발주, 공급처 전환, lot 격리, 긴급 운송 같은 실행 태스크를 생성한다.
5. 실행 상태는 진행 중, 완료, 실패 단위로 추적된다.
6. 이후 납기 회복 여부, 품질 결과, 비용 영향, ESG 영향까지 사후평가에 반영한다.

#### 메인 시나리오 3 흐름도

```mermaid
flowchart TB
A["권고안 생성"] --> B["수용 / 미수용<br/>결정"]
B -->|"수용"| C["실행 태스크 생성"]
B -->|"미수용"| D["미수용 사유 기록 /<br/>재검토"]
C --> E["실행 상태 추적"]
D --> E
E --> F["납기 / 품질 / 비용 / ESG<br/>사후평가"]
F --> G["권고안 성과 축적 /<br/>거버넌스 대시보드 반영"]
```

#### 메인 시나리오 3

```mermaid
sequenceDiagram
participant CT as "Control Tower"
participant RCV as "권고안 수신자"
participant WF as "Workflow Engine"
participant GOV as "Governance Board"
participant MB as "메인 발주사 운영자"

CT-->>RCV: "권고안 전달"
CT-->>MB: "권고안 검토 요청"
alt 권고안 수용
    RCV->>WF: "수용 및 실행 요청"
    WF->>WF: "실행 태스크 생성"
    WF-->>CT: "진행 상태 업데이트"
else 권고안 미수용
    RCV->>WF: "미수용 사유 제출"
    WF-->>MB: "재검토 또는 에스컬레이션 요청"
end
CT->>GOV: "납기 / 품질 / 비용 / ESG 결과 반영"
GOV-->>MB: "사후평가 대시보드 갱신"
```

### 보조 시나리오

#### 1. 물류 지연 시나리오

1. 항만 또는 내륙운송 단계에서 `shipment_delayed` 이벤트가 발생한다.
2. 시스템은 ETA를 재계산하고 납기 위험 주문을 갱신한다.
3. 운영자는 긴급 우회 운송, 부분 선적, 대체 공급처 검토 여부를 판단한다.

##### 보조 시나리오 1 흐름도

```mermaid
flowchart TB
A["shipment_delayed<br/>발생"] --> B["ETA 재계산"]
A --> C["배송 이슈<br/>기록"]
B --> D["지연 위험 주문<br/>갱신"]
C --> E["우회 운송 옵션<br/>탐색"]
D --> F["운영자 판단"]
E --> F
```

##### 보조 시나리오 1

```mermaid
sequenceDiagram
participant SH as "Shipment"
participant K as "Kafka"
participant CT as "Control Tower"
participant ETA as "ETA Calculator"
participant MB as "메인 발주사 운영자"

SH-->>K: "shipment_delayed"
K->>CT: "배송 지연 이벤트 전달"
CT->>ETA: "ETA 재계산 요청"
ETA-->>CT: "갱신 ETA 반환"
CT->>CT: "지연 위험 주문 갱신"
CT-->>MB: "긴급 우회 운송 / 부분 선적 검토 알림"
```

#### 2. ESG 기준 미달 시나리오

1. 특정 협력사의 ESG 점수가 임계값 아래로 하락하거나 중대 이슈가 발생한다.
2. 시스템은 해당 협력사를 신규 추천에서 제외하거나 강하게 감점한다.
3. 운영자는 기존 거래 유지 여부와 대체 공급처 전환 필요성을 검토한다.

##### 보조 시나리오 2 흐름도

```mermaid
flowchart TB
A["ESG 점수 하락<br/>또는 중대 이슈"] --> B["후보 감점 /<br/>제외 규칙 적용"]
A --> C["ESG 이슈<br/>등록"]
B --> D["추천 결과<br/>재계산"]
C --> E["기존 거래<br/>유지 여부 검토"]
D --> F["대체 공급처<br/>전환 판단"]
E --> F
```

##### 보조 시나리오 2

```mermaid
sequenceDiagram
participant ESG as "ESG Assessment"
participant K as "Kafka"
participant CT as "Control Tower"
participant RE as "Recommendation Engine"
participant MB as "메인 발주사 운영자"

ESG-->>K: "esg_score_updated / esg_alert"
K->>CT: "ESG 이슈 전달"
CT->>RE: "후보 감점 / 제외 규칙 재적용"
RE-->>CT: "수정된 추천 결과 반환"
CT-->>MB: "거래 유지 여부 / 대체 공급처 전환 검토 알림"
```

### 확장 시나리오

#### 1. Capacity 부족 / 분산 발주 시나리오

1. 기존 1차 또는 2차 협력사가 품질 문제는 없지만 전체 주문 수량을 모두 소화하지 못한다.
2. 시스템은 부족 수량과 우선 납기 주문을 계산한다.
3. 시스템은 기존 공급처 유지 물량과 대체 공급처 분산 물량 조합을 제안한다.
4. 운영자는 전량 전환이 아니라 `부분 유지 + 부분 대체` 전략을 검토한다.

##### 확장 시나리오 흐름도

```mermaid
flowchart TB
A["공급 capacity 부족"] --> B["부족 수량 계산"]
A --> C["우선 납기 주문 식별"]
B --> D["대체 공급처 분산안 생성"]
C --> D
D --> E["부분 유지 + 부분 대체<br/>전략 비교"]
E --> F["분산 발주 의사결정"]
```

##### 확장 시나리오

```mermaid
sequenceDiagram
participant SUP as "기존 공급처"
participant K as "Kafka"
participant CT as "Control Tower"
participant RE as "Recommendation Engine"
participant MB as "메인 발주사 운영자"

SUP-->>K: "capacity_shortage"
K->>CT: "처리 가능 수량 부족 이벤트 전달"
CT->>CT: "부족 수량 / 우선 주문 계산"
CT->>RE: "분산 발주 대안 탐색 요청"
RE-->>CT: "부분 유지 + 부분 대체 조합 반환"
CT-->>MB: "분산 발주 전략 비교 화면 제공"
```

</details>

## 차별화 포인트

| 포인트 | 설명 |
| --- | --- |
| 운영 흐름 완결성 | 단순 주문 관리가 아니라 `리스크 감지 -> 권고안 생성 -> 실행 추적 -> 사후평가`까지 연결한다. |
| 다단계 분석 | 공급망 네트워크 기반으로 1차, 2차, 3차 병목을 분석한다. |
| 정책 반영 | ESG와 인증 정보를 권고안 점수에 반영한다. |
| 거버넌스 강화 | 협력사별 수용률과 회복 성과를 관리해 공급망 거버넌스를 강화한다. |
| 확장성 | 블록체인, 디지털 트윈 Lite, AI 고도화, MSA 확장까지 연결 가능한 구조를 가진다. |

## MVP 범위

| 구분 | 내용 |
| --- | --- |
| 운영 화면 | 메인 발주사 기준 Control Tower |
| 공급망 가시화 | 1차, 2차, 3차 Supplier Network 시각화 |
| 이벤트 처리 | 이벤트 기반 병목 감지 |
| 분석 | 영향 주문 계산 |
| 권고안 | 대체 2차 / 대체 1차 권고안 생성 |
| 거버넌스 | 권고안 수용/미수용 기록, 사후 결과 평가 |
| 정책 | ESG 점수 1차 버전 |

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | `Spring Boot`, `Spring Security`, `JWT 심화`, `Spring Data JPA`, `QueryDSL`, `Spring Batch` |
| Frontend | `Vue` |
| Realtime | `WebSocket`, `STOMP` |
| Data | `MariaDB` 또는 `MySQL`, `MongoDB`, `Redis`, `Elasticsearch` |
| Event | `Kafka` |
| AI | `Spring AI`, `sLLM` |
| Infra | `Docker`, `Kubernetes` |
| CI/CD | `GitHub Actions` |

## 기능 영역별 최종 적용 기술

| 기능 영역 | 최종 적용 기술 | 적용 방향 |
| --- | --- | --- |
| 인증/권한 | `JWT 심화` | 메인 발주사 운영자, 1차 협력사 담당자, 관리자 역할을 분리한다. |
| 주문/공급망 기준 데이터 관리 | `QueryDSL` | PO, 공급업체, 품목, 공급망 링크, 권고안, 사후평가를 복합 조건으로 조회한다. |
| 이벤트 기반 리스크 오케스트레이션 | `Kafka` | `supplier_issue`, `shipment_delayed`, `quality_issue`, `esg_alert` 같은 이벤트를 기반으로 오케스트레이션을 수행한다. |
| 실시간 대시보드/알림 | `WebSocket/STOMP` | Control Tower 실시간 상태 반영과 권고안 생성/이슈 알림을 화면에 즉시 전달한다. |
| 배치/집계 | `Spring Batch` | 유통기한 임박 계산, 협력사 납기 준수율, 권고안 수용률, 회복 성공률을 주기적으로 집계한다. |
| 메인 DB/보조 저장소 | `MariaDB/MySQL`, `MongoDB` | 메인 트랜잭션 데이터와 AI/비정형 보조 데이터를 함께 관리한다. |
| 캐시 | `Redis` | 대시보드 수치, 최근 권고안, 세션/토큰 보조 데이터를 캐싱한다. |
| 검색 최적화 | `Elasticsearch` | 공급처, 품목, lot, 인증서, 이력 검색을 최적화한다. |
| CI/CD | `GitHub Actions` | 자동 테스트, 빌드, 배포 파이프라인을 구성한다. |
| 배포 인프라 | `Docker`, `Kubernetes` | 컨테이너 기반 배포와 운영 환경을 구성한다. |
| AI/권고안 고도화 | `Spring AI`, `sLLM` | 규칙 기반 권고안 위에 이슈 해석, 가중치 조정, 설명 생성 보조 계층을 둔다. |
| 아키텍처 | `이벤트 기반 분리 구조` | 운영형 워크플로우 중심으로 서비스를 느슨하게 분리하고, 추후 MSA로 확장 가능하게 설계한다. |

### 최종 채택 요약

| 구분 | 기술 |
| --- | --- |
| 우선 채택 | `JWT 심화`, `QueryDSL`, `Kafka`, `WebSocket/STOMP`, `Spring Batch`, `Elasticsearch`, `GitHub Actions` |
| 여유 있으면 채택 | `Prometheus + Grafana`, `Spring AI + sLLM` |

### 팀 회의 반영 포인트

| 제안자 | 회의 반영 내용 | 연결되는 기술/기능 |
| --- | --- | --- |
| 정윤 | 문서/인증 만료 사전 경고 기능을 강화하고, 사전 예방형 리스크 관리 축과 검색 기능 고도화를 함께 고려한다. | `Spring Batch`, `Elasticsearch`, 문서/인증 만료 경고 |
| 병찬 | 대안 권고와 추적 항목을 더 세분화하고, 여러 벤더와 원활히 소통할 수 있도록 채팅 기능을 검토한다. | 권고안 이행 추적, `WebSocket/STOMP`, 채팅 기능 |
| 강현&도균 | 이슈 해결에 가장 적합한 업체를 추천하는 로직과, 이슈 현황 및 납기 영향을 보여주는 대시보드 축을 강화한다. | 추천 로직, `Kafka`, `QueryDSL`, Control Tower 대시보드 |
| 태환 | sLLM 기반 적합 업체 추천 고도화를 검토하고, 채팅 또는 메일 시스템, 발주 시 문서 첨부 및 전자계약/인증 흐름까지 확장 가능성을 본다. | `Spring AI`, `sLLM`, 채팅/메일, 문서 첨부, 전자계약/인증 |

## 트렌드 및 사례

| 구분 | 내용 | 링크 |
| --- | --- | --- |
| 트렌드 1 | 글로벌 기업들은 1차 공급처만이 아니라 다단계 공급망과 물류 흐름을 함께 보는 방향으로 확장하고 있다. |  |
| 트렌드 2 | Control Tower에 AI, 디지털 트윈, 실시간 ETA, ESG 데이터를 결합하는 흐름이 강화되고 있다. |  |
| PepsiCo | Siemens·NVIDIA와 함께 공장·창고·end-to-end supply chain을 AI와 디지털 트윈으로 전환하는 협업을 공식 발표했다. | [PepsiCo 공식](https://www.pepsico.com/newsroom/press-releases/2025/pepsico-announces-industry-first-ai-and-digital-twin-collaboration-with-siemens-and-nvidia) |
| Coca-Cola HBC | Shippeo를 통해 실시간 운송 가시성과 ETA 공유를 도입하고 SAP 및 기존 시스템과의 연동을 공식 자료에서 강조했다. | [Coca-Cola HBC 공식](https://www.coca-colahellenic.com/content/dam/cch/us/documents/media/news/CCHBC%20enables%20real-time%20delivery%20tracking%20for%20customers%20by%20partnering%20with%20Shippeo_Press%20Release.pdf.downloadasset.pdf) |
| Zespri | SAP `S/4HANA`, `IBP`, `Sustainability Control Tower` 기반 공급망 디지털 전환 확대 사례로 소개됐다. | [SAP 공식](https://news.sap.com/sea/2024/10/zespri-upgrades-sap-s-4hana-digital-core-and-begins-next-stage-of-supply-chain-digital-transformation/) |

## 기대 효과

| 항목 | 설명 |
| --- | --- |
| 운영 효율 향상 | 병목 발생 시 원인 파악과 대응 결정 시간을 줄인다. |
| 납기 리스크 완화 | 영향 주문을 빠르게 식별하고 회복 가능한 대안을 제시한다. |
| 품질/인증 대응 강화 | lot 추적과 인증 검증을 통해 품질 리스크 대응력을 높인다. |
| 협력사 거버넌스 강화 | 권고안 수용률과 사후 결과를 관리해 공급망 대응 기준을 표준화한다. |
| 확장 가능성 확보 | 블록체인, 디지털 트윈 Lite, AI 고도화, MSA 확장까지 자연스럽게 연결할 수 있다. |

</details>

<details>
<summary>요구사항명세서</summary>

[요구사항 명세서](https://docs.google.com/spreadsheets/d/1-9XXxS_f5A81Zk5Z6et8IepxUo-Pm2fp-omdqqYuDvY/edit?gid=0#gid=0)

</details>

<details>
<summary>ERD</summary>

![ERD](images/erd.png)

</details>

<details>
<summary>디자인</summary>

<a href="https://stitch.withgoogle.com/projects/3858843644991271330">
  <img src="images/screen.png" alt="Google Stitch 디자인 미리보기" width="900" />
</a>

이미지를 클릭하면 Google Stitch 프로젝트로 이동한다.
직접 열기: [Google Stitch](https://stitch.withgoogle.com/projects/3858843644991271330)
</details>

<details>
<summary>프로그램 사양서 및 단위테스트결과서</summary>

클릭 -> [Swagger hub link](https://app.swaggerhub.com/apis-docs/personal-359/atlas-backend-api/0.1.0 ) 

## Swagger / OpenAPI

Spring Boot 애플리케이션 실행 후 서비스별 Swagger UI는 아래 경로로 접근한다.

| 서비스 | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| auth-service | `/api/auth/swagger-ui.html` | `/api/auth/v3/api-docs` |
| supply-service | `/api/supply/swagger-ui.html` | `/api/supply/v3/api-docs` |
| control-service | `/api/control/swagger-ui.html` | `/api/control/v3/api-docs` |
| file-service | `/api/files/swagger-ui.html` | `/api/files/v3/api-docs` |

참고:

- `auth-service` 문서에는 JWT Bearer 인증 스키마가 포함된다.
- 일부 공급망 컨트롤러는 `ResponseEntity<?>`를 사용하므로 응답 스키마가 문서에서 일반 `object`로 보일 수 있다.
- `supply-service`, `control-service`, `file-service`의 일부 엔드포인트는 API Gateway가 전달하는 `X-Organization-Public-Id`, `X-User-Public-Id` 헤더를 사용한다.
- SwaggerHub 업로드용 통합 스펙 초안 파일은 [docs/openapi/atlas-backend-openapi.json](/Users/iamxoghks/Documents/GitHub/be23-fin-team1-Atlas-be/docs/openapi/atlas-backend-openapi.json)에 둔다.
- SwaggerHub 문서형 페이지는 [atlas-backend-api docs](https://app.swaggerhub.com/apis-docs/personal-359/atlas-backend-api/0.1.0?view=uiDocs)에서 확인한다.
- 이 통합 스펙은 소스 컨트롤러 기준 초안이라, 운영 전에는 예시값과 응답 스키마를 보강하는 것을 권장한다.


</details>