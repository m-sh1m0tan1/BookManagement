# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

@AGENTS.md

## Claude Code 向けの補足

AGENTS.md に書かれていない内容だけをここに置く。

### テストの実行

- 単一のテストメソッド: `mvnw.cmd -Dtest=LoanServiceTests#メソッド名 test`
- DB を使うテスト(`@SpringBootTest`、`*ConcurrencyTests`)には `DATABASE_*` と `JWT_SECRET` の環境変数が必要。`fnox exec -- mvnw.cmd test` のように渡す。

### アーキテクチャの要点

- `auth/`: `JwtAuthenticationFilter` が JWT を検証して利用者を識別する。`security/SecurityConfig` がステートレスな `SecurityFilterChain` に組み込む。`/api/auth/**` と静的リソースは公開、`/api/books` の POST/PUT/DELETE は `ROLE_ADMIN`、それ以外は認証必須。CSRF は無効。
- `exception/handler/`: 業務例外を `dto/*ErrorResponse` の JSON に変換する。
- 貸出まわり(`LoanService` / `LoanMapper`)は、利用者行→本の行の順のロックと、同一トランザクション内の件数確認が前提。変更の検証には PostgreSQL を使う `LoanConcurrencyTests` を使う。
