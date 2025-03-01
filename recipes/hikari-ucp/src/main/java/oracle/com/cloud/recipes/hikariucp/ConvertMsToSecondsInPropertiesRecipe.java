// Copyright (c) 2025, Oracle and/or its affiliates.
// Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

package oracle.com.cloud.recipes.hikariucp;

import org.openrewrite.NlsRewrite;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.properties.PropertiesVisitor;
import org.openrewrite.properties.tree.Properties;

public class ConvertMsToSecondsInPropertiesRecipe extends Recipe {

  private final String keyRegex;

  // Takes a keyRegex parameter to specify which property keys to target (e.g., Hikari timeout properties).
  public ConvertMsToSecondsInPropertiesRecipe(String keyRegex) {
      this.keyRegex = keyRegex;
  }

  //  Extends PropertiesVisitor to process each Properties.Entry (key-value pair) in application.properties.
  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor() {

    return new PropertiesVisitor<ExecutionContext>() {

      @Override
      public Properties visitEntry(Properties.Entry entry, ExecutionContext ctx) {
        if (entry.getKey().matches(keyRegex)) {
          String value = entry.getValue().getText().trim();
          try {
            long ms = Long.parseLong(value);
            double seconds = ms / 1000.0;
            String newValue = String.valueOf(seconds);
            Properties.Value updatedValue = entry.getValue().withText(newValue);
            return entry.withValue(updatedValue);
          } catch (NumberFormatException e) {
            // If the value isn't a valid number, ignore it.
          }
        }
        return super.visitEntry(entry, ctx);
      }
      
    };
  }

  @Override
  public @NlsRewrite.DisplayName String getDisplayName() {
    return "Convert milliseconds to seconds for Hikari properties";
  }

  @Override
  public @NlsRewrite.Description String getDescription() {
    return "Transforms millisecond values to seconds for Hikari connection pool properties matching the given regex.";
  }
}
