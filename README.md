# Absolute Magic v0.5.3 (未コンパイル・未検証)

> v0.5.2: 魔術師の出現条件と威力を調整。v0.5.3: 魔術師が周りのモンスターを回復・強化する機能を追加。
> どちらも**まだ一度もビルド・実機確認をしていません**。GitHub Actions のビルドで出たエラーは、そのまま貼れば直せます。

Iron's Spells 'n Spellbooks 3.16.2 (1.20.1 / Forge 47.4.0+) 用アドオン。

| 魔法 | 内部名 | 学派 | 内容 |
|---|---|---|---|
| フィックスト・ストライク | fixed_damage | Holy | 攻撃のたびに固定値の確定ダメージ追加(v0.3.0 から変更なし) |
| パーセント・ストライク | percent_damage | Ender | 攻撃のたびに敵HP割合の確定ダメージ追加(v0.3.0 から変更なし) |
| **タイム・ボム** (新) | time_bomb | Fire | 狙った相手に時限爆弾 (下記) |

## タイム・ボム
- 視線の先の生き物(射程32)に発動。マナ100、クールダウン60秒、最大レベル1。
- 導火線は10秒 (config `time_bomb.fuseSeconds`)。残り時間はエフェクトアイコンにも出る。
- **プレイヤー**: 毎秒チャットに「[時限爆弾] 爆発まで N」。0で爆発して死亡(トーテム無効、吸収ハートも無視)。
- **モブ**: 10秒後に確定ダメージ20 (config `mobDamage`)。即死はしない。
- 爆発は演出のみ(ブロック破壊なし、周囲への巻き込みダメージなし)。
- 爆弾を付けられたプレイヤーが**他のプレイヤーを近接攻撃**すると、爆弾は相手に移る(残り時間は引き継ぎ。
  `transferResetsTimer=true` で毎回フルに戻す)。キルは最初に仕掛けた術者の撃破として扱う。
- クリエイティブ/スペクテイター中のプレイヤーは対象外(爆発時は不発)。牛乳では解除できない。`/effect clear` で解除可。
- 爆弾中にログアウトすると導火線は止まり、ログインで再開する。

## 魔術師 (Magician) 特性 (v0.5 新規 / L2 Hostility 2.5.19 必須)
- L2Hostility の特性 `absolutemagic:magician` (最大ランク5)。付いたモブが、標的に Iron's Spells の魔法を撃つ。
- 全モブ対象。付き方は L2Hostility の通常の特性抽選 (最低レベル75・コスト45・重み50・最大ランク5)。
  変更は `data/absolutemagic/l2hostility_config/trait/magician.json` (データパックで上書き可)。
- 特性レベルNのモブは、config の level1〜levelN の魔法リストすべて(累積)からランダムに選ぶ。
- マナは使わない。**撃ち終わってから最低6秒**(config `cooldownSeconds`、既定8、下限6)は次を撃たない。
- 詠唱中は**立ち止まる**(移動速度-100%、経路停止)。専用モーションが無いので腕を振る動作で代用。
  INSTANT魔法は `instantWindupTicks`(既定20=1秒)、LONG魔法は魔法自身の詠唱時間。
- 威力 = 魔法レベル(特性レベル+offset)と、モブの `spell_power` 属性。
  spell_power に「特性レベル×0.25 + モブレベル×0.08」(上限30.0)を加算 (config `[power]`)。
- 誤射対策は無し。持続型(CONTINUOUS)の魔法は未対応(自動でスキップ)。
- config: `config/absolutemagic-magician.toml`
- **特性アイテム (v0.5.1)**: 「魔術師」アイテム(`absolutemagic:magician`)がクリエイティブの L2Hostility タブに入る。
  他の特性アイテムと同じく、持ってモブを右クリックすると特性が付く(L2Hostility の TraitSymbol)。
  アイコンは `assets/absolutemagic/textures/item/trait/magician.png` (16x16)。差し替えるときは同じ名前・サイズで。
  タブに出ない時は、クリエイティブの検索欄に「魔術師」と入力する。
- 動作確認: 特性レベル付きのモブを出す。例 `/l2hostility` 系コマンドや、ボスのエンティティ設定の traits に
  `absolutemagic:magician` を指定する。魔法が出ない時は latest.log を `[Magician]` で検索。

## ビルド
### GitHub Actions (推奨)
1. このフォルダの中身をリポジトリに push する(`libs/` の jar も含める)。
2. Actions タブの `build` が走り、成功すると Artifacts の `absolutemagic-jar` に `absolutemagic-0.5.3.jar` が入る。
3. `v0.5.3` のような `v` で始まるタグを push すると、Releases にも jar が付く。
- 注意: `libs/` には他の mod (Iron's Spells、L2Hostility など) の jar が入っている。**公開リポジトリに置くと再配布になるので、リポジトリは private にすること。**
- `libs/` の7つはビルド用の参照 jar。mod 本体には含まれない。

### ローカル
JDK 17 と Gradle 8.1.1 (または Forge 1.20.1-47.4.0 MDK の gradlew) で `gradle build` → `build/libs/absolutemagic-0.5.3.jar`。

## 動作確認
- 魔法を覚える前に効果だけ: `/effect give <対象> absolutemagic:time_bomb 10 0 true`
  (この方法だと術者がいないので、キル扱いなしで爆発する)

## 設定 config/absolutemagic-common.toml  [time_bomb]
fuseSeconds(10) / mobDamage(20.0) / canTargetPlayers(true) / transferResetsTimer(false) / forceKillPlayers(true)
