package org.optaplanner.openshift.employeerostering.gwtui.client.common;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import elemental2.dom.HTMLTableRowElement;
import org.gwtbootstrap3.client.ui.Anchor;
import org.jboss.errai.databinding.client.api.DataBinder;
import org.jboss.errai.databinding.client.api.StateSync;
import org.jboss.errai.databinding.client.components.ListComponent;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.optaplanner.openshift.employeerostering.shared.employee.Employee;

public abstract class TableRow<T> extends Composite implements TakesValue<T> {

    @Inject
    protected DataBinder<T> dataBinder;

    @Inject
    @DataField
    private Anchor deleteCell;
    @Inject
    @DataField
    private Anchor editCell;

    @Inject
    @DataField
    private Button commitChanges;
    @Inject
    @DataField
    private Button cancelChanges;

    @Inject
    @DataField
    private HTMLTableRowElement editor;
    @Inject
    @DataField
    private HTMLTableRowElement presenter;

    // Not Null if and only if this row is a row for creating an instance
    private KiePager<T> pager;
    // Not Null if and only if this row is a row for creating an instance
    private ListComponent<T, ?> table;

    @PostConstruct
    private void init() {
        setEditing(false);
    }

    @EventHandler("editCell")
    public void onEditClick(ClickEvent e) {
        setEditing(true);
    }

    @EventHandler("deleteCell")
    public void onDeleteClick(ClickEvent e) {
        deleteRow();
    }

    @EventHandler("cancelChanges")
    public void onCancelClick(ClickEvent e) {
        setEditing(false);
        if (isCreatingRow()) {
            table.getValue().remove(getOldValue());
            pager.refresh();
        }
    }

    @EventHandler("commitChanges")
    public void onSaveClick(ClickEvent e) {
        if (isCreatingRow()) {
            createRow();
        } else {
            updateRow();
        }
    }

    public void setEditing(boolean isEditing) {
        if (isEditing) {
            dataBinder.pause();
        } else {
            dataBinder.setModel(getOldValue(), StateSync.FROM_MODEL, true);
        }
        presenter.hidden = isEditing;
        editor.hidden = !isEditing;
    }

    public void markAsCreator(ListComponent<T, ?> table, KiePager<T> pager) {
        this.table = table;
        this.pager = pager;
    }

    public T getOldValue() {
        return dataBinder.getModel();
    }

    public T getNewValue() {
        return dataBinder.getWorkingModel();
    }

    @Override
    public void setValue(T value) {
        dataBinder.setModel(value, StateSync.FROM_MODEL, true);
    }

    public void commitChanges() {
        setValue(getNewValue());
    }

    @Override
    public T getValue() {
        return dataBinder.getModel();
    }

    public static <T> void createNewRow(T value, ListComponent<T, ? extends TableRow<T>> table, KiePager<T> pager) {
        if (!table.getComponent(0).isCreatingRow()) {
            pager.getPager().firstPage();
            table.getValue().add(0, value);
            pager.refresh();
            table.getComponent(0).setEditing(true);
            table.getComponent(0).markAsCreator(table, pager);
        }
    }

    protected boolean isCreatingRow() {
        return null != table;
    }

    protected abstract void deleteRow();

    protected abstract void updateRow();

    protected abstract void createRow();
}
