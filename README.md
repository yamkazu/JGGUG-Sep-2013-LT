[JGGUG名物・ライトじゃないLT大会 - JGGUG G*ワークショップZ Sep 2013](http://jggug.doorkeeper.jp/events/5745)のLT資料です。

# テーマ: Grailsで高速にインサートしたい

* Hibernate前提のお話
* 今回はPostgreSQLを使っています

## よくある例

ドメイン:
```
class UniqueDomain {
    String value
    static constraints = {
        value unique: true
    }
}
```

いっぱい保存するコード:

```
10000.times {
    new UniqueDomain(value: "$it").save()
    if (it % 50 == 0) {
        session.flush()
    }
}
session.flush()
```

### 問題点

* ユニーク制約を保証するSQLが大量(保存する件数x2)に発行される
* シーケンスから値を払い出すSQLが保存する件数分発行される

## バリデーション時のデータベースアクセスをやめる

```
class SimpleDomain {
    String value
}
```

でもユニーク制約どうすんの？

* データベースのUnique制約にまかせる
* アプリケーション側でなんとかする
    * 初めに一度だけ全件取得しておいてアプリで頑張る
* 排他制御的なあれには注意

## ID自動採番時にデータベースアクセスをしない

```
class IncrementIdDomain {
    String value

    static mapping = {
        id generator: "increment"
    }
}
```

`id generator:`で保存ごとにデータベースアクセスを伴わない方式を選択する。
`increment`は初めにテーブルの最大値を取得して、そこからインクリメントしてくれる。
同時にプロセスが動作する場合は使用不可。

`increment`以外にもいくつかある。

see: <http://grails.jp/doc/latest/ref/Database%20Mapping/id.html>

## StatelessSessionを使う

状態を持たないセッション。カスケードとか1キャッシュとかない。

```
def session = sessionFactory.openStatelessSession()
def tx = session.beginTransaction()
iterations.times {
  ...
}
tx.commit()
session.close()
```

コレクションを保存できないという制約がある。単一終端関連は保存できる。

see <http://docs.jboss.org/hibernate/core/3.6/reference/ja-JP/html/batch.html#batch-statelesssession>

## まとめ

高速にインサートしたいなら

* バリデーション時に大量のSQL発行されいないように注意する
* ID払い出しをアプリケーション側でがんばる
* StatelessSessionが使えるなら使う

