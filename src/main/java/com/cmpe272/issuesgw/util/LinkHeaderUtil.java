package com.cmpe272.issuesgw.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal Link header parser for GitHub-style pagination.
 * Examples:
 *   <https://api.github.com/resource?page=2>; rel="next",
 *   <https://api.github.com/resource?page=5>; rel="last"
 */
public final class LinkHeaderUtil {
  private LinkHeaderUtil() {}

  public static Map<String, String> parseLinkHeader(String header) {
    Map<String, String> out = new HashMap<>();
    if (header == null) return out;
    String h = header.trim();
    if (h.isEmpty()) return out;

    // Split by top-level commas between links
    String[] parts = h.split(",\\s*");
    for (String part : parts) {
      String p = part.trim();

      // URL must be enclosed in <...>
      int lt = p.indexOf('<');
      int gt = p.indexOf('>');
      if (lt < 0 || gt < 0 || gt <= lt + 1) continue;
      String url = p.substring(lt + 1, gt).trim();

      // Find rel="name"
      int relIdx = p.indexOf("rel=");
      if (relIdx < 0) continue;
      // allow rel=name or rel="name"
      int q1 = p.indexOf('"', relIdx);
      int q2 = (q1 >= 0) ? p.indexOf('"', q1 + 1) : -1;
      String rel;
      if (q1 >= 0 && q2 > q1) {
        rel = p.substring(q1 + 1, q2).trim();
      } else {
        // fallback: rel=next
        int start = relIdx + 4;
        int end = p.indexOf(';', start);
        if (end < 0) end = p.length();
        rel = p.substring(start, end).trim();
      }
      if (!rel.isEmpty()) {
        out.put(rel, url);
      }
    }
    return out;
  }
}
