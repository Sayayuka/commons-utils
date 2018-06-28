/*
 * $Id$
 *
 * Copyright (C) 2006 SuccessFactors, Inc.
 * All Rights Reserved
 */
package com.development.commons.tools.util.xml;

/**
 * @author kbittner
 *
 */
public interface XmlTemplateConstants {
  // used by toXML methods
  public static final String indentString = "  ";
  public static final String DTD_DIRECTORY = "com/sf/dtd/";

  public static final String CDATABEGIN = "<![CDATA[";
  public static final String CDATAEND = "]]>";

  //
  // These static strings define all the XML element names.
  //

  public static final String xmlCountry = "country";
  public static final String xmlTemplateId = "template-id";
  public static final String xmlTemplateName = "template-name";
  public static final String xmlTemplateDesc = "template-desc";
  public static final String xmlTemplateLastModified = "template-lastmodified";
  public static final String xmlFieldDefinition = "field-definition";
  public static final String xmlFieldLabel = "field-label";
  public static final String xmlFieldDesc = "field-description";
  public static final String xmlPicklistId = "picklist-id";
  public static final String xmlParentFieldId = "parent-field-id";
  public static final String xmlBGPicklistId = "picklist";
  public static final String xmlEnumValue = "enum-value";
  public static final String xmlEnumLabel = "enum-label";
  public static final String xmlDefaultValue = "default-value";
  public static final String xmlFieldFormat = "field-format";
  public static final String xmlDescription = "description";
  public static final String xmlRoleName = "role-name";
  public static final String xmlPermissionStatus = "status";
  public static final String xmlField = "field";
  public static final String xmlFieldPermission = "field-permission";
  public static final String xmlButtonPermission = "button-permission";
  public static final String xmlFieldRequired = "required";
  public static final String xmlFieldPublic = "public";
  public static final String xmlFieldCustom = "custom";
  public static final String xmlFieldReadOnly = "readOnly";
  public static final String xmlFieldAnonymize = "anonymize";
  public static final String xmlFieldWidth = "width";
  public static final String xmlFieldUrlLink = "url-link";
  public static final String xmlGroupName = "group-name";
  public static final String xmlFieldId = "id";
  public static final String xmlFieldType = "type";
  public static final String xmlButtonId = "button-id";
  public static final String attrMimeType = "mime-type";
  public static final String MIMETYPE_TEXTHTML = "text-html"; /*
                                                               * description
                                                               * xml:
                                                               * lang="fr-FR"
                                                               * mime
                                                               * -type="text/html"
                                                               * ><![CDATA[<a
                                                               * href
                                                               * ="...">...</
                                                               * a>]]
                                                               * ></description>
                                                               */
  public static final String MIMETYPE_TEXTPLAIN = "text-plain";
  public static final String xmlMultiselect = "multiselect"; /*
                                                              * multiselect
                                                              * picklists
                                                              */
  public static final String xmlFieldForwardIntact = "forward-intact";

  // Currenttly used in offer detail template
  public static final String xmlTemplateType = "template-type";
  public static final String xmlFieldObjectType = "object-type";

}
