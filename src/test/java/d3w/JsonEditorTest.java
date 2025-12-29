package d3w;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import d3w.model.YamlInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class JsonEditorTest {

    @Test
    void testApplyYamlInfo() {
        // テストデータの準備
        JsonObject content = new JsonObject();
        
        YamlInfo yamlInfo = new YamlInfo(
            "テストワーク",
            "テストメモ",
            "テスト件名",
            "テスト文書名",
            "test_template",
            Arrays.asList("field1", "field2"),
            Arrays.asList("Id", "Name", "Amount__c")
        );
        
        // YamlInfoを適用
        JsonObject result = JsonEditor.applyYamlInfo(content, yamlInfo);
        
        // 検証
        assertNotNull(result);
        assertEquals("テストワーク", result.get("name").getAsString());
        assertEquals("テストメモ", result.get("note").getAsString());
        
        assertTrue(result.has("content"));
        JsonObject contentObj = result.getAsJsonObject("content");
        assertEquals("テスト件名", contentObj.get("subject").getAsString());
        
        assertTrue(contentObj.has("document"));
        JsonObject document = contentObj.getAsJsonObject("document");
        
        assertTrue(document.has("template"));
        JsonObject template = document.getAsJsonObject("template");
        assertEquals("test_template", template.get("name").getAsString());
        
        assertTrue(template.has("params"));
        JsonArray params = template.getAsJsonArray("params");
        assertEquals(1, params.size()); // 1つのパラメータオブジェクトに統合される
        
        JsonObject param = params.get(0).getAsJsonObject();
        assertTrue(param.has("values"));
        JsonArray values = param.getAsJsonArray("values");
        assertEquals(2, values.size());
        assertEquals("[field1]", values.get(0).getAsString());
        assertEquals("[field2]", values.get(1).getAsString());
        
        assertTrue(contentObj.has("datasource"));
        JsonObject datasource = contentObj.getAsJsonObject("datasource");
        assertTrue(datasource.has("fields"));
        JsonArray fields = datasource.getAsJsonArray("fields");
        assertEquals(3, fields.size());
        assertEquals("Id", fields.get(0).getAsString());
        assertEquals("Name", fields.get(1).getAsString());
        assertEquals("Amount__c", fields.get(2).getAsString());
    }

    @Test
    void testReadAndWriteJson(@TempDir Path tempDir) throws Exception {
        // JSONファイルを作成
        Path jsonFile = tempDir.resolve("test.json");
        String jsonContent = "{\"name\":\"テスト\",\"note\":\"メモ\",\"content\":{\"subject\":\"件名\"}}";
        Files.write(jsonFile, jsonContent.getBytes(StandardCharsets.UTF_8));
        
        // JSONを読み込み
        JsonObject content = JsonEditor.readJson(jsonFile);
        
        assertNotNull(content);
        assertEquals("テスト", content.get("name").getAsString());
        assertEquals("メモ", content.get("note").getAsString());
        assertTrue(content.has("content"));
        
        JsonObject contentObj = content.getAsJsonObject("content");
        assertEquals("件名", contentObj.get("subject").getAsString());
        
        // JSONに書き込み
        content.addProperty("name", "変更後");
        Path outputFile = tempDir.resolve("output.json");
        JsonEditor.writeJson(content, outputFile);
        
        // 書き込んだファイルを読み込んで検証
        JsonObject reloaded = JsonEditor.readJson(outputFile);
        assertEquals("変更後", reloaded.get("name").getAsString());
        assertEquals("メモ", reloaded.get("note").getAsString());
    }

    @Test
    void testToJson() {
        JsonObject content = new JsonObject();
        content.addProperty("name", "テスト");
        content.addProperty("note", "メモ");
        
        String json = JsonEditor.toJson(content);
        
        assertNotNull(json);
        assertTrue(json.contains("\"name\":\"テスト\""));
        assertTrue(json.contains("\"note\":\"メモ\""));
        assertFalse(json.contains("\n")); // 1行形式であることを確認
    }
    
    @Test
    void testDeepCopy() {
        // 元のJsonObjectを作成
        JsonObject original = new JsonObject();
        original.addProperty("name", "オリジナル");
        original.addProperty("note", "元のメモ");
        
        JsonObject content = new JsonObject();
        content.addProperty("subject", "件名");
        original.add("content", content);
        
        // ディープコピーを実行
        JsonObject copy = JsonEditor.deepCopy(original);
        
        // コピーであることを確認
        assertNotSame(original, copy);
        assertEquals(original.get("name").getAsString(), copy.get("name").getAsString());
        
        // コピーを変更しても元が影響を受けないことを確認
        copy.addProperty("name", "コピー");
        copy.getAsJsonObject("content").addProperty("subject", "新しい件名");
        
        assertEquals("オリジナル", original.get("name").getAsString());
        assertEquals("件名", original.getAsJsonObject("content").get("subject").getAsString());
        assertEquals("コピー", copy.get("name").getAsString());
        assertEquals("新しい件名", copy.getAsJsonObject("content").get("subject").getAsString());
    }
}
