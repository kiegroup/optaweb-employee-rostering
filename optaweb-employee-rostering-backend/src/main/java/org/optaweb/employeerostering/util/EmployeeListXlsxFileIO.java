package org.optaweb.employeerostering.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.optaweb.employeerostering.domain.common.HighContrastColor;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.contract.ContractService;
import org.optaweb.employeerostering.service.skill.SkillService;

@ApplicationScoped
public class EmployeeListXlsxFileIO {

    private final SkillService skillService;

    private final ContractService contractService;

    @Inject
    public EmployeeListXlsxFileIO(SkillService skillService, ContractService contractService) {
        this.skillService = skillService;
        this.contractService = contractService;
    }

    public List<EmployeeView> getEmployeeListFromExcelFile(Integer tenantId,
            InputStream excelFileStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(excelFileStream)) {
            Sheet worksheet = workbook.getSheetAt(0);
            List<EmployeeView> out = new ArrayList<>(worksheet.getPhysicalNumberOfRows() - 1);
            Map<String, Skill> skillMap = skillService.getSkillList(tenantId).stream()
                    .collect(Collectors.toMap(s -> s.getName().toLowerCase(), Function.identity()));
            Contract defaultContract = contractService.getOrCreateDefaultContract(tenantId);
            for (int i = 1; i <= worksheet.getLastRowNum(); i++) {
                Row row = worksheet.getRow(i);

                if (row == null || row.getCell(0) == null) {
                    continue;
                }

                EmployeeView employee = new EmployeeView();
                employee.setTenantId(tenantId);
                employee.setName(row.getCell(0).getStringCellValue());
                String skillListString = (row.getCell(1) != null) ? row.getCell(1).getStringCellValue() : "";
                employee.setSkillProficiencySet(
                        Arrays.stream(skillListString.split(","))
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(skillName -> skillMap.computeIfAbsent(skillName.toLowerCase(), lowercaseName -> {
                                    SkillView skillView = new SkillView();
                                    skillView.setTenantId(tenantId);
                                    skillView.setName(skillName);
                                    return skillService.createSkill(tenantId, skillView);
                                }))
                                .collect(Collectors.toCollection(HashSet::new)));
                employee.setContract(defaultContract);
                employee.setShortId(Employee.generateShortIdFromName(employee.getName()));
                employee.setColor(HighContrastColor.generateColorFromHashcode(employee.getName()));
                out.add(employee);
            }
            return out;
        }
    }
}
