package d3w.model;

import lombok.Data;

import java.util.List;

@Data
public class YamlInfo {
    /**
     * ワーク名:name
     */
    private String workName;
    /**
     * メモ:note
     */
    private String memoText;
    /**
     * 件名:content.subject
     */
    private String subject;
    /**
     * 文書名:content.document.name
     */
    private String documentName;
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

    public YamlInfo(String workName, String memoText, String subject, String documentName, String templateName, List<String> textDatasetFields, List<String> datasourceFields) {
        this.workName = workName;
        this.memoText = memoText;
        this.subject = subject;
        this.documentName = documentName;
        this.templateName = templateName;
        this.textDatasetFields = textDatasetFields;
        this.datasourceFields = datasourceFields;
    }
}
