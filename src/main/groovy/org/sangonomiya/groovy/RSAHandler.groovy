package org.sangonomiya.groovy

import org.apache.commons.codec.binary.Base64
import org.sangonomiya.kotlin.Pair

import javax.crypto.Cipher
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

/**
 * RSA非对称加密工具
 * @author Dioxide.CN
 * @date 2023/4/4 23:31
 * @since 1.0
 */
class RSAHandler {

    /**
     * 生成公钥和私钥对
     * @throws java.security.NoSuchAlgorithmException RSA算法不存在
     */
    static Pair<String, String> genKeyPair() {
        // KeyPairGenerator 类用于生成公钥和私钥对，基于RSA算法生成对象
        def keyPairGen = KeyPairGenerator.getInstance("RSA")
        // 初始化密钥对生成器，密钥大小为96-255位
        keyPairGen.initialize(1024, new SecureRandom())
        // 生成一个密钥对，保存在keyPair中
        KeyPair keyPair = keyPairGen.generateKeyPair()

        def privateKey = (RSAPrivateKey) keyPair.getPrivate()
        def publicKey = (RSAPublicKey) keyPair.getPublic()

        String publicKeyString = new String(Base64.encodeBase64(publicKey.getEncoded()))
        String privateKeyString = new String(Base64.encodeBase64((privateKey.getEncoded())))

        // 将公钥和私钥按照左公钥右私钥的方式进行缓存
        return Pair.of(publicKeyString, privateKeyString)
    }

    /**
     * 使用RSA公钥对信息进行加密
     */
    static String encrypt(String str, String publicKey) {
        // base64编码的公钥
        byte[] decoded = Base64.decodeBase64(publicKey)
        def pubKey = (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded))

        // RSA加密
        def cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.ENCRYPT_MODE, pubKey)

        return Base64.encodeBase64String(cipher.doFinal(str.getBytes(StandardCharsets.UTF_8)))
    }

    /**
     * 使用RSA私钥对加密信息解密，一把公钥对应一把秘钥，公钥加密后的密文只能由对应的秘钥来解密
     * @param str 密文
     * @param privateKey RSA私钥
     * @return 明文
     * @throws Exception 解密过程中的异常信息
     */
    static String decrypt(String str, String privateKey) {
        //64位解码加密后的字符串
        byte[] inputByte = Base64.decodeBase64(str.getBytes(StandardCharsets.UTF_8))
        //base64编码的私钥
        byte[] decoded = Base64.decodeBase64(privateKey)
        def priKey = (RSAPrivateKey) KeyFactory
                        .getInstance("RSA")
                        .generatePrivate(new PKCS8EncodedKeySpec(decoded))

        //RSA解密
        def cipher = Cipher.getInstance("RSA")
        cipher.init(Cipher.DECRYPT_MODE, priKey)

        return new String(cipher.doFinal(inputByte))
    }

    /**
     * 校验key是否符合RSA算法逻辑
     * @param key 公钥、私钥
     * @return true 符合逻辑 false 不符合逻辑
     */
    static boolean verify(String key) {
        try {
            byte[] keyBytes = (byte[]) Base64.decodeBase64(key.getBytes())
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes)
            KeyFactory keyFactory = KeyFactory.getInstance("RSA")
            keyFactory.generatePublic(keySpec)

            return true
        } catch (Exception ignored) {
            return false
        }
    }
    
}
