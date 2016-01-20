
# これは何？

一言で言えばQiitaクローンです。



## 現時点で実装済の機能

- 記事は[Markdown](http://qiita.com/Qiita/items/c686397e4a0f4f11683d)と呼ばれるHTMLの簡略記法（GitHub,Qiitaで採用）で記述
  - シンタックスハイライト
  - リアルタイムプレビュー
- 記事に対するタグ付け
- 記事をストック
- 記事に対するコメント
- 以下の場合にサイト内通知を表示
    - 自分の記事がストック/コメントされた時
    - 自分がコメントした記事にコメントされた時


## 今後実装予定の機能
- コメント、およびストックされた時に、記事の作者へのメール通知を送信
- 全文検索
- ユーザアイコンの登録/表示
- 非公開記事の作成機能
- ストック記事の更新時の通知

## 開発情報
|             |             |
|-------------|-------------|
| 言語          | [Scala](http://www.scala-lang.org/)|
| WEBフレームワーク  | [Scalatra](http://www.scalatra.org)    |
| RDBMS       | [H2Database](http://www.h2database.com) |
| DBアクセスライブラリ | [ScalikeJDBC](http://scalikejdbc.org) |
| Markdown Parser | [markedj](https://github.com/gitbucket/markedj) |
| CSSフレームワーク  | [Materialize](http://materializecss.com/) |
| JSライブラリ     | [knockoutJS](http://knockoutjs.com), [jQuery](https://jquery.com) |
| WEBサーバ      | 組込み[Jetty](http://www.eclipse.org/jetty/),任意のServletコンテナ |


