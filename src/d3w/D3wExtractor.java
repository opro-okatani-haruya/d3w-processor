package d3w;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * D3Wファイル（ZIP形式）の解凍を担当するクラス
 */
public class D3wExtractor {
    
    /**
     * .d3wファイル(ZIP)を解凍して、指定ディレクトリに展開する
     * 
     * @param d3wFilePath 解凍対象の.d3wファイルのパス
     * @param outputDir 解凍先ディレクトリ
     * @return 解凍先ディレクトリのPath
     * @throws IOException 解凍処理に失敗した場合
     */
    public static Path extract(String d3wFilePath, String outputDir) throws IOException {
        Path outputPath = Paths.get(outputDir);
        
        // 出力ディレクトリが存在しない場合は作成
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(Paths.get(d3wFilePath)))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path filePath = outputPath.resolve(entry.getName());
                
                if (entry.isDirectory()) {
                    Files.createDirectories(filePath);
                } else {
                    // 親ディレクトリが存在しない場合は作成
                    if (filePath.getParent() != null) {
                        Files.createDirectories(filePath.getParent());
                    }
                    
                    // ファイルを書き出し
                    try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
        
        return outputPath;
    }
    
    /**
     * .d3wファイルを一時ディレクトリに解凍する
     * 
     * @param d3wFilePath 解凍対象の.d3wファイルのパス
     * @return 解凍先の一時ディレクトリのPath
     * @throws IOException 解凍処理に失敗した場合
     */
    public static Path extractToTemp(String d3wFilePath) throws IOException {
        Path tempDir = Files.createTempDirectory("d3w_extract_");
        return extract(d3wFilePath, tempDir.toString());
    }
    
    /**
     * 解凍したディレクトリ内のJSONファイル（拡張子なし）を検索する
     * ファイル名パターン: _, w1, e1, s1, w2, e2, s2, ...
     * 
     * @param extractedDir 解凍されたディレクトリのパス
     * @return 見つかったJSONファイルのパスのリスト
     * @throws IOException ファイル検索に失敗した場合
     */
    public static List<Path> findJsonFiles(Path extractedDir) throws IOException {
        List<Path> jsonFiles = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(extractedDir)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    String fileName = path.getFileName().toString();
                    // _, w数字, e数字, s数字 のパターンにマッチするファイル
                    if (fileName.equals("_") || 
                        fileName.matches("[wes]\\d+")) {
                        jsonFiles.add(path);
                    }
                }
            }
        }
        
        // ファイル名でソート（_, w1, e1, s1, w2, e2, s2, ... の順）
        jsonFiles.sort((p1, p2) -> {
            String name1 = p1.getFileName().toString();
            String name2 = p2.getFileName().toString();
            
            // "_" は最初
            if (name1.equals("_")) return -1;
            if (name2.equals("_")) return 1;
            
            // w, e, s の順で、同じプレフィックスなら数字順
            return compareFileNames(name1, name2);
        });
        
        return jsonFiles;
    }
    
    /**
     * ファイル名を比較する（w1 < e1 < s1 < w2 < e2 < s2 の順）
     */
    private static int compareFileNames(String name1, String name2) {
        char prefix1 = name1.charAt(0);
        char prefix2 = name2.charAt(0);
        
        int num1 = Integer.parseInt(name1.substring(1));
        int num2 = Integer.parseInt(name2.substring(1));
        
        if (num1 != num2) {
            return Integer.compare(num1, num2);
        }
        
        // 同じ番号の場合は w < e < s の順
        return Character.compare(prefix1, prefix2);
    }
}
