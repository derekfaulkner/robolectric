package org.robolectric.annotation.processing.generator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import org.robolectric.annotation.processing.RobolectricModel;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class JavadocJsonGenerator extends Generator {

  public static final Pattern START_OR_NEWLINE_SPACE = Pattern.compile("(^|\n) ");
  private final RobolectricModel model;
  private final Messager messager;

  public JavadocJsonGenerator(RobolectricModel model, ProcessingEnvironment environment) {
    super();

    this.model = model;
    this.messager = environment.getMessager();
  }

  @Override
  public void generate() {
    Map<String, String> shadowedTypes = new HashMap<>();
    for (Map.Entry<TypeElement, TypeElement> entry : model.getShadowOfMap().entrySet()) {
      String shadowType = entry.getKey().getQualifiedName().toString();
      String shadowedType = entry.getValue().getQualifiedName().toString();
      shadowedTypes.put(shadowType, shadowedType);
    }

    for (Map.Entry<String, String> entry : model.getExtraShadowTypes().entrySet()) {
      String shadowType = entry.getKey();
      String shadowedType = entry.getValue();
      shadowedTypes.put(shadowType, shadowedType);
    }

    File docs = new File("robolectric-shadows/shadows-core/build/json-docs");

    for (RobolectricModel.DocumentedPackage documentedPackage : model.getDocumentedPackages()) {
      JsonObject packageJsonObj = new JsonObject();
      packageJsonObj.addProperty("doc", documentedPackage.documentation);
//      writeJson(packageJsonObj, new File(docs, documentedPackage.getName() + ".json"));

      for (RobolectricModel.DocumentedType documentedType : documentedPackage.getDocumentedTypes()) {
        String shadowedType = shadowedTypes.get(documentedType.getName());
//        System.out.println("For " + shadowedType + " methods are " + documentedType.getDocumentedMethods());

        JsonObject typeJsonObj = new JsonObject();
        typeJsonObj.addProperty("shadowClass", documentedType.getName());
        putDoc(typeJsonObj, documentedType.documentation);

        JsonObject methodsJsonObj = new JsonObject();
        for (RobolectricModel.DocumentedMethod documentedMethod : documentedType.getDocumentedMethods()) {
          JsonObject methodJsonObj = new JsonObject();
          methodsJsonObj.add(documentedMethod.getName(), methodJsonObj);
          putDoc(methodJsonObj, documentedMethod.documentation);
        }
        typeJsonObj.add("methods", methodsJsonObj);

        writeJson(typeJsonObj, new File(docs, shadowedType + ".json"));
      }
    }
  }

  private void putDoc(JsonObject typeJsonObj, String docStr) {
    if (docStr != null && !docStr.isEmpty()) {
      String formattedDocStr = START_OR_NEWLINE_SPACE.matcher(docStr).replaceAll("$1");
      typeJsonObj.addProperty("doc", formattedDocStr);
    }
  }

  private void writeJson(JsonObject json, File file) {
    file.getParentFile().mkdirs();

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
      JsonWriter jsonWriter = new JsonWriter(writer);
      jsonWriter.setIndent("  ");
      new Gson().toJson(json, jsonWriter);
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write javadoc JSON file: " + e);
      throw new RuntimeException(e);
    }
  }

}
