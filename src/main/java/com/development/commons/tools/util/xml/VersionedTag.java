package com.development.commons.tools.util.xml;

import java.io.Serializable;

/**
 * It is for form elementent xml tag which has msgKey and version.
 *
 * @author keying
 *
 */
public class VersionedTag implements Serializable {
  private static final long serialVersionUID = 1L;
  private String msgKey;
  private Long ver;

  public VersionedTag(final String msgKey, final Long ver) {
    this.msgKey = msgKey;
    this.ver = ver;
  }

  public String getMsgKey() {
    return msgKey;
  }

  public void setMsgKey(final String msgKey) {
    this.msgKey = msgKey;
  }

  public Long getVer() {
    return ver;
  }

  public void setVer(final Long ver) {
    this.ver = ver;
  }
}
