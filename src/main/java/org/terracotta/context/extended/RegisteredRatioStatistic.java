/*
 * Copyright Terracotta, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terracotta.context.extended;

import org.terracotta.statistics.extended.CompoundOperation;
import org.terracotta.statistics.extended.SampledStatistic;

import java.util.EnumSet;

/**
 * @author Ludovic Orban
 */
public class RegisteredRatioStatistic<T extends Enum<T>> extends RegisteredCompoundOperationStatistic<T> {
  private final EnumSet<T> numerator;
  private final EnumSet<T> denominator;

  public RegisteredRatioStatistic(CompoundOperation<T> compoundOperation, EnumSet<T> numerator, EnumSet<T> denominator) {
    super(compoundOperation);
    this.numerator = numerator;
    this.denominator = denominator;
  }

  @Override
  public RegistrationType getType() {
    return RegistrationType.RATIO;
  }

  public EnumSet<T> getNumerator() {
    return numerator;
  }

  public EnumSet<T> getDenominator() {
    return denominator;
  }

  public SampledStatistic<Double> getSampledStatistic() {
    CompoundOperation<T> operation = getCompoundOperation();
    return operation.ratioOf(getNumerator(), getDenominator());
  }
}
