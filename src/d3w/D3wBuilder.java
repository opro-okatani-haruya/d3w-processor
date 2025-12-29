package d3w;

import java.io.*;
import java.nio.file.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 編集したJSONファイルをD3Wファイル（ZIP形式）に再構築するクラス
 */
public class D3wBuilder {
    
    /**
     * 指定ディレクトリ内のファイルをZIP化して.d3wファイルを作成する
     * 
     * @param sourceDir ZIP化するディレクトリ
     * @param outputD3wPath 出力する.d3wファイルのパス
     * @throws IOException ZIP化に失敗した場合
     */
    public static void build(Path sourceDir, String outputD3wPath) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Paths.get(outputD3wPath)))) {
            List<Path> files = D3wExtractor.findJsonFiles(sourceDir);
            
            for (Path file : files) {
                String fileName = file.getFileName().toString();
                
                // ZIPエントリを作成
                ZipEntry zipEntry = new ZipEntry(fileName);
                zos.putNextEntry(zipEntry);
                
                // ファイル内容を書き込み
                byte[] bytes = Files.readAllBytes(file);
                zos.write(bytes);
                zos.closeEntry();
            }
        }
    }

}
