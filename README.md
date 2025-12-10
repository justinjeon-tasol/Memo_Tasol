# FileShare - 문서 관리 앱

안드로이드 앱으로 사진이 포함된 문서를 안전하게 관리하고 공유할 수 있습니다.

## 주요 기능

### 📱 핵심 기능
- **멀티 이미지 지원** - 문서당 여러 이미지 저장 및 관리
- **동적 카테고리** - 카테고리 추가, 수정, 삭제 가능
- **검색 기능** - 제목과 메모로 빠른 검색
- **이미지 확대/축소** - 핀치 줌으로 이미지 확대 (최대 500%)
- **보안 기능** - PIN 및 생체 인증으로 앱 잠금
- **공유** - 카카오톡 등 다양한 앱으로 문서 공유

### 📂 카테고리 관리
- 기본 카테고리: 통장사본, 사업자등록증, 제품사진, 기타
- 사용자 정의 카테고리 추가 가능
- 카테고리별 필터링

### 🔍 검색
- 실시간 검색
- 제목 및 메모 내용 검색
- 검색 결과 즉시 표시

### 🖼️ 이미지 관리
- 갤러리에서 여러 이미지 선택
- 카메라로 직접 촬영
- 이미지 스와이프로 탐색
- 이미지 클릭 시 전체화면 확대
- 핀치 줌 및 드래그 지원

## 기술 스택

- **언어**: Kotlin
- **UI**: Jetpack Compose
- **아키텍처**: MVVM
- **로컬 DB**: Room Database
- **이미지 로딩**: Coil
- **비동기**: Kotlin Coroutines & Flow
- **최소 SDK**: 26 (Android 8.0)
- **타겟 SDK**: 36

## 프로젝트 구조

```
app/
├── data/
│   ├── local/
│   │   ├── entity/          # Room 엔티티
│   │   ├── dao/             # Database Access Objects
│   │   └── AppDatabase.kt   # Room 데이터베이스
│   └── repository/          # 데이터 저장소
├── domain/
│   └── model/               # 도메인 모델
├── ui/
│   ├── components/          # 재사용 가능한 UI 컴포넌트
│   ├── screens/             # 화면 Composables
│   │   ├── main/           # 메인 화면
│   │   ├── detail/         # 문서 상세
│   │   ├── add/            # 문서 추가/수정
│   │   ├── settings/       # 설정
│   │   └── lock/           # 잠금 화면
│   └── theme/              # 테마 및 스타일
├── viewmodel/              # ViewModels
└── util/                   # 유틸리티 클래스
```

## 설치 및 실행

### 요구사항
- Android Studio Hedgehog | 2023.1.1 이상
- JDK 17 이상

### 실행 방법
1. 저장소 클론
```bash
git clone https://github.com/YOUR_USERNAME/fileshare.git
cd fileshare
```

2. Android Studio에서 프로젝트 열기

3. Gradle Sync 실행

4. 에뮬레이터 또는 실제 기기에서 실행

## APK 빌드

### Debug APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

출력 위치: `app/build/outputs/apk/debug/app-debug.apk`

### Release APK
1. 서명 키 생성 (처음 한 번만)
```bash
keytool -genkey -v -keystore release-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias fileshare-key
```

2. `app/build.gradle.kts`에 서명 설정 추가

3. Build → Generate Signed Bundle / APK

## 데이터베이스 스키마

### Categories (카테고리)
- `id`: Long (PK)
- `name`: String
- `displayOrder`: Int
- `isDefault`: Boolean
- `createdAt`: Long

### Documents (문서)
- `id`: Long (PK)
- `title`: String
- `categoryId`: Long (FK)
- `fileUri`: String (첫 번째 이미지)
- `memo`: String?
- `createdAt`: Long
- `updatedAt`: Long
- `shareCount`: Int

### DocumentImages (문서 이미지)
- `id`: Long (PK)
- `documentId`: Long (FK, CASCADE)
- `imageUri`: String
- `displayOrder`: Int
- `createdAt`: Long

## 주요 화면

### 메인 화면
- 문서 목록 (이미지 썸네일, 제목, 카테고리, 날짜)
- 카테고리 필터
- 검색 바
- 다중 선택 및 공유

### 문서 상세
- 이미지 스와이프 뷰
- 페이지 인디케이터
- 문서 정보 (제목, 카테고리, 메모)
- 메타데이터 (생성일, 수정일, 공유 횟수)
- 공유 버튼

### 문서 추가/수정
- 멀티 이미지 선택
- 카메라 촬영
- 카테고리 선택
- 제목 및 메모 입력

### 설정
- 앱 잠금 설정
- PIN 변경
- 생체 인증 설정
- 카테고리 관리
- 앱 정보

## 라이선스

이 프로젝트는 개인 프로젝트이며, 자유롭게 사용 및 수정 가능합니다.

## 개발자

프로젝트에 대한 문의는 이슈를 통해 남겨주세요.

---

**Made with ❤️ using Jetpack Compose**
