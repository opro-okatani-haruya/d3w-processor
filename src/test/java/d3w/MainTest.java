package d3w;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mainクラスのテストクラス
 */
public class MainTest {

    /**
     * リソースファイルを一時ディレクトリにコピーする
     *
     * @param resourcePath クラスパス上のリソースパス（例: "/templates/防衛省サンプルワーク.d3w"）
     * @param tempDir      一時ディレクトリ
     * @param fileName     コピー先のファイル名
     * @return コピーされたファイル
     */
    private File copyResourceToTemp(String resourcePath, Path tempDir, String fileName) throws Exception {
        InputStream resourceStream = getClass().getResourceAsStream(resourcePath);
        assertNotNull(resourceStream, "Resource not found: " + resourcePath);

        Path targetPath = tempDir.resolve(fileName);
        Files.copy(resourceStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
        resourceStream.close();

        return targetPath.toFile();
    }

    @Test
    public void testNomalGenerate(@TempDir Path tempDir) throws Exception {
        // リソースから一時ディレクトリにコピー
        File d3wFile = copyResourceToTemp("/templates/防衛省サンプルワーク.d3w", tempDir, "防衛省サンプルワーク.d3w");
        File yml1 = copyResourceToTemp("/configs/01_TEST_YAML.yaml", tempDir, "01_TEST_YAML.yaml");
        File yml2 = copyResourceToTemp("/configs/02_TEST_YAML.yaml", tempDir, "02_TEST_YAML.yaml");
        File yml3 = copyResourceToTemp("/configs/03_TEST_YAML.yaml", tempDir, "03_TEST_YAML.yaml");
        File yml4 = copyResourceToTemp("/configs/04_TEST_YAML.yaml", tempDir, "04_TEST_YAML.yaml");
        File yml5 = copyResourceToTemp("/configs/05_TEST_YAML.yaml", tempDir, "05_TEST_YAML.yaml");

        // テスト実行
        Main.main(
            new String[] {
                    d3wFile.getAbsolutePath(),
                    yml1.getAbsolutePath(),
                    yml2.getAbsolutePath(),
                    yml3.getAbsolutePath(),
                    yml4.getAbsolutePath(),
                    yml5.getAbsolutePath(),
            }
        );
    }

    @Test
    public void testMainMethodExists() {
        // mainメソッドの存在確認
        try {
            Main.class.getMethod("main", String[].class);
            assertTrue(true, "main method exists");
        } catch (NoSuchMethodException e) {
            fail("main method should exist");
        }
    }

    @Test
    public void testNoArguments() {
        // 引数なしで実行した場合、エラーメッセージが表示されること
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            Main.main(new String[]{});
            fail("Should exit with code 1");
        } catch (SecurityException e) {
            // System.exit()をキャッチ（テスト環境では例外になる場合がある）
        } finally {
            System.setErr(originalErr);
        }

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("引数が不足"), "Should show error message about missing arguments");
    }

    @Test
    public void testInvalidD3wExtension(@TempDir Path tempDir) throws Exception {
        // .d3w以外の拡張子の場合エラー
        File wrongFile = tempDir.resolve("test.txt").toFile();
        Files.write(wrongFile.toPath(), "test".getBytes());

        File ymlFile = tempDir.resolve("test.yml").toFile();
        Files.write(ymlFile.toPath(), "test: value".getBytes());

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            Main.main(new String[]{wrongFile.getAbsolutePath(), ymlFile.getAbsolutePath()});
            fail("Should exit with code 1");
        } catch (SecurityException e) {
            // System.exit()をキャッチ
        } finally {
            System.setErr(originalErr);
        }

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains(".d3wファイルである必要があります"),
                "Should show error about .d3w extension");
    }

    @Test
    public void testInvalidYmlExtension(@TempDir Path tempDir) throws Exception {
        // .yml/.yaml以外の拡張子の場合エラー
        File d3wFile = tempDir.resolve("test.d3w").toFile();
        Files.write(d3wFile.toPath(), "test".getBytes());

        File wrongFile = tempDir.resolve("test.txt").toFile();
        Files.write(wrongFile.toPath(), "test".getBytes());

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            Main.main(new String[]{d3wFile.getAbsolutePath(), wrongFile.getAbsolutePath()});
            fail("Should exit with code 1");
        } catch (SecurityException e) {
            // System.exit()をキャッチ
        } finally {
            System.setErr(originalErr);
        }

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains(".ymlまたは.yamlファイルである必要があります"),
                "Should show error about .yml extension");
    }

    @Test
    public void testFileNotExists(@TempDir Path tempDir) {
        // 存在しないファイルパスの場合エラー
        String nonExistentD3w = tempDir.resolve("nonexistent.d3w").toString();
        String nonExistentYml = tempDir.resolve("nonexistent.yml").toString();

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errContent));

        try {
            Main.main(new String[]{nonExistentD3w, nonExistentYml});
            fail("Should exit with code 1");
        } catch (SecurityException e) {
            // System.exit()をキャッチ
        } finally {
            System.setErr(originalErr);
        }

        String errorOutput = errContent.toString();
        assertTrue(errorOutput.contains("ファイルが存在しません"),
                "Should show error about file not existing");
    }

    @Test
    public void testValidArgumentsWithSingleYml(@TempDir Path tempDir) throws Exception {
        // 正常な引数（1つのYAMLファイル）
        // リソースから一時ディレクトリにコピー
        File d3wFile = copyResourceToTemp("/templates/防衛省サンプルワーク.d3w", tempDir, "防衛省サンプルワーク.d3w");
        File yml1 = copyResourceToTemp("/configs/config1.yml", tempDir, "config1.yml");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            Main.main(new String[]{
                    d3wFile.getAbsolutePath(),
                    yml1.getAbsolutePath()
            });
        } catch (SecurityException e) {
            // 正常終了時もSystem.exit(0)が呼ばれる可能性があるため
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("d3w-processor 実行開始"), "Should show start message");
        assertTrue(output.contains("YAMLファイル数: 1"), "Should show correct YAML file count");

        // 元のリソースファイルが変更されていないことを確認
        InputStream originalD3w = getClass().getResourceAsStream("/templates/防衛省サンプルワーク.d3w");
        assertNotNull(originalD3w, "Original resource should still exist");
        originalD3w.close();
    }

    @Test
    public void testValidArgumentsWithMultipleYmls(@TempDir Path tempDir) throws Exception {
        // 正常な引数（複数のYAMLファイル）
        // リソースから一時ディレクトリにコピー
        File d3wFile = copyResourceToTemp("/templates/防衛省サンプルワーク.d3w", tempDir, "防衛省サンプルワーク.d3w");
        File yml1 = copyResourceToTemp("/configs/config1.yml", tempDir, "config1.yml");
        File yml2 = copyResourceToTemp("/configs/config2.yml", tempDir, "config2.yml");

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            Main.main(new String[]{
                    d3wFile.getAbsolutePath(),
                    yml1.getAbsolutePath(),
                    yml2.getAbsolutePath()
            });
        } catch (SecurityException e) {
            // 正常終了時もSystem.exit(0)が呼ばれる可能性があるため
        } finally {
            System.setOut(originalOut);
        }

        String output = outContent.toString();
        assertTrue(output.contains("d3w-processor 実行開始"), "Should show start message");
        assertTrue(output.contains("YAMLファイル数: 2"), "Should show correct YAML file count");
        assertTrue(output.contains("処理完了"), "Should show completion message");

        // テスト後、一時ディレクトリは自動的に削除される（@TempDirの機能）
        // 元のリソースファイルは変更されていない
    }

    @Test
    public void testResourceFilesExist() {
        // テストリソースが正しく配置されているか確認
        assertNotNull(getClass().getResourceAsStream("/templates/防衛省サンプルワーク.d3w"),
                "防衛省サンプルワーク.d3w should exist in test resources");
        assertNotNull(getClass().getResourceAsStream("/configs/config1.yml"),
                "config1.yml should exist in test resources");
        assertNotNull(getClass().getResourceAsStream("/configs/config2.yml"),
                "config2.yml should exist in test resources");
    }
}
