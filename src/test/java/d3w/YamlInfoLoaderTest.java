package d3w;

import d3w.model.YamlInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class YamlInfoLoaderTest {

    @Test
    void testLoadFromYaml() throws Exception {
        // テストリソースのパスを取得
        String resourcePath = getClass().getClassLoader()
                .getResource("configs/01_TEST_YAML.yaml")
                .getPath();

        // YAMLファイルから読み込み
        YamlInfo yamlInfo = YamlInfoLoader.loadFromYaml(resourcePath);

        // 検証
        assertNotNull(yamlInfo);
        assertEquals("ワーク生成_テスト岡谷_1", yamlInfo.getWorkName());
        assertEquals("ワーク自動生成テスト_1", yamlInfo.getMemoText());
        assertEquals("ワークテスト件名_1", yamlInfo.getSubject());
        assertEquals("[文書名]", yamlInfo.getDocumentName());
        assertEquals("[テンプレート名]", yamlInfo.getTemplateName());

        // textDatasetFieldsの検証
        assertNotNull(yamlInfo.getTextDatasetFields());
        assertEquals(4, yamlInfo.getTextDatasetFields().size());
        assertTrue(yamlInfo.getTextDatasetFields().contains("あいうえお"));
        assertTrue(yamlInfo.getTextDatasetFields().contains("かきくけこ"));
        assertTrue(yamlInfo.getTextDatasetFields().contains("さしすせそ"));
        assertTrue(yamlInfo.getTextDatasetFields().contains("たちつてと"));

        // datasourceFieldsの検証
        assertNotNull(yamlInfo.getDatasourceFields());
        assertEquals(6, yamlInfo.getDatasourceFields().size());
        assertTrue(yamlInfo.getDatasourceFields().contains("テンプレート名"));
        assertTrue(yamlInfo.getDatasourceFields().contains("文書名"));
        assertTrue(yamlInfo.getDatasourceFields().contains("あいうえお"));
        assertTrue(yamlInfo.getDatasourceFields().contains("かきくけこ"));
        assertTrue(yamlInfo.getDatasourceFields().contains("さしすせそ"));
        assertTrue(yamlInfo.getDatasourceFields().contains("たちつてと"));
    }

    @Test
    void testLoadFromYaml_WithNullValues(@TempDir Path tempDir) throws Exception {
        // memoTextがnullのYAMLファイルを作成
        File testFile = tempDir.resolve("test_null.yaml").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("workName: \"テストワーク\"\n");
            writer.write("memoText: null\n");
            writer.write("subject: \"テスト件名\"\n");
            writer.write("templateName: \"test_template\"\n");
            writer.write("textDatasetFields:\n");
            writer.write("  - field1\n");
            writer.write("datasourceFields:\n");
            writer.write("  - Id\n");
        }

        YamlInfo yamlInfo = YamlInfoLoader.loadFromYaml(testFile.getAbsolutePath());

        assertNotNull(yamlInfo);
        assertEquals("テストワーク", yamlInfo.getWorkName());
        assertNull(yamlInfo.getMemoText());
        assertEquals("テスト件名", yamlInfo.getSubject());
    }

    @Test
    void testLoadFromYaml_EmptyLists(@TempDir Path tempDir) throws Exception {
        // 空のリストを持つYAMLファイルを作成
        File testFile = tempDir.resolve("test_empty.yaml").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("workName: \"空リストテスト\"\n");
            writer.write("memoText: \"空のリスト\"\n");
            writer.write("subject: \"空リスト件名\"\n");
            writer.write("templateName: \"empty_template\"\n");
            writer.write("textDatasetFields: []\n");
            writer.write("datasourceFields: []\n");
        }

        YamlInfo yamlInfo = YamlInfoLoader.loadFromYaml(testFile.getAbsolutePath());

        assertNotNull(yamlInfo);
        assertNotNull(yamlInfo.getTextDatasetFields());
        assertNotNull(yamlInfo.getDatasourceFields());
        assertEquals(0, yamlInfo.getTextDatasetFields().size());
        assertEquals(0, yamlInfo.getDatasourceFields().size());
    }

    @Test
    void testLoadFromYaml_FileNotFound() {
        Exception exception = assertThrows(Exception.class, () -> YamlInfoLoader.loadFromYaml("non_existent_file.yaml"));

        assertNotNull(exception);
    }

    @Test
    void testLoadAllFromDirectory_EmptyDirectory(@TempDir Path tempDir) throws Exception {
        List<YamlInfo> allWorks = YamlInfoLoader.loadAllFromDirectory(tempDir.toString());

        assertNotNull(allWorks);
        assertEquals(0, allWorks.size());
    }

    @Test
    void testLoadFromYaml_ComplexStructure(@TempDir Path tempDir) throws Exception {
        // 複雑な構造のYAMLファイルを作成
        File testFile = tempDir.resolve("test_complex.yaml").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("workName: \"複雑なワーク\"\n");
            writer.write("memoText: |\n");
            writer.write("  これは複数行の\n");
            writer.write("  メモテキストです。\n");
            writer.write("  改行も含まれます。\n");
            writer.write("subject: \"複雑な件名\"\n");
            writer.write("templateName: \"complex_template\"\n");
            writer.write("textDatasetFields:\n");
            writer.write("  - field_with_underscore\n");
            writer.write("  - fieldWithCamelCase\n");
            writer.write("  - field-with-hyphen\n");
            writer.write("datasourceFields:\n");
            writer.write("  - ParentObject__r.ChildObject__r.Field__c\n");
            writer.write("  - CustomObject__c.Name\n");
        }

        YamlInfo yamlInfo = YamlInfoLoader.loadFromYaml(testFile.getAbsolutePath());

        assertNotNull(yamlInfo);
        assertEquals("複雑なワーク", yamlInfo.getWorkName());
        assertTrue(yamlInfo.getMemoText().contains("複数行"));
        assertTrue(yamlInfo.getMemoText().contains("改行も含まれます"));

        assertEquals(3, yamlInfo.getTextDatasetFields().size());
        assertTrue(yamlInfo.getTextDatasetFields().contains("field_with_underscore"));
        assertTrue(yamlInfo.getTextDatasetFields().contains("fieldWithCamelCase"));

        assertEquals(2, yamlInfo.getDatasourceFields().size());
        assertTrue(yamlInfo.getDatasourceFields().contains("ParentObject__r.ChildObject__r.Field__c"));
    }
}
