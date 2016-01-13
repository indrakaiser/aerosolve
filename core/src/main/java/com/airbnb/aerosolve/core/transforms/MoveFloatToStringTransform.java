package com.airbnb.aerosolve.core.transforms;

import com.airbnb.aerosolve.core.FeatureVector;
import com.airbnb.aerosolve.core.util.Util;
import com.typesafe.config.Config;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.List;

/**
 * Moves named fields from one family to another.
 */
public class MoveFloatToStringTransform extends Transform {
  private String fieldName1;
  private double bucket;
  private String outputName;
  private List<String> keys;
  private double cap;

  @Override
  public void configure(Config config, String key) {
    fieldName1 = config.getString(key + ".field1");
    bucket = config.getDouble(key + ".bucket");
    outputName = config.getString(key + ".output");
    keys = config.getStringList(key + ".keys");
    try {
      cap = config.getDouble(key + ".cap");
    } catch (Exception e) {
      cap = 1e10;
    }
  }

  @Override
  public void doTransform(FeatureVector featureVector) {
    Map<String, Map<String, Double>> floatFeatures = featureVector.getFloatFeatures();

    if (floatFeatures == null) {
      return;
    }
    Map<String, Double> feature1 = floatFeatures.get(fieldName1);
    if (feature1 == null || feature1.isEmpty()) {
      return;
    }

    Util.optionallyCreateStringFeatures(featureVector);
    Map<String, Set<String>> stringFeatures = featureVector.getStringFeatures();
    Set<String> output = Util.getOrCreateStringFeature(outputName, stringFeatures);

    for (String key : keys) {
      if (feature1.containsKey(key)) {
        Double dbl = feature1.get(key);
        if (dbl > cap) {
          dbl = cap;
        }

        Double quantized = LinearLogQuantizeTransform.quantize(dbl, bucket);
        output.add(key + '=' + quantized);
        feature1.remove(key);
      }
    }
  }
}
