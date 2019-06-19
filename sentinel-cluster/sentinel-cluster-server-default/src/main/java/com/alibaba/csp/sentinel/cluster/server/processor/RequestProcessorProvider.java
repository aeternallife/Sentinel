/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.cluster.server.processor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.cluster.annotation.RequestType;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.SpiLoader;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public final class RequestProcessorProvider {

    private static final Map<Integer, RequestProcessor> PROCESSOR_MAP = new HashMap<>();

    static {
        loadAndInit();
    }

    private static void loadAndInit() {
        List<RequestProcessor> processors = SpiLoader.loadInstanceList(RequestProcessor.class);
        for (RequestProcessor processor : processors) {
            Integer type = parseRequestType(processor);
            if (type != null) {
                PROCESSOR_MAP.put(type, processor);
            } else {
                RecordLog.warn("[RequestProcessorProvider] Request type not provided, "
                    + "ignoring the processor: {0}", processor.getClass().getCanonicalName());
            }
        }
    }

    private static Integer parseRequestType(RequestProcessor processor) {
        RequestType requestType = processor.getClass().getAnnotation(RequestType.class);
        if (requestType != null) {
            return requestType.value();
        } else {
            return null;
        }
    }

    public static RequestProcessor getProcessor(int type) {
        return PROCESSOR_MAP.get(type);
    }

    /**
     * Note: not thread-safe, only for unit test.
     *
     * @param type      processor type
     * @param processor valid processor
     */
    static void addProcessorIfAbsent(int type, RequestProcessor processor) {
        // TBD: use putIfAbsent in JDK 1.8.
        if (PROCESSOR_MAP.containsKey(type)) {
            return;
        }
        PROCESSOR_MAP.put(type, processor);
    }

    /**
     * Note: not thread-safe, only for unit test.
     *
     * @param type      processor type
     * @param processor valid processor
     */
    static void addProcessor(int type, RequestProcessor processor) {
        AssertUtil.notNull(processor, "processor cannot be null");
        PROCESSOR_MAP.put(type, processor);
    }

    private RequestProcessorProvider() {}
}
