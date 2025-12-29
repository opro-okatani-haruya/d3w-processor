package d3w;

import lombok.Data;
import lombok.var;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/**
 * 一時ディレクトリを管理し、スコープを抜けると自動削除するクラス
 */
@Data
public class TempDirectory implements AutoCloseable {
    private final Path path;

    private TempDirectory(Path path) {
        this.path = path;
    }

    /**
     * 一時ディレクトリを作成
     *
     * @param prefix ディレクトリ名のプレフィックス
     * @return TempDirectoryインスタンス
     */
    public static TempDirectory create(String prefix) throws IOException {
        Path tempDir = Files.createTempDirectory(prefix);
        return new TempDirectory(tempDir);
    }

    @Override
    public void close() {
        deleteDirectory(path);
    }

    /**
     * ディレクトリを再帰的に削除
     */
    private static void deleteDirectory(Path directory) {
        if (directory == null || !Files.exists(directory)) {
            return;
        }

        try (var stream = Files.walk(directory)) {
            stream.sorted(Comparator.reverseOrder()) // 深い階層から削除
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("警告: ファイル削除失敗: " + path + " - " + e.getMessage());
                        }
                    });
        } catch (IOException e) {
            System.err.println("警告: ディレクトリ削除失敗: " + directory + " - " + e.getMessage());
        }
    }
}
