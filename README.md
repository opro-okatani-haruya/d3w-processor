# D3W Processor

D3Wファイル（ZIP形式）を解析・編集し、複数のワーク設定を1つのD3WファイルにまとめるCLIツール

## 概要

このツールは、`.d3w`ファイル（実体はZIPファイル）と複数の設定YAMLファイルを受け取り、YAMLの設定内容に基づいて**1つの.d3wファイル**を生成します。YAMLの数だけ`w1`, `w2`, `w3`...のワークファイルが作成されます。

## 主な特徴

- ✅ **JsonObjectベースの実装**: クラス定義なしで柔軟にJSON操作
- ✅ **未知のプロパティの保持**: JSON構造の変更に強い
- ✅ **雛型の完全保持**: envelopes、servicesはそのまま維持
- ✅ **複数ワークの一括生成**: YAMLファイルの数だけワークを自動生成
- ✅ **堅牢なエラーハンドリング**: 詳細なエラー情報を表示

## 機能

1. **YAMLファイルの解析**: 複数のYAMLファイルからワーク設定を読み込み
2. **D3Wファイルの解凍**: ZIPファイルとして格納されているJSONファイルを抽出
3. **雛型の検証**: w1ファイルが存在し、w2以降が存在しないことを確認
4. **ワークファイルの生成**: YAMLの数だけw1, w2, w3...ファイルを生成
5. **メインファイルの編集**: _ファイル（メイン）のworksのみを更新
6. **D3Wファイルの再構築**: すべてのファイルを1つの.d3wファイルにまとめる

## 使用方法

```bash
java -jar d3w-processor.jar <雛型.d3wファイル> <設定.ymlファイル1> [<設定.ymlファイル2> ...]
```

### 引数

- **第1引数**: 雛型となる`.d3w`ファイルのパス（必須）
  - w1ファイルのみを含む必要があります
  - w2以降のファイルがあるとエラーになります
- **第2引数以降**: ワークの設定を記載したYAMLファイルのパス（1つ以上、必須）

### 出力

雛型`.d3w`ファイルと同じディレクトリに、**1つの**編集済み`.d3w`ファイルを生成します。

**出力ファイル名**: `output_yyyyMMddHHmmss.d3w`

**ファイル内容**: `_`, `e1`, `s1`, `s2`, `w1`, `w2`, `w3`...
- `_`: メインファイル（worksのみ更新、envelopesとservicesは保持）
- `e1`: エンベロープファイル（雛型から保持）
- `s1`, `s2`: サービスファイル（雛型から保持）
- `w1`, `w2`, `w3`: ワークファイル（YAMLの数だけ生成）

## 処理の仕組み

```
入力:
  - template.d3w (雛型)
    ├── _ (メインファイル)
    ├── e1 (エンベロープ)
    ├── s1, s2 (サービス)
    └── w1 (雛型ワーク)
  - config1.yaml
  - config2.yaml  
  - config3.yaml

処理:
  1. template.d3wを解凍
  2. w1ファイルの存在とw2以降の不在を検証
  3. w1を_template_workとしてコピー（テンプレート保存）
  4. 元のw1を削除
  5. YAMLを解析
  6. テンプレートからw1生成 (config1の内容)
  7. テンプレートからw2生成 (config2の内容)
  8. テンプレートからw3生成 (config3の内容)
  9. テンプレートファイル削除
  10. _ファイルのworksを更新（envelopes、servicesは保持）
  11. すべてを1つのZIPにまとめる

出力:
  output_20251229183045.d3w
  ├── _ (メインファイル - worksのみ更新)
  ├── e1 (エンベロープ - 雛型から保持)
  ├── s1, s2 (サービス - 雛型から保持)
  ├── w1 (ワーク1)
  ├── w2 (ワーク2)
  └── w3 (ワーク3)
```

## YAMLファイルの形式

```yaml
workName: "ワーク生成_テスト岡谷_1"
memoText: "ワーク自動生成テスト_1"
subject: "ワークテスト件名_1"
documentName: "[文書名]"
templateName: "[テンプレート名]"

datasourceFields:
  - "テンプレート名"
  - "文書名"
  - "あいうえお"
  - "かきくけこ"
  - "さしすせそ"
  - "たちつてと"

textDatasetFields:
  - "あいうえお"
  - "かきくけこ"
  - "さしすせそ"
  - "たちつてと"
```

### フィールド説明

| フィールド | 型 | 説明 |
|----------|------|------|
| `workName` | String | ワーク名 |
| `memoText` | String | メモ（説明文） |
| `subject` | String | 件名 |
| `documentName` | String | ドキュメント名 |
| `templateName` | String | 帳票テンプレート名 |
| `datasourceFields` | List<String> | データソースフィールドのリスト |
| `textDatasetFields` | List<String> | テキストデータセットフィールドのリスト |


### フィールドの適用先

| YAMLフィールド | JSON内の反映先 | 例                                  |
|--------------|--------------|------------------------------------|
| `workName` | `name` | "ワーク生成_テスト岡谷_1"                    |
| `memoText` | `note` | "ワーク自動生成テスト_1"                     |
| `subject` | `content.subject` | "ワークテスト件名_1"                       |
| `documentName` | `content.document.name` | "[文書名]"                            |
| `templateName` | `content.document.template.name` | "[テンプレート名]"                        |
| `datasourceFields` | `content.datasource.fields` | ["あいうえお", "かきくけこ", "さしすせそ", "たちつてと"]      |
| `textDatasetFields` | `content.document.template.params[0].values` | ["あいうえお", "かきくけこ", "さしすせそ", "たちつてと"] |


## D3Wファイルの構造

`.d3w`ファイルは実際にはZIPファイルで、以下のファイルを含みます:

```
.d3w (ZIP)
├── _       (JSON: メインワーク定義)
├── w1      (JSON: ワーク1)
├── w2      (JSON: ワーク2)
├── w3      (JSON: ワーク3)
├── e1      (JSON: エンベロープ1)
├── s1      (JSON: サービス1)
├── s2      (JSON: サービス2)
└── ...
```

各ファイルは**拡張子なしのJSONファイル（1行形式）**です。

### _ファイル（メインファイル）の構造

```json
{
  "works": [
    {
      "content_class": "net.opro.product.d3w.work.csv.CsvWork",
      "key": "",
      "name": "ワーク生成_テスト岡谷_1",
      "path": "w1"
    },
    {
      "content_class": "net.opro.product.d3w.work.csv.CsvWork",
      "key": "",
      "name": "ワーク生成_テスト岡谷_2",
      "path": "w2"
    }
  ],
  "envelopes": [
    {
      "key": "GZvMjSTR6I_GgG",
      "name": "ワーク実行 - ファイル_Box配送_防衛省",
      "content_class": "net.opro.product.d3w.envelope.StandardEnvelope",
      "path": "e1"
    }
  ],
  "services": [
    {
      "key": "GZvMBw6GGI_GcC",
      "name": "Salesforce Files [文書化]_添付ファイル取得(複数)",
      "content_class": "net.opro.product.d3w.service.salesforce.files.SFFilesDocumentService",
      "path": "s1"
    }
  ]
}
```

**重要**: `envelopes`と`services`は雛型から**そのまま保持**されます。変更されるのは`works`のみです。

## 使用例

### 例1: 3つのワークを含む.d3wを生成

```bash
java -jar d3w-processor.jar template.d3w \
  configs/invoice.yaml \
  configs/balance.yaml \
  configs/payment.yaml
```

**実行結果:**
```
╔════════════════════════════════════════════════════════╗
║           D3W Processor - 処理開始                      ║
╚════════════════════════════════════════════════════════╝

雛型.d3wファイル: template.d3w
YAMLファイル数: 3
  [1] configs/invoice.yaml
  [2] configs/balance.yaml
  [3] configs/payment.yaml

=== 処理開始 ===
✓ 読み込み完了: 3個のワーク設定
✓ 解凍完了: C:\Users\...\Temp\d3w_extract_12345
✓ 既存ファイル: 5個

--- 雛型ファイル検証 ---
  ✓ w1ファイル: 存在
✓ 検証完了

--- 雛型ワークファイル保存 ---
✓ テンプレート保存: w1 → _template_work

--- 雛型ワークファイル削除 ---
✓ 削除: w1

--- ワークファイル生成 ---
[1/3] 月次請求書発行
  ✓ 生成: w1
[2/3] 残高証明書生成
  ✓ 生成: w2
[3/3] 支払通知書
  ✓ 生成: w3

✓ テンプレートファイル削除: _template_work

--- メインファイル編集 ---
✓ 編集完了: _

--- D3Wファイル構築 ---
✓ 構築完了: output_20251229183045.d3w
✓ クリーンアップ完了

=== 処理完了 ===

╔════════════════════════════════════════════════════════╗
║           処理完了 - すべて正常に終了しました             ║
╚════════════════════════════════════════════════════════╝

✓ 生成ファイル: C:\path\to\output_20251229183045.d3w
```

生成されたファイル:
- `output_20251229183045.d3w` 
  - 内部: `_`, `e1`, `s1`, `s2`, `w1`, `w2`, `w3`

### 例2: 単一ワークの.d3wを生成

```bash
java -jar d3w-processor.jar template.d3w single_config.yaml
```

生成されたファイル:
- `output_20251229183100.d3w`
  - 内部: `_`, `e1`, `s1`, `s2`, `w1`

## ビルド方法

### 前提条件

- Java 8以上
- IntelliJ IDEA（推奨）

### 依存ライブラリ

- **SnakeYAML 1.33**: YAML解析
- **Gson 2.8.9**: JSON処理
- **Lombok 1.18.30**: ボイラープレートコード削減

### ビルド手順

1. IntelliJ IDEAでプロジェクトを開く
2. ライブラリの追加（Gsonが必要な場合）
   - `File` → `Project Structure...` → `Libraries`
   - `+` → `From Maven...`
   - `com.google.code.gson:gson:2.8.9` を検索して追加
3. `Build` → `Build Artifacts...` → `Build`
4. `build/d3w-processor.jar` が生成される

## プロジェクト構造

```
d3w-processor/
├── src/
│   ├── d3w/
│   │   ├── Main.java                 # メインエントリーポイント
│   │   ├── Launcher.java             # UI  
│   │   ├── D3wProcessor.java         # メイン処理ロジック
│   │   ├── D3wExtractor.java         # ZIP解凍
│   │   ├── JsonEditor.java           # JSON編集（JsonObjectベース）
│   │   ├── D3wBuilder.java           # ZIP再構築
│   │   ├── YamlInfoLoader.java       # YAML読み込み
│   │   ├── TempDirectory.java        # 一時ディレクトリ管理
│   │   └── model/
│   │       └── YamlInfo.java         # YAML情報モデル
│   └── test/
│       └── java/d3w/
│           ├── YamlInfoLoaderTest.java
│           ├── JsonEditorTest.java
│           └── D3wProcessorTest.java
├── lib/
│   └── gson-2.8.9.jar
├── d3w-processor.iml
└── README.md
```

## アーキテクチャ

```
┌─────────────┐
│Launcher.java│
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Main.java │
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ D3wProcessor     │ ← メイン処理制御
└─────┬────────────┘
      │
      ├─► YamlInfoLoader  ← YAML解析
      ├─► D3wExtractor    ← ZIP解凍
      ├─► JsonEditor      ← JSON編集（JsonObjectベース）
      ├─► D3wBuilder      ← ZIP再構築
      └─► TempDirectory   ← 一時ディレクトリ管理
```

## 処理フロー詳細

1. **入力検証**: コマンドライン引数の検証
2. **YAML解析**: 各YAMLファイルを`YamlInfo`オブジェクトに変換
3. **D3W解凍**: 雛型`.d3w`ファイルを一時ディレクトリに解凍
4. **雛型検証**: 
   - w1ファイルが存在することを確認
   - w2以降のファイルが存在しないことを確認
5. **テンプレート保存**: w1を_template_workとして別名保存
6. **雛型削除**: 元のw1ファイルを削除
7. **ワークファイル生成**: 
   - 各YamlInfoに対してテンプレートからw1, w2, w3...を生成
   - YAMLの内容を反映
   - keyは空文字列に設定
8. **テンプレート削除**: _template_workファイルを削除
9. **メインファイル編集**: 
   - 既存の_ファイルを読み込み
   - `works`配列のみを更新
   - `envelopes`と`services`はそのまま保持
10. **D3W再構築**: すべてのファイルを1つのZIPにまとめる
11. **ファイル出力**: タイムスタンプ付きファイル名で保存
12. **クリーンアップ**: 一時ディレクトリを削除

## JsonObjectベースの実装

### なぜJsonObjectを使うのか

従来のクラス定義ベースの実装では、JSON構造が変わるたびにクラスを修正する必要がありました。JsonObjectベースの実装には以下のメリットがあります:

1. **柔軟性**: JSON構造が変わっても影響を受けない
2. **保守性**: クラス定義が不要
3. **安全性**: 未知のプロパティを失わない
4. **シンプル**: コードが明確で理解しやすい

### 実装例

```java
// JSON読み込み
JsonObject content = JsonEditor.readJson(file);

// 特定の項目のみを差し替え
content.addProperty("name", "新しいワーク名");

// ネストされた項目にアクセス
JsonObject nested = content.getAsJsonObject("content");
nested.addProperty("subject", "新しい件名");

// JSON書き込み（未知のプロパティも保持される）
JsonEditor.writeJson(content, file);
```

## テスト

### テストの実行

IntelliJ IDEAで:
1. `src/test/java/d3w` を右クリック
2. `Run 'Tests in 'd3w''` を選択

または個別に:
- `YamlInfoLoaderTest` - YAML読み込みテスト
- `JsonEditorTest` - JSON編集テスト
- `D3wProcessorTest` - 統合テスト（End-to-End）

### 統合テストの内容

`D3wProcessorTest`では以下をテストします:

1. **単一YAML処理**: 1つのYAMLから.d3wを生成（w1を含む）
2. **複数YAML処理**: 3つのYAMLから.d3wを生成（w1, w2, w3を含む）
3. **w1不在エラー**: 雛型にw1がない場合のエラー
4. **w2存在エラー**: 雛型にw2以降がある場合のエラー
5. **ファイル存在確認**: 生成された.d3w内のファイル検証
6. **内容検証**: 各wファイルの内容が正しいことを確認

## エラーハンドリング

### 詳細なエラー表示

エラー発生時には以下の情報が表示されます:

```
╔════════════════════════════════════════════════════════╗
║           エラー発生 - 処理を中断しました                ║
╚════════════════════════════════════════════════════════╝

エラー詳細:
エラータイプ: IllegalStateException
メッセージ: 雛型.d3wファイルにw1ファイルが存在しません。

スタックトレース:
  at d3w.D3wProcessor.validateTemplateFiles(D3wProcessor.java:157)
  at d3w.D3wProcessor.process(D3wProcessor.java:68)
  ...

原因: IOException: ファイルが見つかりません
```

### 主なエラーケース

- **ファイルが存在しない**: 詳細なエラーメッセージを表示
- **YAMLフォーマットが不正**: パースエラーを表示
- **w1ファイルが存在しない**: バリデーションエラー
- **w2以降のファイルが存在する**: バリデーションエラー
- **権限エラーなどのI/Oエラー**: スタックトレースと共に表示

## 注意事項

- `.d3w`ファイルは**1つだけ**生成されます
- YAMLの数だけ`w1`, `w2`, `w3`...が生成されます
- JSONファイルは**1行形式**で保存されます（改行なし）
- 一時ディレクトリは処理後に自動削除されます
- **雛型の構造は保持**: `envelopes`と`services`はそのまま残ります
- **worksのみ更新**: _ファイルでは`works`配列のみが書き換えられます
- **keyは空**: 生成されるワークの`key`は空文字列に設定されます
- **JsonObjectベースの実装**: クラス定義なしで柔軟にJSON操作を行います
  - 特定の項目のみを差し替えるため、未知のプロパティも保持されます
  - JSON構造の変更に強い実装になっています

## トラブルシューティング

### ライブラリが見つからない

```bash
# IntelliJ IDEAで
File → Project Structure... → Libraries
+ → From Maven...
com.google.code.gson:gson:2.8.9 を検索して追加
```

### JARファイルが実行できない

```bash
# Java 8以上がインストールされているか確認
java -version

# JARファイルの実行
java -jar d3w-processor.jar template.d3w config.yaml
```

### 雛型ファイルのエラー

**エラー**: `雛型.d3wファイルにw1ファイルが存在しません`
- 雛型.d3wファイルにw1ファイルを含める必要があります

**エラー**: `雛型.d3wファイルにw2以降のファイルが存在します`
- 雛型.d3wファイルにはw1ファイルのみを含める必要があります
- w2以降のファイルは自動生成されるため、事前に存在してはいけません

## よくある質問

### Q: envelopesやservicesは変更されますか？
A: いいえ、`envelopes`と`services`は雛型からそのまま保持されます。変更されるのは`works`のみです。

### Q: 生成されるワークのkeyは何ですか？
A: 生成されるワークの`key`は空文字列("")に設定されます。

### Q: JSON構造が変わった場合、コードを修正する必要がありますか？
A: いいえ、JsonObjectベースの実装なので、特定の項目以外は自動的に保持されます。

### Q: 複数のYAMLファイルを一度に処理できますか？
A: はい、コマンドライン引数として複数のYAMLファイルを指定できます。YAMLの数だけw1, w2, w3...が生成されます。

### Q: 出力ファイルの名前は変更できますか？
A: 現在は`output_yyyyMMddHHmmss.d3w`形式で自動生成されます。タイムスタンプが含まれるため、ファイル名の衝突は発生しません。

## バージョン履歴

### v1.0.0 (2025/12/29)
- 初回リリース
- 基本的なD3Wファイル処理機能
- JsonObjectベースの実装に全面移行
- D3wContentクラスを削除し、柔軟なJSON操作を実現
- envelopes、servicesの完全保持機能を追加
- UI実装