package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.TagName;

// TODO: Find out why gwt-bootscrape3 Slider get a jQuery error on attach
@TagName("input")
public class RangeSlider extends InputElement {
	public static RangeSlider create() {
		Element e = Document.get().createElement("input");
		e.setAttribute("type", "range");
		return (RangeSlider) e;
	}

	protected RangeSlider() {
	}

	public final String getMin() {
		return (!this.getAttribute("min").isEmpty()) ? this.getAttribute("min") : "0";
	}

	public final void setMin(String min) {
		this.setAttribute("min", min);
	}

	public final String getMax() {
		return (!this.getAttribute("max").isEmpty()) ? this.getAttribute("max") : "1";
	}

	public final void setMax(String max) {
		this.setAttribute("max", max);
	}

	public final String getStep() {
		return (!this.getAttribute("step").isEmpty()) ? this.getAttribute("step") : "1";
	}

	public final void setStep(String step) {
		this.setAttribute("step", step);
	}
}
