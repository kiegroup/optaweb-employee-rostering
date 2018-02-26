package org.optaplanner.openshift.employeerostering.gwtui.client.employee;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import elemental2.promise.Promise;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.RejectCallbackFn;
import elemental2.promise.Promise.PromiseExecutorCallbackFn.ResolveCallbackFn;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.extras.tagsinput.client.ui.base.SingleValueTagsInput;
import org.gwtbootstrap3.extras.typeahead.client.base.CollectionDataset;
import org.gwtbootstrap3.extras.typeahead.client.base.Dataset;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.ui.client.local.api.elemental2.IsElement;
import org.jboss.errai.ui.client.local.spi.TranslationService;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.optaplanner.openshift.employeerostering.gwtui.client.calendar.twodayview.TwoDayView;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.CommonUtils;
import org.optaplanner.openshift.employeerostering.gwtui.client.common.FailureShownRestCallback;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.ErrorPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.popups.FormPopup;
import org.optaplanner.openshift.employeerostering.gwtui.client.util.PromiseUtils;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeAvailabilityState;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeSkillProficiency;
import org.optaplanner.openshift.employeerostering.shared.employee.view.EmployeeAvailabilityView;
import org.optaplanner.openshift.employeerostering.shared.shift.view.ShiftView;
import org.optaplanner.openshift.employeerostering.shared.skill.Skill;
import org.optaplanner.openshift.employeerostering.shared.spot.Spot;
import org.optaplanner.openshift.employeerostering.shared.employee.EmployeeRestServiceBuilder;

@Templated
public class EmployeeEditForm implements IsElement {

    @Inject
    @DataField
    private EmployeeSubform employeeSubform;

    @Inject
    @DataField
    private Button saveButton;

    @Inject
    @DataField
    private Button cancelButton;

    @Inject
    @DataField
    private Button closeButton;

    @Inject
    @DataField
    private @Named(value = "h3") HeadingElement title;

    @Inject
    private TranslationService CONSTANTS;

    private Employee employee;
    private List<Skill> skillList;

    private FormPopup popup;

    ResolveCallbackFn<Void> resolve;
    RejectCallbackFn reject;

    public EmployeeEditForm withEmployee(Employee employee) {
        this.employee = new Employee(employee);
        return this;
    }

    public EmployeeEditForm withSkillList(List<Skill> skillList) {
        this.skillList = skillList;
        return this;
    }

    public Promise<Void> show() {
        employeeSubform.setSkillList(skillList);
        employeeSubform.setEmployeeModel(employee);

        title.setInnerSafeHtml(new SafeHtmlBuilder().appendEscaped(employee.getName())
                .toSafeHtml());
        popup = FormPopup.getFormPopup(this);
        popup.center();
        return new Promise<Void>((res, rej) -> {
            resolve = res;
            reject = rej;
        });
    }

    @EventHandler("cancelButton")
    public void cancel(ClickEvent e) {
        popup.hide();
        reject.onInvoke(null);
    }

    @EventHandler("closeButton")
    public void close(ClickEvent e) {
        popup.hide();
        reject.onInvoke(null);
    }

    @EventHandler("saveButton")
    public void save(ClickEvent click) {
        popup.hide();
        employeeSubform.getIfValid().then((myEmployee) -> {
            EmployeeRestServiceBuilder.updateEmployee(myEmployee.getTenantId(), myEmployee,
                    new FailureShownRestCallback<
                            Employee>() {

                        @Override
                        public void onSuccess(Employee employee) {
                            resolve.onInvoke(PromiseUtils.resolve());
                        }
                    });
            return PromiseUtils.resolve();
        }).catch_((e) -> {
            ErrorPopup.show(CommonUtils.delimitCollection((Set<ConstraintViolation<Employee>>) e, (error) -> error
                    .toString(), "\n"));
            return PromiseUtils.resolve();
        });
    }

}
