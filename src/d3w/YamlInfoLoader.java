package d3w;

import d3w.model.YamlInfo;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * YAMLファイルからYamlInfoオブジェクトを読み込むユーティリティクラス
 */
public class YamlInfoLoader {

    /**
     * YAMLファイルから単一のYamlInfoを読み込む
     *
     * @param yamlFilePath YAMLファイルのパス
     * @return YamlInfoオブジェクト
     * @throws Exception 読み込みに失敗した場合
     */
    public static YamlInfo loadFromYaml(String yamlFilePath) throws Exception {
        Yaml yaml = new Yaml();
        try (InputStream inputStream = Files.newInputStream(Paths.get(yamlFilePath))) {
            Map<String, Object> data = yaml.load(inputStream);
            return mapToYamlInfo(data);
        }
    }

    /**
     * MapをYamlInfoオブジェクトに変換
     *
     * @param map YAMLから読み込んだMap
     * @return YamlInfoオブジェクト
     */
    @SuppressWarnings("unchecked")
    private static YamlInfo mapToYamlInfo(Map<String, Object> map) {
        return new YamlInfo(
                (String) map.get("workName"),
                (String) map.get("memoText"),
                (String) map.get("subject"),
                (String) map.get("documentName"),
                (String) map.get("templateName"),
                (List<String>) map.get("textDatasetFields"),
                (List<String>) map.get("datasourceFields")
        );
    }

    /**
     * ディレクトリ内の全YAMLファイルを読み込む
     *
     * @param directoryPath YAMLファイルが格納されているディレクトリのパス
     * @return YamlInfoオブジェクトのリスト
     */
    public static List<YamlInfo> loadAllFromDirectory(String directoryPath) {
        File dir = new File(directoryPath);
        File[] yamlFiles = dir.listFiles((d, name) ->
                name.endsWith(".yaml") || name.endsWith(".yml"));

        if (yamlFiles == null || yamlFiles.length == 0) {
            return Collections.emptyList();
        }

        return Arrays.stream(yamlFiles)
                .map(file -> {
                    try {
                        return loadFromYaml(file.getAbsolutePath());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load: " + file.getName(), e);
                    }
                })
                .collect(Collectors.toList());
    }
}
