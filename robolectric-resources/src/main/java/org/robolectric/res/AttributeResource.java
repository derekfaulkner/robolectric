package org.robolectric.res;

import javax.annotation.Nonnull;

public class AttributeResource {
  public static final String ANDROID_RES_NS_PREFIX = "http://schemas.android.com/apk/res/";
  public static final String RES_AUTO_NS_URI = "http://schemas.android.com/apk/res-auto";

  public static final String NULL_VALUE = "@null";
  public static final String EMPTY_VALUE = "@empty";

  public final @Nonnull ResName resName;
  public final @Nonnull String value;
  public final @Nonnull String contextPackageName;
  private final Integer referenceResId;

  public AttributeResource(@Nonnull ResName resName, @Nonnull String value, @Nonnull String contextPackageName) {
    this(resName, value, contextPackageName, null);
  }

  public AttributeResource(@Nonnull ResName resName, @Nonnull String value, @Nonnull String contextPackageName, Integer referenceResId) {
    this.referenceResId = referenceResId;
    if (!resName.type.equals("attr")) throw new IllegalStateException("\"" + resName.getFullyQualifiedName() + "\" unexpected");

    this.resName = resName;
    this.value = value;
    this.contextPackageName = contextPackageName;
  }

  public boolean isResourceReference() {
    return isResourceReference(value);
  }

  public @Nonnull ResName getResourceReference() {
    if (!isResourceReference()) throw new RuntimeException("not a resource reference: " + this);
    return ResName.qualifyResName(value.substring(1).replace("+", ""), contextPackageName, "style");
  }

  public boolean isStyleReference() {
    return isStyleReference(value);
  }

  public ResName getStyleReference() {
    if (!isStyleReference()) throw new RuntimeException("not a style reference: " + this);
    return ResName.qualifyResName(value.substring(1), contextPackageName, "attr");
  }

  public boolean isNull() {
    return NULL_VALUE.equals(value);
  }

  public boolean isEmpty() {
    return EMPTY_VALUE.equals(value);
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "name='" + resName + '\'' +
        ", value='" + value + '\'' +
        ", contextPackageName='" + contextPackageName + '\'' +
        '}';
  }

  public static boolean isResourceReference(String value) {
    return value.startsWith("@") && !isNull(value);
  }

  public static @Nonnull ResName getResourceReference(String value, String defPackage, String defType) {
    if (!isResourceReference(value)) throw new IllegalArgumentException("not a resource reference: " + value);
    return ResName.qualifyResName(value.substring(1).replace("+", ""), defPackage, defType);
  }

  public static boolean isStyleReference(String value) {
    return value.startsWith("?");
  }

  public static ResName getStyleReference(String value, String defPackage, String defType) {
    if (!isStyleReference(value)) throw new IllegalArgumentException("not a style reference: " + value);
    return ResName.qualifyResName(value.substring(1), defPackage, defType);
  }

  public static boolean isNull(String value) {
    return NULL_VALUE.equals(value);
  }

  public static boolean isEmpty(String value) {
    return EMPTY_VALUE.equals(value);
  }

  public Integer getReferenceResId() {
    return referenceResId;
  }
}
