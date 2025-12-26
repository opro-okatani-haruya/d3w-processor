package d3w;

import lombok.Data;

import java.util.List;

@Data
public class WorkInfo {
    /**
     * ワーク名:name
     */
    private String workName;
    /**
     * メモ:note
     */
    private String memoText;
    /**
     * 帳票テンプレート名:content.document.template.name
     */
    private String templateName;
    /**
     * テキストデータセットフィールド:content.document.template.params.[values]
     */
    private List<String> textDatasetFields;
    /**
     * データソース:content.datasource.fields
     */
    private List<String> datasourceFields;

    public WorkInfo(String workName, String memoText, String templateName, List<String> textDatasetFields, List<String> datasourceFields) {
        this.workName = workName;
        this.memoText = memoText;
        this.templateName = templateName;
        this.textDatasetFields = textDatasetFields;
        this.datasourceFields = datasourceFields;
    }
}
