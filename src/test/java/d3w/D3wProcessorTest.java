package d3w;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * D3wProcessor の統合テスト（JsonObjectベースの実装に対応）
 */
class D3wProcessorTest {

    @Test
    void testProcess_SingleYaml(@TempDir Path tempDir) throws Exception {
        // 1. テスト用の雛型.d3wファイルを作成
        Path templateD3w = createRealisticTemplateD3w(tempDir);

        // 2. テスト用のYAMLファイルを作成
        Path yaml1 = createTestYaml(tempDir, "work1.yaml",
                "テストワーク1", "メモ1", "件名1", "template1",
                Arrays.asList("項目01", "項目02"),
                Arrays.asList("文書名", "テンプレート名", "項目01", "項目02"));

        // 3. 出力先を設定
        Path outputD3w = tempDir.resolve("output.d3w");

        // 4. 処理を実行
        D3wProcessor.process(
                templateD3w.toString(),
                Collections.singletonList(yaml1.toString()),
                outputD3w.toString()
        );

        // 5. 検証
        assertTrue(Files.exists(outputD3w), "出力ファイルが生成されていること");
        assertTrue(Files.size(outputD3w) > 0, "出力ファイルが空でないこと");

        // 6. 生成された.d3wファイルを解凍して検証
        Path extractedDir = D3wExtractor.extractToTemp(outputD3w.toString());
        List<Path> jsonFiles = D3wExtractor.findJsonFiles(extractedDir);

        // _ファイルとw1ファイルが存在することを確認
        boolean hasMainFile = false;
        boolean hasW1File = false;

        for (Path file : jsonFiles) {
            String fileName = file.getFileName().toString();
            if (fileName.equals("_")) {
                hasMainFile = true;
            } else if (fileName.equals("w1")) {
                hasW1File = true;
            }
        }

        assertTrue(hasMainFile, "_ファイルが存在すること");
        assertTrue(hasW1File, "w1ファイルが存在すること");

        // w1の内容を検証（JsonObjectベース）
        JsonObject w1Content = JsonEditor.readJson(extractedDir.resolve("w1"));
        assertEquals("テストワーク1", w1Content.get("name").getAsString());
        assertEquals("メモ1", w1Content.get("note").getAsString());
        assertTrue(w1Content.has("content"));

        JsonObject content = w1Content.getAsJsonObject("content");
        assertEquals("件名1", content.get("subject").getAsString());
    }

    @Test
    void testProcess_MultipleYamls(@TempDir Path tempDir) throws Exception {
        // 1. テスト用の雛型.d3wファイルを作成
        Path templateD3w = createRealisticTemplateD3w(tempDir);

        // 2. テスト用のYAMLファイルを複数作成
        Path yaml1 = createTestYaml(tempDir, "work1.yaml",
                "テストワーク1", "メモ1", "件名1", "template1",
                Arrays.asList("項目01", "項目02"),
                Arrays.asList("Id", "Name", "Amount__c"));

        Path yaml2 = createTestYaml(tempDir, "work2.yaml",
                "テストワーク2", "メモ2", "件名2", "template2",
                Arrays.asList("項目A", "項目B", "項目C"),
                Arrays.asList("Id", "Status__c"));

        Path yaml3 = createTestYaml(tempDir, "work3.yaml",
                "テストワーク3", "メモ3", "件名3", "template3",
                Collections.singletonList("フィールド1"),
                Arrays.asList("CreatedDate", "UpdatedDate"));

        // 3. 出力先を設定
        Path outputD3w = tempDir.resolve("output_multi.d3w");

        // 4. 処理を実行
        D3wProcessor.process(
                templateD3w.toString(),
                Arrays.asList(yaml1.toString(), yaml2.toString(), yaml3.toString()),
                outputD3w.toString()
        );

        // 5. 検証
        assertTrue(Files.exists(outputD3w), "出力ファイルが生成されていること");

        // 6. 生成された.d3wファイルを解凍して検証
        Path extractedDir = D3wExtractor.extractToTemp(outputD3w.toString());
        List<Path> jsonFiles = D3wExtractor.findJsonFiles(extractedDir);

        // _、w1、w2、w3ファイルが存在することを確認
        boolean hasMainFile = false;
        boolean hasW1File = false;
        boolean hasW2File = false;
        boolean hasW3File = false;

        for (Path file : jsonFiles) {
            String fileName = file.getFileName().toString();
            switch (fileName) {
                case "_":
                    hasMainFile = true;
                    break;
                case "w1":
                    hasW1File = true;
                    break;
                case "w2":
                    hasW2File = true;
                    break;
                case "w3":
                    hasW3File = true;
                    break;
            }
        }

        assertTrue(hasMainFile, "_ファイルが存在すること");
        assertTrue(hasW1File, "w1ファイルが存在すること");
        assertTrue(hasW2File, "w2ファイルが存在すること");
        assertTrue(hasW3File, "w3ファイルが存在すること");

        // 各ファイルの内容を検証（JsonObjectベース）
        JsonObject w1Content = JsonEditor.readJson(extractedDir.resolve("w1"));
        assertEquals("テストワーク1", w1Content.get("name").getAsString());
        assertEquals("メモ1", w1Content.get("note").getAsString());

        JsonObject w2Content = JsonEditor.readJson(extractedDir.resolve("w2"));
        assertEquals("テストワーク2", w2Content.get("name").getAsString());
        assertEquals("メモ2", w2Content.get("note").getAsString());

        JsonObject w3Content = JsonEditor.readJson(extractedDir.resolve("w3"));
        assertEquals("テストワーク3", w3Content.get("name").getAsString());
        assertEquals("メモ3", w3Content.get("note").getAsString());

        // _ファイルの内容を検証（3つのワークが登録されていること）
        JsonObject mainContent = JsonEditor.readJson(extractedDir.resolve("_"));
        assertTrue(mainContent.has("works"));

        JsonArray works = mainContent.getAsJsonArray("works");
        assertEquals(3, works.size());

        JsonObject work1 = works.get(0).getAsJsonObject();
        assertEquals("テストワーク1", work1.get("name").getAsString());
        assertEquals("w1", work1.get("path").getAsString());

        JsonObject work2 = works.get(1).getAsJsonObject();
        assertEquals("テストワーク2", work2.get("name").getAsString());
        assertEquals("w2", work2.get("path").getAsString());

        JsonObject work3 = works.get(2).getAsJsonObject();
        assertEquals("テストワーク3", work3.get("name").getAsString());
        assertEquals("w3", work3.get("path").getAsString());
    }

    @Test
    void testProcess_NoW1File_ShouldFail(@TempDir Path tempDir) throws Exception {
        // 1. w1ファイルが存在しない雛型.d3wファイルを作成
        Path templateD3w = createInvalidTemplateD3w_NoW1(tempDir);

        // 2. テスト用のYAMLファイルを作成
        Path yaml1 = createTestYaml(tempDir, "work1.yaml",
                "テストワーク1", "メモ1", "件名1", "template1",
                Collections.singletonList("項目01"),
                Arrays.asList("Id", "Name"));

        // 3. 出力先を設定
        Path outputD3w = tempDir.resolve("output.d3w");

        // 4. エラーが発生することを確認
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> D3wProcessor.process(
                templateD3w.toString(),
                Collections.singletonList(yaml1.toString()),
                outputD3w.toString()
        ));

        // 5. エラーメッセージを検証
        assertTrue(exception.getMessage().contains("w1ファイルが存在しません"));
    }

    @Test
    void testProcess_W2Exists_ShouldFail(@TempDir Path tempDir) throws Exception {
        // 1. w2ファイルが存在する雛型.d3wファイルを作成
        Path templateD3w = createInvalidTemplateD3w_HasW2(tempDir);

        // 2. テスト用のYAMLファイルを作成
        Path yaml1 = createTestYaml(tempDir, "work1.yaml",
                "テストワーク1", "メモ1", "件名1", "template1",
                Collections.singletonList("項目01"),
                Arrays.asList("Id", "Name"));

        // 3. 出力先を設定
        Path outputD3w = tempDir.resolve("output.d3w");

        // 4. エラーが発生することを確認
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> D3wProcessor.process(
                templateD3w.toString(),
                Collections.singletonList(yaml1.toString()),
                outputD3w.toString()
        ));

        // 5. エラーメッセージを検証
        assertTrue(exception.getMessage().contains("w2以降のファイルが存在します"));
    }

    /**
     * 実際の構造に近いテスト用の雛型.d3wファイルを作成
     */
    private Path createRealisticTemplateD3w(Path tempDir) throws Exception {
        Path d3wFile = tempDir.resolve("template.d3w");

        // _ファイルのJSON（実際の構造）
        String mainJson = "{"
                + "\"envelopes\":[{\"content_class\":\"net.opro.product.d3w.envelope.StandardEnvelope\","
                + "\"key\":\"GZvtFd9HmH_GzF\",\"name\":\"サンプルエンベロープ\",\"path\":\"e1\"}],"
                + "\"services\":[{\"content_class\":\"net.opro.product.d3w.service.oproarts.TenantArtsService\","
                + "\"key\":\"G78YQaRQMD_GcH\",\"name\":\"OPROARTS\",\"path\":\"s1\"}],"
                + "\"works\":[{\"content_class\":\"net.opro.product.d3w.work.csv.CsvWork\","
                + "\"key\":\"GZvtst4WLH_GqR\",\"name\":\"雛型ワーク\",\"path\":\"w1\"}]"
                + "}";

        // w1ファイルのJSON（実際の構造）
        String w1Json = "{"
                + "\"content\":{\"subject\":\"\",\"datasource\":{\"fields\":[\"文書名\",\"テンプレート名\"],"
                + "\"ignore_first_row\":false,\"charset\":\"Windows-31J\"},"
                + "\"envelope\":{\"field\":{\"name\":\"\",\"sort\":false},"
                + "\"envelopes\":[{\"key\":\"GZvtFd9HmH_GzF\",\"other\":false,"
                + "\"activate_field\":{\"name\":\"\"},\"values\":[],\"params\":[]}]},"
                + "\"document\":{\"name\":\"[文書名]\",\"embed_font\":false,"
                + "\"template\":{\"name\":\"[テンプレート名]\",\"delimiter_field\":{\"name\":\"\"},"
                + "\"params\":[{\"name\":\"TextDataset1\",\"type\":\"csv\","
                + "\"ignore_empty_row\":false,\"insert_first_empty_row\":false,"
                + "\"values\":[\"[項目01]\",\"[項目02]\"]}]}}},"
                + "\"content_class\":\"net.opro.product.d3w.work.csv.CsvWork\","
                + "\"key\":\"GZvtst4WLH_GqR\",\"name\":\"雛型ワーク\",\"note\":\"雛型\""
                + "}";

        // ZIPファイルとして作成
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(d3wFile.toFile().toPath()))) {
            // _ファイルを追加
            ZipEntry mainEntry = new ZipEntry("_");
            zos.putNextEntry(mainEntry);
            zos.write(mainJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // w1ファイルを追加
            ZipEntry w1Entry = new ZipEntry("w1");
            zos.putNextEntry(w1Entry);
            zos.write(w1Json.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return d3wFile;
    }

    /**
     * w1ファイルが存在しない不正な雛型.d3wファイルを作成
     */
    private Path createInvalidTemplateD3w_NoW1(Path tempDir) throws Exception {
        Path d3wFile = tempDir.resolve("invalid_no_w1.d3w");

        String mainJson = "{\"envelopes\":[],\"services\":[],\"works\":[]}";

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(d3wFile.toFile().toPath()))) {
            ZipEntry mainEntry = new ZipEntry("_");
            zos.putNextEntry(mainEntry);
            zos.write(mainJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return d3wFile;
    }

    /**
     * w2ファイルが存在する不正な雛型.d3wファイルを作成
     */
    private Path createInvalidTemplateD3w_HasW2(Path tempDir) throws Exception {
        Path d3wFile = tempDir.resolve("invalid_has_w2.d3w");

        String mainJson = "{\"envelopes\":[],\"services\":[],\"works\":[]}";
        String w1Json = "{\"name\":\"ワーク1\"}";
        String w2Json = "{\"name\":\"ワーク2\"}";

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(d3wFile.toFile().toPath()))) {
            ZipEntry mainEntry = new ZipEntry("_");
            zos.putNextEntry(mainEntry);
            zos.write(mainJson.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            ZipEntry w1Entry = new ZipEntry("w1");
            zos.putNextEntry(w1Entry);
            zos.write(w1Json.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            ZipEntry w2Entry = new ZipEntry("w2");
            zos.putNextEntry(w2Entry);
            zos.write(w2Json.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        return d3wFile;
    }

    /**
     * テスト用のYAMLファイルを作成
     */
    private Path createTestYaml(Path tempDir, String fileName,
                                String workName, String memoText,
                                String subject, String templateName,
                                List<String> textDatasetFields,
                                List<String> datasourceFields) throws Exception {
        Path yamlFile = tempDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(yamlFile.toFile())) {
            writer.write("workName: \"" + workName + "\"\n");
            writer.write("memoText: \"" + memoText + "\"\n");
            writer.write("subject: \"" + subject + "\"\n");
            writer.write("templateName: \"" + templateName + "\"\n");
            writer.write("textDatasetFields:\n");
            for (String field : textDatasetFields) {
                writer.write("  - " + field + "\n");
            }
            writer.write("datasourceFields:\n");
            for (String field : datasourceFields) {
                writer.write("  - " + field + "\n");
            }
        }

        return yamlFile;
    }
}
