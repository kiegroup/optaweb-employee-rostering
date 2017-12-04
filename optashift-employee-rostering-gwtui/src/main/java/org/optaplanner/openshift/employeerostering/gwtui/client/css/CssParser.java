package org.optaplanner.openshift.employeerostering.gwtui.client.css;

import com.google.gwt.resources.client.CssResource;

public class CssParser {
    /**
     * Get the String value of a CSS property
     * @param cssResource CssResource to search (ex: CssResources.popup())
     * @param className Name of class in the CssResource (ex: CssResource.popup().panel())
     * @param classProperty Name of property to extract from the class (ex: "width")
     * @return String value of property if it exists, null otherwise
     */
    public static String getCssProperty(CssResource cssResource, String className, String classProperty) {
        return getCssProperty(cssResource.getText(), "." + className, classProperty);
    }
    
    private static native String getCssProperty(String styleContent, String className, String classProperty) /*-{
        var theDoc = document.implementation.createHTMLDocument("");
        var styleElement = document.createElement("style");
        
        styleElement.textContent = styleContent;
        theDoc.body.appendChild(styleElement);
        var rules = styleElement.sheet.cssRules;
        var out = null;
        
        for (var j = rules.length - 1; j >= 0; j--) {
            if (rules[j].selectorText) {
                var styles = rules[j].selectorText.split(", ");
                if (styles.indexOf(className) != -1 && rules[j].style[classProperty] !== undefined) {
                    out =  rules[j].style[classProperty];
                    break;
                }
            }
        }
        
        theDoc.body.removeChild(styleElement);
        return out;
    }-*/; 
}
