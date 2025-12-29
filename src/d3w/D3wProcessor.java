package d3w;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import d3w.model.YamlInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * D3Wファイル処理のメインクラス
 * <p>
 * 処理フロー:
 * 1. YAMLファイル群を解析してYamlInfoリストを生成
 * 2. 雛型.d3wファイルを解凍
 * 3. バリデーション（w1が存在すること、w2以降が存在しないこと）
 * 4. 雛型のw1ファイルを別名で保存（テンプレート用）
 * 5. 雛型のw1ファイルを削除
 * 6. 各YamlInfoに対してテンプレートからw1, w2, w3...ファイルを生成
 * 7. テンプレートファイルを削除
 * 8. _ファイル（メイン）を編集
 * 9. 編集後のすべてのファイルを1つの.d3wファイルに再構築
 */
public class D3wProcessor {

    private static final String TEMPLATE_FILE_NAME = "_template_work";

    /**
     * D3Wファイルを処理する（複数YAMLから1つの.d3wを生成）
     *
     * @param templateD3wPath 雛型となる.d3wファイルのパス
     * @param yamlPaths       設定YAMLファイルのパスリスト
     * @param outputD3wPath   出力する.d3wファイルのパス
     * @throws Exception 処理に失敗した場合
     */
    public static void process(String templateD3wPath, List<String> yamlPaths, String outputD3wPath)
            throws Exception {

        System.out.println("\n=== 処理開始 ===");

        // 1. YAMLファイルを解析してYamlInfoリストを生成
        List<YamlInfo> yamlInfos = loadYamlInfos(yamlPaths);
        System.out.println("✓ 読み込み完了: " + yamlInfos.size() + "個のワーク設定");
        // ワーク名に重複がないかチェックする。
        hasDuplicateWorkName(yamlInfos);

        // 2. 雛型.d3wファイルを一時ディレクトリに解凍
        // try-with-resourcesを使用して自動的にクリーンアップ
        try (TempDirectory tempDirectory = TempDirectory.create("d3w_extract_")) {
            Path extractedDir = D3wExtractor.extract(templateD3wPath, tempDirectory.getPath().toString());
            System.out.println("✓ 解凍完了: " + extractedDir);
            // 3. 既存のJSONファイルを検索
            List<Path> existingJsonFiles = D3wExtractor.findJsonFiles(extractedDir);
            System.out.println("✓ 既存ファイル: " + existingJsonFiles.size() + "個");

            // 4. _ファイル（メイン）を取得
            Path mainJsonFile = findMainJsonFile(existingJsonFiles);
            if (mainJsonFile == null) {
                throw new IllegalStateException("エラー: _ファイルが見つかりません");
            }

            // 5. バリデーション（w1が存在、w2以降が存在しない）
            System.out.println("\n--- 雛型ファイル検証 ---");
            Path originalW1File = validateTemplateFiles(existingJsonFiles);
            System.out.println("✓ 検証完了");

            // 6. 雛型のw1ファイルを別名でコピー（テンプレートとして保存）
            System.out.println("\n--- 雛型ワークファイル保存 ---");
            Path templateWorkFile = preserveTemplateWorkFile(originalW1File, extractedDir);

            // 7. 雛型のw1ファイルを削除
            System.out.println("\n--- 雛型ワークファイル削除 ---");
            Files.delete(originalW1File);
            System.out.println("✓ 削除: " + originalW1File.getFileName());

            // 8. 各YamlInfoに対してw1, w2, w3...ファイルを生成
            System.out.println("\n--- ワークファイル生成 ---");
            List<WorkReference> generatedWorks = new ArrayList<>();
            for (int i = 0; i < yamlInfos.size(); i++) {
                YamlInfo yamlInfo = yamlInfos.get(i);
                int workNumber = i + 1;

                System.out.println("[" + workNumber + "/" + yamlInfos.size() + "] " + yamlInfo.getWorkName());

                // w1, w2, w3...ファイルを生成（ファイルシステム上のテンプレートを使用）
                WorkReference workRef = createWorkFile(extractedDir, workNumber, yamlInfo, templateWorkFile);
                generatedWorks.add(workRef);

                System.out.println("  ✓ 生成: w" + workNumber);
            }

            // 9. テンプレートファイルを削除
            Files.delete(templateWorkFile);
            System.out.println("\n✓ テンプレートファイル削除: " + templateWorkFile.getFileName());

            // 10. _ファイル（メイン）を編集
            System.out.println("\n--- メインファイル編集 ---");
            editMainFile(mainJsonFile, generatedWorks);
            System.out.println("✓ 編集完了: _");

            // 11. 出力ディレクトリを作成
            Path outputPath = Paths.get(outputD3wPath);
            Path outputDir = outputPath.getParent();
            if (outputDir != null && !Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 12. すべてのファイルを1つの.d3wファイルに再構築
            System.out.println("\n--- D3Wファイル構築 ---");
            D3wBuilder.build(extractedDir, outputD3wPath);
            System.out.println("✓ 構築完了: " + outputPath.getFileName());


            System.out.println("✓ クリーンアップ完了");
            System.out.println("\n=== 処理完了 ===");
        } // try-with-resources により自動的に一時ディレクトリが削除される
    }

    /**
     * _ファイル（メインファイル）を探す
     */
    private static Path findMainJsonFile(List<Path> jsonFiles) {
        for (Path file : jsonFiles) {
            if (file.getFileName().toString().equals("_")) {
                return file;
            }
        }
        return null;
    }

    /**
     * YamlInfoに重複したワーク名がないかチェックする。
     */
    private static void hasDuplicateWorkName(List<YamlInfo> yamlInfos) {
        Set<String> workNameSet = new HashSet<>();

        for (YamlInfo yamlInfo : yamlInfos) {
            String workName = yamlInfo.getWorkName();
            if (!workNameSet.add(workName)) {
                System.err.println("エラー: YAMLファイルに重複したワーク名が存在します。：" + workName);
                System.exit(1);
            }
        }
    }

    /**
     * 雛型ファイルのバリデーション
     * - w1ファイルが存在すること
     * - w2以降のファイルが存在しないこと
     *
     * @param existingJsonFiles 既存のJSONファイルリスト
     * @return w1ファイルのPath
     * @throws IllegalStateException バリデーションエラーの場合
     */
    private static Path validateTemplateFiles(List<Path> existingJsonFiles) {
        Path w1File = null;
        List<String> invalidWorkFiles = new ArrayList<>();

        for (Path file : existingJsonFiles) {
            String fileName = file.getFileName().toString();

            // w1の存在確認
            if (fileName.equals("w1")) {
                w1File = file;
                System.out.println("  ✓ w1ファイル: 存在");
            }

            // w2以降の存在確認
            if (fileName.matches("w[2-9]\\d*")) {
                invalidWorkFiles.add(fileName);
            }
        }

        // エラーチェック
        if (w1File == null) {
            throw new IllegalStateException(
                    "エラー: 雛型.d3wファイルにw1ファイルが存在しません。\n" +
                            "雛型ファイルには必ずw1ファイルが必要です。"
            );
        }

        if (!invalidWorkFiles.isEmpty()) {
            throw new IllegalStateException(
                    "エラー: 雛型.d3wファイルにw2以降のファイルが存在します: " + String.join(", ", invalidWorkFiles) + "\n" +
                            "雛型ファイルにはw1ファイルのみが存在する必要があります。\n" +
                            "w2以降のファイルは自動生成されるため、事前に存在してはいけません。"
            );
        }

        return w1File;
    }

    /**
     * 雛型のw1ファイルを別名でコピーして保存
     * テンプレートとして使用するため、削除されないように別名で保存する
     *
     * @param originalW1File 雛型のw1ファイル
     * @param extractedDir   解凍先ディレクトリ
     * @return 保存したテンプレートファイルのPath
     */
    private static Path preserveTemplateWorkFile(Path originalW1File, Path extractedDir)
            throws IOException {
        // 別名でコピー（_template_work という名前で保存）
        Path templateFile = extractedDir.resolve(TEMPLATE_FILE_NAME);
        Files.copy(originalW1File, templateFile, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("✓ テンプレート保存: " + originalW1File.getFileName() + " → " + templateFile.getFileName());
        return templateFile;
    }

    /**
     * w1, w2, w3...ファイルを生成
     *
     * @param extractedDir     解凍先ディレクトリ
     * @param workNumber       ワーク番号（1, 2, 3...）
     * @param yamlInfo         ワーク設定情報
     * @param templateWorkFile テンプレートファイルのPath（ファイルシステム上）
     * @return 生成されたワークの参照情報
     */
    private static WorkReference createWorkFile(Path extractedDir, int workNumber,
                                                YamlInfo yamlInfo, Path templateWorkFile) throws Exception {

        // テンプレートファイルを読み込み（JsonObjectとして）
        JsonObject content = JsonEditor.readJson(templateWorkFile);

        // YamlInfoの内容を適用
        JsonEditor.applyYamlInfo(content, yamlInfo);

        // キーはインポートした際に採番されるので空にしておく。
        if (content.has("key") && content.get("key").isJsonPrimitive()) {
            content.addProperty("key", "");
        }

        // wN ファイル名を生成
        String fileName = "w" + workNumber;
        Path workFile = extractedDir.resolve(fileName);

        // ファイルに書き込み
        JsonEditor.writeJson(content, workFile);

        // ワーク参照情報を作成して返す
        WorkReference workRef = new WorkReference();
        workRef.contentClass = content.has("content_class")
                ? content.get("content_class").getAsString()
                : null;
        workRef.key = "";
        workRef.name = content.has("name")
                ? content.get("name").getAsString()
                : null;
        workRef.path = fileName;

        return workRef;
    }

    /**
     * _ファイル（メインファイル）を編集
     * envelopes、servicesはそのまま保持し、worksのみを更新する
     */
    private static void editMainFile(Path mainJsonFile, List<WorkReference> generatedWorks) throws Exception {
        // 既存の_ファイルを読み込む
        JsonObject mainContent = JsonEditor.readJson(mainJsonFile);

        // worksプロパティを更新
        JsonArray worksArray = new JsonArray();
        for (WorkReference workRef : generatedWorks) {
            JsonObject workObj = new JsonObject();
            if (workRef.contentClass != null) {
                workObj.addProperty("content_class", workRef.contentClass);
            }
            if (workRef.key != null) {
                workObj.addProperty("key", workRef.key);
            }
            if (workRef.name != null) {
                workObj.addProperty("name", workRef.name);
            }
            if (workRef.path != null) {
                workObj.addProperty("path", workRef.path);
            }
            worksArray.add(workObj);
        }
        mainContent.add("works", worksArray);

        JsonEditor.writeJson(mainContent, mainJsonFile);
    }

    /**
     * YAMLファイルのリストからYamlInfoのリストを生成
     */
    private static List<YamlInfo> loadYamlInfos(List<String> yamlPaths) throws Exception {
        List<YamlInfo> yamlInfos = new ArrayList<>();
        for (String yamlPath : yamlPaths) {
            YamlInfo yamlInfo = YamlInfoLoader.loadFromYaml(yamlPath);
            yamlInfos.add(yamlInfo);
        }
        return yamlInfos;
    }

    /**
     * ワーク参照情報を保持する内部クラス
     * _ファイルのworksリストに含める情報
     */
    private static class WorkReference {
        String contentClass;
        String key;
        String name;
        String path;
    }
}
