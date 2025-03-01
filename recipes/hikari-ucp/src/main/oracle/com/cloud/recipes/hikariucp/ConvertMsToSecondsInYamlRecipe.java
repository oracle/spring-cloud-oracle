// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package oracle.com.cloud.recipes.hikariucp;

import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.yaml.YamlVisitor;
import org.openrewrite.yaml.tree.Yaml;

import java.util.regex.Pattern;

public class ConvertMsToSecondsInYamlRecipe extends Recipe {

  private final String pathRegex;

  public ConvertMsToSecondsInYamlRecipe(String pathRegex) {
    this.pathRegex = pathRegex;
  }

  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor() {

    Pattern pattern = Pattern.compile(pathRegex);

    return new YamlVisitor<ExecutionContext>() {
      private String currentPath = "";

      @Override
      public Yaml visitMappingEntry(Yaml.Mapping.Entry entry, ExecutionContext ctx) {
        String previousPath = currentPath;
        currentPath += (currentPath.isEmpty() ? "" : ".") + entry.getKey().getValue();
        Yaml.Mapping.Entry updatedEntry = (Yaml.Mapping.Entry) super.visitMappingEntry(entry, ctx);
        currentPath = previousPath;
        return updatedEntry;
      }

      @Override
      public Yaml visitScalar(Yaml.Scalar scalar, ExecutionContext ctx) {
        if (pattern.matcher(currentPath).matches()) {
          String value = scalar.getValue().trim();
          try {
            long ms = Long.parseLong(value);
            double seconds = ms / 1000.0;
            String newValue = String.valueOf(seconds);
            return scalar.withValue(newValue);
          } catch (NumberFormatException e) {
            // If the value isn't a valid number, ignore it.
          }
        }
        return super.visitScalar(scalar, ctx);
      }
    };

  }

  @Override
  public @NlsRewrite.DisplayName String getDisplayName() {
    return "";
  }

  @Override
  public @NlsRewrite.Description String getDescription() {
    return "";
  }
}
