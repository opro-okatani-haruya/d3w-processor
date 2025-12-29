package d3w;

import com.google.gson.*;
import d3w.model.YamlInfo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * D3WファイルのJSON編集を担当するクラス
 * JsonObjectを使用して、クラス定義なしで柔軟にJSON操作を行う
 */
public class JsonEditor {

    public static final Gson GSON = new GsonBuilder().create();

    /**
     * JSONファイル（1行）を読み込んでJsonObjectに変換する
     *
     * @param jsonFilePath JSONファイルのパス
     * @return JsonObject
     * @throws IOException ファイル読み込みに失敗した場合
     */
    public static JsonObject readJson(Path jsonFilePath) throws IOException {
        String jsonContent = new String(Files.readAllBytes(jsonFilePath), StandardCharsets.UTF_8);
        return JsonParser.parseString(jsonContent).getAsJsonObject();
    }

    /**
     * JsonObjectをJSON文字列（1行）に変換する
     *
     * @param jsonObject JsonObject
     * @return JSON文字列（1行、改行なし）
     */
    public static String toJson(JsonObject jsonObject) {
        return GSON.toJson(jsonObject);
    }

    /**
     * JsonObjectをJSONファイルに書き込む（1行形式）
     *
     * @param jsonObject   JsonObject
     * @param jsonFilePath 書き込み先のJSONファイルパス
     * @throws IOException ファイル書き込みに失敗した場合
     */
    public static void writeJson(JsonObject jsonObject, Path jsonFilePath) throws IOException {
        String jsonString = toJson(jsonObject);
        Files.write(jsonFilePath, jsonString.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * YamlInfoの情報をJsonObjectに適用する
     *
     * @param jsonObject 編集対象のJsonObject
     * @param yamlInfo   適用するYamlInfo
     */
    public static void applyYamlInfo(JsonObject jsonObject, YamlInfo yamlInfo) {
        // ワーク名を設定
        if (yamlInfo.getWorkName() != null) {
            jsonObject.addProperty("name", yamlInfo.getWorkName());
        }

        // メモを設定
        if (yamlInfo.getMemoText() != null) {
            jsonObject.addProperty("note", yamlInfo.getMemoText());
        }

        // content階層を取得または作成
        JsonObject content = getOrCreateObject(jsonObject, "content");

        // 件名を設定
        if (yamlInfo.getSubject() != null) {
            content.addProperty("subject", yamlInfo.getSubject());
        }

        // datasource階層を取得または作成
        JsonObject datasource = getOrCreateObject(content, "datasource");

        // データソースフィールドを設定
        if (yamlInfo.getDatasourceFields() != null && !yamlInfo.getDatasourceFields().isEmpty()) {
            JsonArray fieldsArray = new JsonArray();
            for (String field : yamlInfo.getDatasourceFields()) {
                fieldsArray.add(field);
            }
            datasource.add("fields", fieldsArray);
        }

        // document階層を取得または作成
        JsonObject document = getOrCreateObject(content, "document");

        // template階層を取得または作成
        JsonObject template = getOrCreateObject(document, "template");

        // テンプレート名を設定
        if (yamlInfo.getTemplateName() != null) {
            template.addProperty("name", yamlInfo.getTemplateName());
        }

        // テキストデータセットフィールドをパラメータとして設定
        if (yamlInfo.getTextDatasetFields() != null && !yamlInfo.getTextDatasetFields().isEmpty()) {
            // 既存のparamsを取得（存在しない場合は新規作成）
            JsonArray params = template.has("params") && template.get("params").isJsonArray()
                    ? template.getAsJsonArray("params")
                    : new JsonArray();

            // 既存の最初のパラメータをベースとして使用
            JsonObject baseParam = null;
            if (!params.isEmpty() && params.get(0).isJsonObject()) {
                baseParam = params.get(0).getAsJsonObject();
            }

            // 新しいパラメータオブジェクトを作成
            JsonObject param = new JsonObject();

            // ベースパラメータから設定をコピー
            if (baseParam != null) {
                if (baseParam.has("name")) {
                    param.addProperty("name", baseParam.get("name").getAsString());
                }
                if (baseParam.has("type")) {
                    param.addProperty("type", baseParam.get("type").getAsString());
                }
                if (baseParam.has("ignore_empty_row")) {
                    param.addProperty("ignore_empty_row", baseParam.get("ignore_empty_row").getAsBoolean());
                }
                if (baseParam.has("insert_first_empty_row")) {
                    param.addProperty("insert_first_empty_row", baseParam.get("insert_first_empty_row").getAsBoolean());
                }
            } else {
                // デフォルト値を設定
                param.addProperty("name", "TextDataset1");
                param.addProperty("type", "csv");
                param.addProperty("ignore_empty_row", false);
                param.addProperty("insert_first_empty_row", false);
            }

            // フィールドを[項目名]形式に変換してvaluesに設定
            JsonArray values = new JsonArray();
            for (String field : yamlInfo.getTextDatasetFields()) {
                values.add("[" + field + "]");
            }
            param.add("values", values);

            // paramsを更新（既存の場合は置き換え、新規の場合は追加）
            JsonArray newParams = new JsonArray();
            newParams.add(param);
            template.add("params", newParams);
        }
    }

    /**
     * JsonObjectから指定されたキーのオブジェクトを取得する
     * 存在しない場合は新しいJsonObjectを作成して設定する
     *
     * @param parent 親JsonObject
     * @param key    キー名
     * @return 取得または作成されたJsonObject
     */
    private static JsonObject getOrCreateObject(JsonObject parent, String key) {
        if (parent.has(key) && parent.get(key).isJsonObject()) {
            return parent.getAsJsonObject(key);
        } else {
            JsonObject newObject = new JsonObject();
            parent.add(key, newObject);
            return newObject;
        }
    }

    /**
     * JsonObjectをディープコピーする
     *
     * @param source コピー元のJsonObject
     * @return コピーされた新しいJsonObject
     */
    public static JsonObject deepCopy(JsonObject source) {
        String json = GSON.toJson(source);
        return JsonParser.parseString(json).getAsJsonObject();
    }
}
