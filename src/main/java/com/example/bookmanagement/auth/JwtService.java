package com.example.bookmanagement.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String secret; // 共通鍵
    @Value("${jwt.expiration}")
    private long expiration; // トークンの有効期限(ミリ秒)

    private SecretKey getSigningKey() {
        byte[] decodedKey = Base64.getDecoder().decode(secret); // 共通鍵をBase64から元のバイト文字列に戻す
        return Keys.hmacShaKeyFor(decodedKey); // それをHS256で使える形にする
    }

//    JWTトークン作成
    public String generateToken(String mail) {
        return Jwts.builder() // トークン組み立て開始
            .subject(mail) // 誰のものかを記録
            .issuedAt(new Date()) // 発行日時を記録
            .expiration(new Date(System.currentTimeMillis() + expiration)) // 有効期限を記録
            .signWith(getSigningKey(), Jwts.SIG.HS256) // 秘密鍵でHS256署名をつける
            .compact(); // 完成したJWTを文字列に
    }

//    JWTトークンからユーザ名を取得
    public String getMailFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

//    JWTトークンから有効期限を取得
    public Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

//    JWTトークンをパースしてClaimsを取得
    private Claims getClaimsFromToken(String token) {
        return Jwts.parser() // JWT読み取り処理を開始
            .verifyWith(getSigningKey()) // 署名確認に使う秘密鍵を指定
            .build()
            .parseSignedClaims(token) // 署名付きのJWTとして読み込む
            .getPayload(); // 読み取った本文の情報を返す
    }

//    トークンの有効期限チェック
    public Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token); // JWTから期限を取り出す
        return expiration.before(new Date()); // 期限が過ぎているかをチェック
    }

//    トークンの有効性チェック
    public Boolean validateToken(String token, String mail) {
        String tokenMail = getMailFromToken(token); // トークンからメールを取り出す
        return (tokenMail.equals(mail) && !isTokenExpired(token)); // メールが一致していて期限内であればtrueが返る
    }


}
