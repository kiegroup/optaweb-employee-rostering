package org.optaplanner.openshift.employeerostering.shared.file;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.optaplanner.openshift.employeerostering.shared.common.AbstractPersistable;

@Entity
@NamedQueries({
        @NamedQuery(name = "TextFile.findAll", query = "select distinct f from TextFile f" +
                " where f.tenantId = :tenantId and f.index = 0" +
                " order by f.fileName"),
        @NamedQuery(name = "TextFile.fetchFile", query = "select distinct f from TextFile f"
                + " where f.tenantId = :tenantId and f.fileName = :fileName" + " order by f.index")
})
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tenantId", "fileName", "index"}))
public class TextFile extends AbstractPersistable {

    public static final int BLOCK_SIZE = 4096;

    @NotNull
    @Size(min = 1, max = 256)
    private String fileName;

    @NotNull
    private Integer index;

    @NotNull
    @Size(min = 1, max = BLOCK_SIZE)
    private String data;

    @SuppressWarnings("unused")
    public TextFile() {

    }

    public TextFile(Integer tenantId, String fileName, Integer index, String data) {
        this.tenantId = tenantId;
        this.fileName = fileName;
        this.index = index;
        this.data = data;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
