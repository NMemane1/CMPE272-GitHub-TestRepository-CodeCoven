package com.cmpe272.issuesgw.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;

class LinkHeaderUtilTest {

  @Test
  void parsesSingleRel() {
    String h = "<https://api.github.com/resource?page=2>; rel=\"next\"";
    Map<String,String> out = LinkHeaderUtil.parseLinkHeader(h);
    assertEquals("https://api.github.com/resource?page=2", out.get("next"));
  }

  @Test
  void parsesMultipleRels() {
    String h = "<https://api.github.com/resource?page=2>; rel=\"next\", " +
               "<https://api.github.com/resource?page=5>; rel=\"last\", " +
               "<https://api.github.com/resource?page=1>; rel=\"prev\"";
    Map<String,String> out = LinkHeaderUtil.parseLinkHeader(h);
    assertEquals(3, out.size());
    assertTrue(out.containsKey("next"));
    assertTrue(out.containsKey("last"));
    assertTrue(out.containsKey("prev"));
  }

  @Test
  void emptyOrNullYieldsEmptyMap() {
    assertTrue(LinkHeaderUtil.parseLinkHeader(null).isEmpty());
    assertTrue(LinkHeaderUtil.parseLinkHeader("").isEmpty());
    assertTrue(LinkHeaderUtil.parseLinkHeader("   ").isEmpty());
  }

  @Test
  void ignoresMalformedParts() {
    // no rel=, or angle brackets missing → should not blow up
    String h = "https://api.github.com/resource?page=2; rel=\"next\", " +
               "<https://api.github.com/resource?page=3>; rel=, " +
               "<https://api.github.com/resource?page=4>; rel=\"last\"";
    Map<String,String> out = LinkHeaderUtil.parseLinkHeader(h);
    // Still capture 'last', anything malformed is ignored
    assertEquals("https://api.github.com/resource?page=4", out.get("last"));
  }
}
