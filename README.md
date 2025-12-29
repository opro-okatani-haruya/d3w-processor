# D3W Processor

D3Wファイル（ZIP形式）を解析・編集し、複数のワーク設定を1つのD3WファイルにまとめるCLIツール

## 概要

このツールは、`.d3w`ファイル（実体はZIPファイル）と複数の設定YAMLファイルを受け取り、YAMLの設定内容に基づいて**1つの.d3wファイル**を生成します。YAMLの数だけ`w1`, `w2`, `w3`...のワークファイルが作成されます。

## 機能

1. **YAMLファイルの解析**: 複数のYAMLファイルからワーク設定を読み込み
2. **D3Wファイルの解凍**: ZIPファイルとして格納されているJSONファイルを抽出
3. **ワークファイルの生成**: YAMLの数だけw1, w2, w3...ファイルを生成
4. **メインファイルの編集**: _ファイル（メイン）を編集
5. **D3Wファイルの再構築**: すべてのファイルを1つの.d3wファイルにまとめる

## 使用方法

```bash
java -jar d3w-processor.jar <雛型.d3wファイル> <設定.ymlファイル1> [<設定.ymlファイル2> ...]
```

### 引数

- **第1引数**: 雛型となる`.d3w`ファイルのパス（必須）
- **第2引数以降**: ワークの設定を記載したYAMLファイルのパス（1つ以上、必須）

### 出力

雛型`.d3w`ファイルと同じディレクトリに、**1つの**編集済み`.d3w`ファイルを生成します。

**出力ファイル名**: `output_yyyyMMddHHmmss.d3w`

**ファイル内容**: `_`, `w1`, `w2`, `w3`...（YAMLの数だけwファイルが生成される）

## 処理の仕組み

```
入力:
  - template.d3w (雛型)
  - config1.yaml
  - config2.yaml  
  - config3.yaml

処理:
  1. template.d3wを解凍
  2. YAMLを解析
  3. w1ファイル生成 (config1の内容)
  4. w2ファイル生成 (config2の内容)
  5. w3ファイル生成 (config3の内容)
  6. _ファイルを編集
  7. すべてを1つのZIPにまとめる

出力:
  output_20251229183045.d3w
  ├── _ (メインファイル)
  ├── w1 (ワーク1)
  ├── w2 (ワーク2)
  └── w3 (ワーク3)
```

## YAMLファイルの形式

```yaml
workName: "月次請求書発行"
memoText: "毎月末に実行する請求書発行処理"
subject: "2025年1月分請求書"
templateName: "invoice_template_v2"

textDatasetFields:
  - company_name
  - invoice_date
  - due_date
  - total_amount

datasourceFields:
  - Id
  - Name
  - Amount__c
  - Status__c
  - Account.Name
```

### フィールド説明

| フィールド | 型 | 説明 |
|----------|------|------|
| `workName` | String | ワーク名 |
| `memoText` | String | メモ（説明文） |
| `subject` | String | 件名 |
| `templateName` | String | 帳票テンプレート名 |
| `textDatasetFields` | List<String> | テキストデータセットフィールドのリスト |
| `datasourceFields` | List<String> | データソースフィールドのリスト |

## D3Wファイルの構造

`.d3w`ファイルは実際にはZIPファイルで、以下のファイルを含みます:

```
.d3w (ZIP)
├── _       (JSON: メインワーク定義)
├── w1      (JSON: ワーク1)
├── w2      (JSON: ワーク2)
├── w3      (JSON: ワーク3)
├── e1      (JSON: イベント1) ※オプション
├── s1      (JSON: ステップ1) ※オプション
└── ...
```

各ファイルは**拡張子なしのJSONファイル（1行形式）**です。

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
║           D3W Processor - 処理開始                    ║
╚════════════════════════════════════════════════════════╝

雛型.d3wファイル: template.d3w
YAMLファイル数: 3
  [1] configs/invoice.yaml
  [2] configs/balance.yaml
  [3] configs/payment.yaml

生成予定:
  - 1つの.d3wファイル（w1～w3を含む）

=== 処理開始 ===
✓ 読み込み完了: 3個のワーク設定
✓ 解凍完了: C:\Users\...\Temp\d3w_extract_12345
✓ 既存ファイル: 1個

--- ワークファイル生成 ---
[1/3] 月次請求書発行
  ✓ 生成: w1
[2/3] 残高証明書生成
  ✓ 生成: w2
[3/3] 支払通知書
  ✓ 生成: w3

--- メインファイル編集 ---
✓ 編集完了: _

--- D3Wファイル構築 ---
✓ 構築完了: output_20251229183045.d3w
✓ クリーンアップ完了

=== 処理完了 ===

╔════════════════════════════════════════════════════════╗
║           処理完了 - すべて正常に終了しました         ║
╚════════════════════════════════════════════════════════╝

✓ 生成ファイル: C:\path\to\output_20251229183045.d3w
```

生成されたファイル:
- `output_20251229183045.d3w` (内部: `_`, `w1`, `w2`, `w3`)

### 例2: 単一ワークの.d3wを生成

```bash
java -jar d3w-processor.jar template.d3w single_config.yaml
```

生成されたファイル:
- `output_20251229183100.d3w` (内部: `_`, `w1`)

## JSON加工のサンプルコード

詳細なJSON操作のサンプルコードは`JsonManipulationExamples.java`に含まれています:

### サンプル内容

1. **基本的なJSON読み込みと書き込み**
2. **ネストされたオブジェクトの操作**
3. **配列（リスト）の操作**
4. **条件に基づく動的なJSON生成**
5. **既存JSONの部分的な更新**
6. **パラメータ配列の構築**
7. **1行JSON形式での書き込み（D3W形式）**
8. **複雑なJSON構造の完全な例**

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
2. `ファイル` → `キャッシュの無効化/再起動...` → `無効化して再起動`
3. ライブラリが自動ダウンロードされることを確認
4. `ビルド` → `アーティファクトをビルド...` → `ビルド`
5. `out/artifacts/d3w-processor/d3w-processor.jar` が生成される

## プロジェクト構造

```
d3w-processor/
├── src/
│   ├── d3w/
│   │   ├── Main.java              # メインエントリーポイント
│   │   ├── D3wProcessor.java      # メイン処理ロジック
│   │   ├── D3wExtractor.java      # ZIP解凍
│   │   ├── JsonEditor.java        # JSON編集（JsonObjectベース）
│   │   ├── D3wBuilder.java        # ZIP再構築
│   │   ├── YamlInfo.java          # ワーク情報モデル
│   │   ├── YamlInfoLoader.java    # YAML読み込み
│   │   ├── model/
│   │   │   └── D3wMainContent.java    # _ファイル用JSON構造モデル
│   │   └── examples/
│   │       └── JsonManipulationExamples.java  # JSON操作サンプル
│   └── test/
│       ├── java/d3w/
│       │   ├── YamlInfoLoaderTest.java
│       │   ├── JsonEditorTest.java
│       │   └── D3wProcessorTest.java      # 統合テスト
│       └── resources/configs/
│           ├── invoice_work.yaml
│           ├── balance_work.yaml
│           └── payment_work.yaml
├── d3w-processor.iml
└── README.md
```

## アーキテクチャ

```
┌─────────────┐
│   Main.java │ ← エントリーポイント
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ D3wProcessor     │ ← メイン処理制御
└─────┬────────────┘
      │
      ├─► YamlInfoLoader  ← YAML解析
      ├─► D3wExtractor    ← ZIP解凍
      ├─► JsonEditor      ← JSON編集
      └─► D3wBuilder      ← ZIP再構築（1ファイル）
```

## 処理フロー

1. **入力検証**: コマンドライン引数の検証
2. **YAML解析**: 各YAMLファイルを`YamlInfo`オブジェクトに変換
3. **D3W解凍**: 雛型`.d3w`ファイルを一時ディレクトリに解凍
4. **ワークファイル生成**: 
   - 各YamlInfoに対してw1, w2, w3...ファイルを生成
   - YAMLの内容を反映
5. **メインファイル編集**: `_`ファイルに全体設定を反映
6. **D3W再構築**: すべてのファイルを1つのZIPにまとめる
7. **ファイル出力**: タイムスタンプ付きファイル名で保存
8. **クリーンアップ**: 一時ディレクトリを削除

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
3. **ファイル存在確認**: 生成された.d3w内のファイル検証
4. **内容検証**: 各wファイルの内容が正しいことを確認

## エラーハンドリング

- ファイルが存在しない場合 → 詳細なエラーメッセージを表示
- YAMLフォーマットが不正な場合 → パースエラーを表示
- D3Wファイルの構造が不正な場合 → 構造エラーを表示
- 権限エラーなどのI/Oエラー → スタックトレースと共に表示

すべてのエラーは詳細なメッセージとスタックトレースと共に表示されます。

## 注意事項

- `.d3w`ファイルは**1つだけ**生成されます
- YAMLの数だけ`w1`, `w2`, `w3`...が生成されます
- JSONファイルは**1行形式**で保存されます（改行なし）
- 一時ディレクトリは処理後に自動削除されます
- **JsonObjectベースの実装**: クラス定義なしで柔軟にJSON操作を行います
  - 特定の項目のみを差し替えるため、未知のプロパティも保持されます
  - JSON構造の変更に強い実装になっています

## トラブルシューティング

### ライブラリが見つからない

```bash
# IntelliJ IDEAで
ファイル → キャッシュの無効化/再起動... → 無効化して再起動
```

### JARファイルが実行できない

```bash
# Java 8以上がインストールされているか確認
java -version

# JARファイルの実行
java -jar d3w-processor.jar template.d3w config.yaml
```

## ライセンス

（適切なライセンスを記載してください）

## 作者

（作者情報を記載してください）
