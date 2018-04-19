package org.optaplanner.openshift.employeerostering.gwtui.client.viewport;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.enterprise.context.ApplicationScoped;

import elemental2.dom.HTMLElement;
import elemental2.dom.HTMLStyleElement;

@ApplicationScoped
public class CSSGlobalStyle {

    private HTMLStyleElement styleElement;
    private String className;
    private Map<GridVariables, Number> gridVariableValueMap;

    public void setRootElement(String className, HTMLElement root) {
        gridVariableValueMap = new HashMap<>();
        this.className = className;
        styleElement = createStyleSheet(className, root);
    }

    public void setGridVariable(GridVariables variable, Number value) {
        setCSSVariable(variable.getCssPropertyName(), variable.getCssPropertyValueOf(value));
        gridVariableValueMap.put(variable, value);
    }

    public void unsetGridVariable(GridVariables variable) {
        unsetCSSVariable(variable.getCssPropertyName());
        gridVariableValueMap.remove(variable);
    }

    public void unsetCSSVariable(String variableName) {
        String[] properties = styleElement.innerHTML.substring(styleElement.innerHTML.indexOf('{') + 1,
                styleElement.innerHTML.lastIndexOf('}')).split(";");

        StringBuilder newStyle = new StringBuilder(className).append(" {");

        for (String property : properties) {
            if (property.isEmpty()) {
                continue;
            }
            String name = property.substring(property.indexOf('-'), property.indexOf(':'));
            if (!name.equals(variableName)) {
                newStyle.append(property).append(";");
            }
        }
        newStyle.append("}");
        styleElement.innerHTML = newStyle.toString();
    }

    private void setCSSVariable(String variableName, String value) {
        String[] properties = styleElement.innerHTML.substring(styleElement.innerHTML.indexOf('{') + 1,
                styleElement.innerHTML.lastIndexOf('}')).split(";");

        StringBuilder newStyle = new StringBuilder(className).append(" {");
        boolean foundProperty = false;

        for (String property : properties) {
            if (property.isEmpty()) {
                continue;
            }
            String name = property.substring(property.indexOf('-'), property.indexOf(':'));
            if (name.equals(variableName)) {
                newStyle.append(name)
                        .append(":")
                        .append(value).append(";");
                foundProperty = true;
            } else {
                newStyle.append(property).append(";");
            }
        }

        if (!foundProperty) {
            newStyle.append(variableName)
                    .append(":")
                    .append(value);
        }
        newStyle.append("}");
        styleElement.innerHTML = newStyle.toString();
    }

    public Number getGridVariableValue(GridVariables variable) {
        return gridVariableValueMap.getOrDefault(variable, 0);
    }

    private static native HTMLStyleElement createStyleSheet(String className, HTMLElement root) /*-{
           var style = document.createElement('style');
           style.type = 'text/css';
           style.innerHTML = className + ' {}';
           root.appendChild(style);
           return style;
    }-*/;
    
    public static enum GridVariables {
        GRID_UNIT_SIZE("--grid-unit-size", p -> p + "px"),
        GRID_SOFT_LINE_INTERVAL("--grid-soft-line-interval", 
                p -> "calc(" + GRID_UNIT_SIZE.get() + "*" + p.doubleValue() + ")"),
        GRID_HARD_LINE_INTERVAL("--grid-hard-line-interval", 
                p -> "calc(" + GRID_UNIT_SIZE.get() + "*" + p.doubleValue() + ")"),
        GRID_HEADER_COLUMN_WIDTH("--grid-header-column-width", p -> p + "px");
        
        private String cssPropertyName;
        private Function<Number,String> valueMapper;
        
        private GridVariables(String cssPropertyName, Function<Number,String> valueMapper) {
            this.cssPropertyName = cssPropertyName;
            this.valueMapper = valueMapper;
        }
        
        public String get() {
            return "var(" + cssPropertyName + ")";
        }
        
        public String getCssPropertyName() {
            return cssPropertyName;
        }
        
        public String getCssPropertyValueOf(Number value) {
            return valueMapper.apply(value);
        }
    }
}
