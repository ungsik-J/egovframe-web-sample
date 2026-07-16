package egovframework.example.cmmn;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 특정 디렉토리 내 파일 생성/삭제를 위한 최적화 유틸리티 - java.nio.file (NIO.2) 기반으로 java.io 대비 예외
 * 처리와 성능이 우수함 - 전자정부프레임워크(Egov) 등 Java/Spring 기반 프로젝트에서 공통 유틸로 사용 가능
 */
public final class FileManagerUtil {
	private static final Logger log = LoggerFactory.getLogger(FileManagerUtil.class);

	private FileManagerUtil() {
		// 인스턴스화 방지 (Utility class)
	}

	public static void systemInfo() {
		System.out.println("OS 이름: " + System.getProperty("os.name"));
		System.out.println("OS 버전: " + System.getProperty("os.version"));
		System.out.println("OS 아키텍처: " + System.getProperty("os.arch"));
		System.out.println("사용자 이름: " + System.getProperty("user.name"));
		System.out.println("사용자 홈 디렉토리: " + System.getProperty("user.home"));
		System.out.println("현재 작업 디렉토리: " + System.getProperty("user.dir"));
		System.out.println("Java 버전: " + System.getProperty("java.version"));
		System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
		System.out.println("파일 구분자: " + System.getProperty("file.separator"));
		System.out.println("경로 구분자: " + System.getProperty("path.separator"));
		System.out.println("줄바꿈 문자: " + System.getProperty("line.separator"));

		OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

		System.out.println("OS 이름: " + osBean.getName());
		System.out.println("OS 버전: " + osBean.getVersion());
		System.out.println("아키텍처: " + osBean.getArch());
		System.out.println("가용 프로세서 수: " + osBean.getAvailableProcessors());
		System.out.println("시스템 부하 평균: " + osBean.getSystemLoadAverage());

	}

	// 사용 예시
	public static void main(String[] args) {
		try {

			String filePath = "/home/john/devHome/file/create/";
			String fileName = "chunkFile";
			
		
			// 파일 삭제
			int deleted = deleteAllFilesByName(filePath, fileName);
			log.info("삭제 여부: " + deleted);
			
			// 파일 생성
			if(deleted > 0) {
				Path created = createFile(filePath, fileName, "테스트 내용입니다.");
				log.info("생성됨: " + created);

				log.info("getFiles: {}", Files.exists(created));

			}
		
			// 하루(24시간) 지난 파일 정리
			deleteOldFiles(filePath, 24 * 60 * 60 * 1000L);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 지정된 디렉토리에 파일을 생성한다. 디렉토리가 없으면 자동으로 생성한다.
	 *
	 * @param dirPath  파일을 생성할 디렉토리 경로 (예: "/data/upload")
	 * @param fileName 생성할 파일명 (예: "test.txt")
	 * @param content  파일에 쓸 내용 (null이면 빈 파일 생성)
	 * @return 생성된 파일의 Path
	 * @throws IOException 디렉토리 생성 또는 파일 생성 실패 시
	 */
	public static Path createFile(String dirPath, String fileName, String content) throws IOException {
		if (dirPath == null || dirPath.isBlank()) {
			throw new IllegalArgumentException("디렉토리 경로는 필수입니다.");
		}
		if (fileName == null || fileName.isBlank()) {
			throw new IllegalArgumentException("파일명은 필수입니다.");
		}

		Path directory = Paths.get(dirPath).normalize();
		Path targetFile = directory.resolve(fileName).normalize();

		// 경로 조작(Path Traversal) 방지: 대상 파일이 지정 디렉토리 하위인지 검증
		if (!targetFile.startsWith(directory)) {
			throw new SecurityException("잘못된 파일 경로입니다: " + fileName);
		}

		// 디렉토리가 없으면 생성 (이미 존재해도 예외 없음)
		Files.createDirectories(directory);

		// 파일이 이미 존재하면 예외 대신 덮어쓰기 여부를 명시적으로 처리
		if (content == null) {
			// 빈 파일 생성 (이미 있으면 그대로 둠)
			Files.createFile(targetFile);
		} else {
			// 내용 있는 파일 생성/덮어쓰기 - StandardOpenOption으로 명시적 제어
			Files.writeString(targetFile, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING);
		}

		return targetFile;
	}

	/**
	 * 지정된 디렉토리 내 특정 파일을 삭제한다.
	 *
	 * @param dirPath  대상 디렉토리 경로
	 * @param fileName 삭제할 파일명
	 * @return 삭제 성공 여부 (파일이 없었으면 false)
	 * @throws IOException 삭제 실패 시 (권한 문제 등)
	 */
	public static boolean deleteFile(String dirPath, String fileName, boolean isDeleted) throws IOException {

		Path directory = Paths.get(dirPath).normalize();
		Path targetFile = directory.resolve(fileName).normalize();

		if (!targetFile.startsWith(directory)) {
			throw new SecurityException("잘못된 파일 경로입니다: " + fileName);
		}

		// deleteIfExists: 파일 존재 여부 체크 + 삭제를 원자적으로 처리 (race condition 방지)
		return (isDeleted ? Files.deleteIfExists(targetFile) : false);
	}

	/**
     * dirPath 하위(서브디렉토리 포함)에서 fileName과 이름이 일치하는 모든 파일을 삭제한다.
     *
     * @param dirPath  검색을 시작할 최상위 디렉토리
     * @param fileName 삭제할 파일명 (예: "temp.log")
     * @return 실제로 삭제된 파일의 개수
     */
    public static int deleteAllFilesByName(String dirPath, String fileName) throws IOException {
        Path directory = Paths.get(dirPath).normalize();

        if (!Files.isDirectory(directory)) {
            throw new IllegalArgumentException("디렉토리가 아닙니다: " + dirPath);
        }

        // 경로 조작(Path Traversal) 방지: fileName 자체에 구분자가 들어오지 못하게 차단
        if (fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            throw new SecurityException("잘못된 파일명입니다: " + fileName);
        }

        int deletedCount = 0;

        // 하위 디렉토리까지 재귀적으로 탐색
        try (Stream<Path> list = Files.list(directory)) {
            List<Path> targets = list
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().startsWith(fileName))
                    .collect(Collectors.toList());

            log.info("targets:{}", targets);
            
            for (Path target : targets) {
                if (Files.deleteIfExists(target)) {
                    deletedCount++;
                }
            }
        }

        return deletedCount;
    }

	/**
	 * 지정된 디렉토리 하위의 모든 파일과 하위 디렉토리를 삭제한다. (디렉토리 자체는 유지) Files.walk를 사용하여 대량 파일도
	 * 효율적으로 처리한다.
	 *
	 * @param dirPath 정리할 디렉토리 경로
	 * @throws IOException 삭제 중 오류 발생 시
	 */
	public static void clearDirectory(String dirPath) throws IOException {
		Path directory = Paths.get(dirPath).normalize();

		if (!Files.exists(directory)) {
			return; // 삭제할 대상 자체가 없으면 종료
		}

		// try-with-resources로 Stream 리소스 누수 방지 (NIO Stream은 반드시 close 필요)
		try (Stream<Path> walk = Files.walk(directory)) {
			walk.sorted((a, b) -> b.compareTo(a)) // 하위 파일부터 삭제되도록 역순 정렬
					.filter(path -> !path.equals(directory)) // 최상위 디렉토리 자체는 제외
					.forEach(path -> {
						try {
							Files.delete(path);
						} catch (IOException e) {
							throw new RuntimeException("삭제 실패: " + path, e);
						}
					});
		}
	}

	/**
	 * 파일 생성 시각(마지막 수정 시각) 기준으로 지정 시간이 지난 파일들을 삭제한다. (예: 임시 파일 정리 배치 작업에 활용)
	 *
	 * @param dirPath      대상 디렉토리
	 * @param maxAgeMillis 이 시간(ms)보다 오래된 파일 삭제
	 * @throws IOException 처리 중 오류 발생 시
	 */
	public static void deleteOldFiles(String dirPath, long maxAgeMillis) throws IOException {
		Path directory = Paths.get(dirPath).normalize();
		if (!Files.exists(directory)) {
			return;
		}

		long now = System.currentTimeMillis();

		try (Stream<Path> walk = Files.walk(directory, 1)) { // depth=1: 하위 파일만 검사 (재귀 없음)
			walk.filter(Files::isRegularFile).forEach(path -> {
				try {
					BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
					long lastModified = attrs.lastModifiedTime().toMillis();
					if (now - lastModified > maxAgeMillis) {
						Files.delete(path);
					}
				} catch (IOException e) {
					throw new RuntimeException("오래된 파일 삭제 실패: " + path, e);
				}
			});
		}
	}
}
