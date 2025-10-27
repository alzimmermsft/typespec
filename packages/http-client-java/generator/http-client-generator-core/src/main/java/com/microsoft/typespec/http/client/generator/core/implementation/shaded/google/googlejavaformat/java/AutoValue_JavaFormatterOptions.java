package com.microsoft.typespec.http.client.generator.core.implementation.shaded.google.googlejavaformat.java;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation.processing.Generated;

@Generated("com.microsoft.typespec.http.client.generator.core.implementation.shaded.google.auto.value.processor.AutoValueProcessor")
final class AutoValue_JavaFormatterOptions extends JavaFormatterOptions {

  private final boolean formatJavadoc;

  private final boolean reorderModifiers;

  private final Style style;

  private AutoValue_JavaFormatterOptions(
      boolean formatJavadoc,
      boolean reorderModifiers,
      Style style) {
    this.formatJavadoc = formatJavadoc;
    this.reorderModifiers = reorderModifiers;
    this.style = style;
  }

  @Override
  public boolean formatJavadoc() {
    return formatJavadoc;
  }

  @Override
  public boolean reorderModifiers() {
    return reorderModifiers;
  }

  @Override
  public Style style() {
    return style;
  }

  @Override
  public String toString() {
    return "JavaFormatterOptions{"
        + "formatJavadoc=" + formatJavadoc + ", "
        + "reorderModifiers=" + reorderModifiers + ", "
        + "style=" + style
        + "}";
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (o instanceof JavaFormatterOptions) {
      JavaFormatterOptions that = (JavaFormatterOptions) o;
      return this.formatJavadoc == that.formatJavadoc()
          && this.reorderModifiers == that.reorderModifiers()
          && this.style.equals(that.style());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int h$ = 1;
    h$ *= 1000003;
    h$ ^= formatJavadoc ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= reorderModifiers ? 1231 : 1237;
    h$ *= 1000003;
    h$ ^= style.hashCode();
    return h$;
  }

  static final class Builder extends JavaFormatterOptions.Builder {
    private Boolean formatJavadoc;
    private Boolean reorderModifiers;
    private Style style;
    Builder() {
    }
    @Override
    public JavaFormatterOptions.Builder formatJavadoc(boolean formatJavadoc) {
      this.formatJavadoc = formatJavadoc;
      return this;
    }
    @Override
    public JavaFormatterOptions.Builder reorderModifiers(boolean reorderModifiers) {
      this.reorderModifiers = reorderModifiers;
      return this;
    }
    @Override
    public JavaFormatterOptions.Builder style(Style style) {
      if (style == null) {
        throw new NullPointerException("Null style");
      }
      this.style = style;
      return this;
    }
    @Override
    public JavaFormatterOptions build() {
      if (this.formatJavadoc == null
          || this.reorderModifiers == null
          || this.style == null) {
        StringBuilder missing = new StringBuilder();
        if (this.formatJavadoc == null) {
          missing.append(" formatJavadoc");
        }
        if (this.reorderModifiers == null) {
          missing.append(" reorderModifiers");
        }
        if (this.style == null) {
          missing.append(" style");
        }
        throw new IllegalStateException("Missing required properties:" + missing);
      }
      return new AutoValue_JavaFormatterOptions(
          this.formatJavadoc,
          this.reorderModifiers,
          this.style);
    }
  }

}
