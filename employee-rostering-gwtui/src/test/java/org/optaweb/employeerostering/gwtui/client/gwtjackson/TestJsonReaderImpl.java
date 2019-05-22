/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.optaweb.employeerostering.gwtui.client.gwtjackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.github.nmorel.gwtjackson.client.stream.JsonReader;
import com.github.nmorel.gwtjackson.client.stream.JsonToken;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * GWT Jackson cannot compile in tests, so we delegate to Jackson.
 * <p>
 * Note: gwt-jackson uses nextToken, which Jackson does not have,
 * thus we use the convention that the current token in the json parser
 * is the "next token" (i.e. if the current token in the parser is 10,
 * then nextInt() should return 10)
 */
public class TestJsonReaderImpl implements JsonReader {

    private String input;
    private JsonParser jsonParser;

    public TestJsonReaderImpl(String json) {
        input = json;
        try {
            jsonParser = new JsonFactory().createParser(json);
            nextToken();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void setLenient(boolean lenient) {
        // Do nothing
    }

    @Override
    public void beginArray() {
        nextToken();
    }

    @Override
    public void endArray() {
        nextToken();
    }

    @Override
    public void beginObject() {
        nextToken();
    }

    @Override
    public void endObject() {
        nextToken();
    }

    @Override
    public boolean hasNext() {
        if (!jsonParser.hasCurrentToken()) {
            return false;
        }
        switch (jsonParser.getCurrentToken()) {
            case END_ARRAY:
            case END_OBJECT:
                return false;
            default:
                return true;
        }
    }

    @Override
    public JsonToken peek() {
        switch (jsonParser.getCurrentToken()) {
            case END_ARRAY:
                return JsonToken.END_ARRAY;
            case END_OBJECT:
                return JsonToken.END_OBJECT;
            case FIELD_NAME:
                return JsonToken.NAME;
            case NOT_AVAILABLE:
                return null;
            case START_ARRAY:
                return JsonToken.BEGIN_ARRAY;
            case START_OBJECT:
                return JsonToken.BEGIN_OBJECT;
            case VALUE_EMBEDDED_OBJECT:
                return null;
            case VALUE_FALSE:
                return JsonToken.BOOLEAN;
            case VALUE_NULL:
                return JsonToken.NULL;
            case VALUE_NUMBER_FLOAT:
                return JsonToken.NUMBER;
            case VALUE_NUMBER_INT:
                return JsonToken.NUMBER;
            case VALUE_STRING:
                return JsonToken.STRING;
            case VALUE_TRUE:
                return JsonToken.BOOLEAN;
            default:
                return null;
        }
    }

    @Override
    public String nextName() {
        try {
            String out = jsonParser.currentName();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public String nextString() {
        try {
            String out = jsonParser.getValueAsString();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean nextBoolean() {
        try {
            boolean out = jsonParser.getBooleanValue();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void nextNull() {
        nextToken();
    }

    @Override
    public double nextDouble() {
        try {
            double out = jsonParser.getDoubleValue();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public long nextLong() {
        try {
            long out = jsonParser.getValueAsLong();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int nextInt() {
        try {
            int out = jsonParser.getValueAsInt();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() {
        try {
            jsonParser.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void skipValue() {
        nextToken();
    }

    @Override
    public String nextValue() {
        try {
            String out = jsonParser.getText();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int getLineNumber() {
        return jsonParser.getCurrentLocation().getLineNr();
    }

    @Override
    public int getColumnNumber() {
        return jsonParser.getCurrentLocation().getColumnNr();
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public Number nextNumber() {
        try {
            Number out = jsonParser.getNumberValue();
            nextToken();
            return out;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public JavaScriptObject nextJavaScriptObject(boolean useSafeEval) {
        // Cannot implement this
        return null;
    }

    private void nextToken() {
        try {
            jsonParser.nextToken();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
