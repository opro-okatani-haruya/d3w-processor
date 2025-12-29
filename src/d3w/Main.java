package d3w;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * D3W Processor CLI Tool
 * YAMLファイルと雛型.d3wを受け取り、加工した後で1つの.d3wファイルを生成する。
 * 使用方法:
 *   java -jar d3w-processor.jar <雛型.d3wファイル> <設定.ymlファイル1> [<設定.ymlファイル2> ...]
 * 引数:
 *   args[0]  雛型.d3wファイルのパス
 *   args[1~] ワークの設定を記載したYAMLファイルのパス（1つ以上）
 * 出力:
 *   雛型.d3wと同じディレクトリに、編集済みの.d3wファイルを1つ生成
 *   ファイル名: output_yyyyMMddHHmmss.d3w
 * 処理内容:
 *   - YAMLの数だけw1, w2, w3...ファイルを生成
 *   - _ファイル（メイン）を編集
 *   - すべてを1つの.d3wファイルにまとめる
 */
public class Main {
    
    public static void main(String[] args) {
        // 引数チェック
        if (args.length < 2) {
            printUsage();
            System.exit(1);
        }

        // 第一引数: .d3wファイル
        final String d3wFilePath = args[0];
        if (!validateD3wFile(d3wFilePath)) {
            System.exit(1);
        }

        // 第二引数以降: .ymlファイル（1つ以上）
        final List<String> ymlFilePaths = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            final String ymlFilePath = args[i];
            if (!validateYamlFile(ymlFilePath, i + 1)) {
                System.exit(1);
            }
            ymlFilePaths.add(ymlFilePath);
        }

        // 処理開始
        printHeader(d3wFilePath, ymlFilePaths);
        
        try {
            String outputPath = processFiles(d3wFilePath, ymlFilePaths);
            printSuccess(outputPath);
        } catch (Exception e) {
            printError(e);
            System.exit(1);
        }
    }

    /**
     * .d3wファイルとYAMLファイルを処理する
     * 
     * @return 生成された.d3wファイルのパス
     */
    private static String processFiles(String d3wFilePath, List<String> ymlFilePaths) throws Exception {
        // 出力先ディレクトリを決定（雛型.d3wと同じディレクトリ）
        String outputDir = Paths.get(d3wFilePath).getParent().toString();
        
        // タイムスタンプを生成
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        
        // 出力ファイル名を生成
        String outputFileName = "output_" + timestamp + ".d3w";
        String outputPath = Paths.get(outputDir, outputFileName).toString();
        
        // D3wProcessorで処理を実行
        D3wProcessor.process(d3wFilePath, ymlFilePaths, outputPath);
        
        return outputPath;
    }

    /**
     * .d3wファイルの検証
     */
    private static boolean validateD3wFile(String filePath) {
        if (!filePath.endsWith(".d3w")) {
            System.err.println("エラー: 第一引数は.d3wファイルである必要があります: " + filePath);
            return false;
        }

        final File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("エラー: .d3wファイルが存在しません: " + filePath);
            return false;
        }

        return true;
    }

    /**
     * YAMLファイルの検証
     */
    private static boolean validateYamlFile(String filePath, int argIndex) {
        if (!filePath.endsWith(".yml") && !filePath.endsWith(".yaml")) {
            System.err.println("エラー: 第" + argIndex + "引数は.ymlまたは.yamlファイルである必要があります: " + filePath);
            return false;
        }

        final File file = new File(filePath);
        if (!file.exists()) {
            System.err.println("エラー: YAMLファイルが存在しません: " + filePath);
            return false;
        }

        return true;
    }

    /**
     * 使用方法を表示
     */
    private static void printUsage() {
        System.err.println("エラー: 引数が不足しています。\n");
        System.err.println("使用方法:");
        System.err.println("  java -jar d3w-processor.jar <雛型.d3wファイル> <設定.ymlファイル1> [<設定.ymlファイル2> ...]\n");
        System.err.println("説明:");
        System.err.println("  - 雛型.d3wファイルを基に、YAMLの設定を反映した.d3wファイルを1つ生成します");
        System.err.println("  - YAMLの数だけw1, w2, w3...ファイルが生成されます\n");
        System.err.println("例:");
        System.err.println("  java -jar d3w-processor.jar template.d3w config1.yml config2.yml config3.yml");
        System.err.println("  → output_20251229183045.d3w が生成されます（w1, w2, w3を含む）");
    }

    /**
     * 処理開始メッセージを表示
     */
    private static void printHeader(String d3wFilePath, List<String> ymlFilePaths) {
        System.out.println("╔════════════════════════════════════════════════════════╗");
        System.out.println("║           D3W Processor - 処理開始                      ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("\n雛型.d3wファイル: " + d3wFilePath);
        System.out.println("YAMLファイル数: " + ymlFilePaths.size());
        for (int i = 0; i < ymlFilePaths.size(); i++) {
            System.out.println("  [" + (i + 1) + "] " + ymlFilePaths.get(i));
        }
    }

    /**
     * 処理成功メッセージを表示
     */
    private static void printSuccess(String outputPath) {
        System.out.println("\n╔════════════════════════════════════════════════════════╗");
        System.out.println("║           処理完了 - すべて正常に終了しました                ║");
        System.out.println("╚════════════════════════════════════════════════════════╝");
        System.out.println("\n✓ 生成ファイル: " + outputPath);
    }

    /**
     * エラーメッセージを表示
     */
    private static void printError(Exception e) {
        System.err.println("\n╔════════════════════════════════════════════════════════╗");
        System.err.println("║           エラー発生 - 処理を中断しました                    ║");
        System.err.println("╚════════════════════════════════════════════════════════╝");
        System.err.println("\nエラー詳細:");
        e.printStackTrace();
    }
}
