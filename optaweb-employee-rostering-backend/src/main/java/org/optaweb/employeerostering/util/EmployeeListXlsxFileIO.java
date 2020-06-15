/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.employee.CovidRiskType;
import org.optaweb.employeerostering.domain.employee.view.EmployeeView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.skill.view.SkillView;
import org.optaweb.employeerostering.service.contract.ContractService;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EmployeeListXlsxFileIO {

    private final SkillService skillService;

    private final ContractService contractService;
    
    @Autowired
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
                employee.setCovidRiskType(CovidRiskType.LOW);
                employee.setContract(defaultContract);
                out.add(employee);
            }
            return out;
        }
    }
}
