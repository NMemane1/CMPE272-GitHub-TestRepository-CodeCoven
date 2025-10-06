package com.cmpe272.issuesgw.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HmacVerifier {
  public static boolean isValid(String body, String signatureHeader, String secret){
    if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false;
    String provided = signatureHeader.substring("sha256=".length());
    String computed = hmacSha256Hex(secret, body);
    return constantTimeEquals(provided, computed);
  }
  static String hmacSha256Hex(String secret, String body){
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(), "HmacSHA256"));
      byte[] out = mac.doFinal(body.getBytes());
      StringBuilder sb = new StringBuilder(out.length*2);
      for (byte b: out) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e){ throw new RuntimeException(e); }
  }
  static boolean constantTimeEquals(String a, String b){
    if (a==null || b==null) return false;
    if (a.length()!=b.length()) return false;
    int r=0;
    for (int i=0;i<a.length();i++) r |= a.charAt(i) ^ b.charAt(i);
    return r==0;
  }
}
