/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.beam.runners.fnexecution.control;

import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import org.apache.beam.runners.core.StateNamespace;
import org.apache.beam.runners.core.StateNamespaces;
import org.apache.beam.runners.core.TimerInternals;
import org.apache.beam.runners.core.TimerInternals.TimerData;
import org.apache.beam.runners.core.construction.Timer;
import org.apache.beam.runners.fnexecution.control.ProcessBundleDescriptors.TimerSpec;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.fn.data.FnDataReceiver;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory that passes timers to {@link TimerReceiverFactory#timerDataConsumer}.
 *
 * <p>The constructed timers use the transform id as the timer id and the timer family id as the
 * timer family id.
 */
public class TimerReceiverFactory {
  private static final Logger LOG = LoggerFactory.getLogger(TimerReceiverFactory.class);

  /** (PTransform, Timer Local Name) => TimerReference. */
  private final HashMap<KV<String, String>, TimerSpec> transformAndTimerIdToSpecMap;

  private final BiConsumer<Timer<?>, TimerData> timerDataConsumer;
  private final Coder windowCoder;

  public TimerReceiverFactory(
      StageBundleFactory stageBundleFactory,
      BiConsumer<Timer<?>, TimerInternals.TimerData> timerDataConsumer,
      Coder windowCoder) {
    this.transformAndTimerIdToSpecMap = new HashMap<>();
    // Create a lookup map using the transform and timerId as the key.
    for (Map<String, ProcessBundleDescriptors.TimerSpec> transformTimerMap :
        stageBundleFactory.getProcessBundleDescriptor().getTimerSpecs().values()) {
      for (ProcessBundleDescriptors.TimerSpec timerSpec : transformTimerMap.values()) {
        transformAndTimerIdToSpecMap.put(
            KV.of(timerSpec.transformId(), timerSpec.timerId()), timerSpec);
      }
    }
    this.timerDataConsumer = timerDataConsumer;
    this.windowCoder = windowCoder;
  }

  // @Override
  public <K> FnDataReceiver<Timer<K>> create(String transformId, String timerFamilyId) {
    final ProcessBundleDescriptors.TimerSpec timerSpec =
        transformAndTimerIdToSpecMap.get(KV.of(transformId, timerFamilyId));

    return receivedElement -> {
      Timer timer =
          checkNotNull(
              receivedElement, "Received null Timer from SDK harness: %s", receivedElement);
      LOG.debug("Timer received: {}", timer);
      for (Object window : timer.getWindows()) {
        StateNamespace namespace = StateNamespaces.window(windowCoder, (BoundedWindow) window);
        TimerInternals.TimerData timerData =
            TimerInternals.TimerData.of(
                timerSpec.transformId(),
                timerSpec.timerId(),
                namespace,
                timer.getFireTimestamp(),
                timer.getHoldTimestamp(),
                timerSpec.getTimerSpec().getTimeDomain());
        timerDataConsumer.accept(timer, timerData);
      }
    };
  }
}
