package com.microsoft.typespec.http.client.generator.core.implementation.shaded.google.googlejavaformat.java;

import com.microsoft.typespec.http.client.generator.core.implementation.shaded.google.common.collect.ImmutableList;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.google.common.collect.ImmutableRangeSet;
import java.util.Optional;
import com.microsoft.typespec.http.client.generator.core.implementation.shaded.javax.annotation.processing.Generated;

@Generated("com.microsoft.typespec.http.client.generator.core.implementation.shaded.google.auto.value.processor.AutoBuilderProcessor")
class AutoBuilder_CommandLineOptions_Builder implements CommandLineOptions.Builder {

  private ImmutableList.Builder<String> filesBuilder$;

  private ImmutableList<String> files;

  private Boolean inPlace;

  private ImmutableRangeSet<Integer> lines;

  private ImmutableList.Builder<Integer> offsetsBuilder$;

  private ImmutableList<Integer> offsets;

  private ImmutableList.Builder<Integer> lengthsBuilder$;

  private ImmutableList<Integer> lengths;

  private Boolean aosp;

  private Boolean version;

  private Boolean help;

  private Boolean stdin;

  private Boolean fixImportsOnly;

  private Boolean sortImports;

  private Boolean removeUnusedImports;

  private Boolean dryRun;

  private Boolean setExitIfChanged;

  private Optional<String> assumeFilename = Optional.empty();

  private Boolean reflowLongStrings;

  private Boolean formatJavadoc;

  AutoBuilder_CommandLineOptions_Builder() {
  }

  @Override
  public ImmutableList.Builder<String> filesBuilder() {
    if (filesBuilder$ == null) {
      filesBuilder$ = ImmutableList.builder();
    }
    return filesBuilder$;
  }

  @Override
  public CommandLineOptions.Builder inPlace(boolean inPlace) {
    this.inPlace = inPlace;
    return this;
  }

  @Override
  public CommandLineOptions.Builder lines(ImmutableRangeSet<Integer> lines) {
    if (lines == null) {
      throw new NullPointerException("Null lines");
    }
    this.lines = lines;
    return this;
  }

  @Override
  public ImmutableList.Builder<Integer> offsetsBuilder() {
    if (offsetsBuilder$ == null) {
      offsetsBuilder$ = ImmutableList.builder();
    }
    return offsetsBuilder$;
  }

  @Override
  public ImmutableList.Builder<Integer> lengthsBuilder() {
    if (lengthsBuilder$ == null) {
      lengthsBuilder$ = ImmutableList.builder();
    }
    return lengthsBuilder$;
  }

  @Override
  public CommandLineOptions.Builder aosp(boolean aosp) {
    this.aosp = aosp;
    return this;
  }

  @Override
  public CommandLineOptions.Builder version(boolean version) {
    this.version = version;
    return this;
  }

  @Override
  public CommandLineOptions.Builder help(boolean help) {
    this.help = help;
    return this;
  }

  @Override
  public CommandLineOptions.Builder stdin(boolean stdin) {
    this.stdin = stdin;
    return this;
  }

  @Override
  public CommandLineOptions.Builder fixImportsOnly(boolean fixImportsOnly) {
    this.fixImportsOnly = fixImportsOnly;
    return this;
  }

  @Override
  public CommandLineOptions.Builder sortImports(boolean sortImports) {
    this.sortImports = sortImports;
    return this;
  }

  @Override
  public CommandLineOptions.Builder removeUnusedImports(boolean removeUnusedImports) {
    this.removeUnusedImports = removeUnusedImports;
    return this;
  }

  @Override
  public CommandLineOptions.Builder dryRun(boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }

  @Override
  public CommandLineOptions.Builder setExitIfChanged(boolean setExitIfChanged) {
    this.setExitIfChanged = setExitIfChanged;
    return this;
  }

  @Override
  public CommandLineOptions.Builder assumeFilename(String assumeFilename) {
    this.assumeFilename = Optional.of(assumeFilename);
    return this;
  }

  @Override
  public CommandLineOptions.Builder reflowLongStrings(boolean reflowLongStrings) {
    this.reflowLongStrings = reflowLongStrings;
    return this;
  }

  @Override
  public CommandLineOptions.Builder formatJavadoc(boolean formatJavadoc) {
    this.formatJavadoc = formatJavadoc;
    return this;
  }

  @Override
  public CommandLineOptions build() {
    if (filesBuilder$ != null) {
      this.files = filesBuilder$.build();
    } else if (this.files == null) {
      this.files = ImmutableList.of();
    }
    if (offsetsBuilder$ != null) {
      this.offsets = offsetsBuilder$.build();
    } else if (this.offsets == null) {
      this.offsets = ImmutableList.of();
    }
    if (lengthsBuilder$ != null) {
      this.lengths = lengthsBuilder$.build();
    } else if (this.lengths == null) {
      this.lengths = ImmutableList.of();
    }
    if (this.inPlace == null
        || this.lines == null
        || this.aosp == null
        || this.version == null
        || this.help == null
        || this.stdin == null
        || this.fixImportsOnly == null
        || this.sortImports == null
        || this.removeUnusedImports == null
        || this.dryRun == null
        || this.setExitIfChanged == null
        || this.reflowLongStrings == null
        || this.formatJavadoc == null) {
      StringBuilder missing = new StringBuilder();
      if (this.inPlace == null) {
        missing.append(" inPlace");
      }
      if (this.lines == null) {
        missing.append(" lines");
      }
      if (this.aosp == null) {
        missing.append(" aosp");
      }
      if (this.version == null) {
        missing.append(" version");
      }
      if (this.help == null) {
        missing.append(" help");
      }
      if (this.stdin == null) {
        missing.append(" stdin");
      }
      if (this.fixImportsOnly == null) {
        missing.append(" fixImportsOnly");
      }
      if (this.sortImports == null) {
        missing.append(" sortImports");
      }
      if (this.removeUnusedImports == null) {
        missing.append(" removeUnusedImports");
      }
      if (this.dryRun == null) {
        missing.append(" dryRun");
      }
      if (this.setExitIfChanged == null) {
        missing.append(" setExitIfChanged");
      }
      if (this.reflowLongStrings == null) {
        missing.append(" reflowLongStrings");
      }
      if (this.formatJavadoc == null) {
        missing.append(" formatJavadoc");
      }
      throw new IllegalStateException("Missing required properties:" + missing);
    }
    return new CommandLineOptions(
        this.files,
        this.inPlace,
        this.lines,
        this.offsets,
        this.lengths,
        this.aosp,
        this.version,
        this.help,
        this.stdin,
        this.fixImportsOnly,
        this.sortImports,
        this.removeUnusedImports,
        this.dryRun,
        this.setExitIfChanged,
        this.assumeFilename,
        this.reflowLongStrings,
        this.formatJavadoc);
  }
}
