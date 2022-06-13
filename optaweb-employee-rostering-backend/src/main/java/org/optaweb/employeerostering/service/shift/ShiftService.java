package org.optaweb.employeerostering.service.shift;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Validator;

import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.score.constraint.Indictment;
import org.optaweb.employeerostering.domain.employee.Employee;
import org.optaweb.employeerostering.domain.shift.Shift;
import org.optaweb.employeerostering.domain.shift.view.ShiftView;
import org.optaweb.employeerostering.domain.skill.Skill;
import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.tenant.RosterConstraintConfiguration;
import org.optaweb.employeerostering.service.common.AbstractRestService;
import org.optaweb.employeerostering.service.common.IndictmentUtils;
import org.optaweb.employeerostering.service.employee.EmployeeRepository;
import org.optaweb.employeerostering.service.roster.RosterService;
import org.optaweb.employeerostering.service.skill.SkillService;
import org.optaweb.employeerostering.service.spot.SpotRepository;
import org.optaweb.employeerostering.service.tenant.RosterConstraintConfigurationRepository;

@ApplicationScoped
public class ShiftService extends AbstractRestService {

    ShiftRepository shiftRepository;

    SpotRepository spotRepository;

    SkillService skillService;

    EmployeeRepository employeeRepository;

    RosterService rosterService;

    RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository;

    IndictmentUtils indictmentUtils;

    @Inject
    public ShiftService(Validator validator,
            ShiftRepository shiftRepository, SpotRepository spotRepository,
            SkillService skillService, EmployeeRepository employeeRepository,
            RosterService rosterService,
            RosterConstraintConfigurationRepository rosterConstraintConfigurationRepository,
            IndictmentUtils indictmentUtils) {
        super(validator);
        this.shiftRepository = shiftRepository;
        this.spotRepository = spotRepository;
        this.skillService = skillService;
        this.employeeRepository = employeeRepository;
        this.rosterService = rosterService;
        this.rosterConstraintConfigurationRepository = rosterConstraintConfigurationRepository;
        this.indictmentUtils = indictmentUtils;
    }

    public List<ShiftView> getShiftList(Integer tenantId) {
        Map<Object, Indictment<HardMediumSoftLongScore>> indictmentMap = indictmentUtils.getIndictmentMapForRoster(
                rosterService.buildRoster(tenantId));
        RosterConstraintConfiguration configuration = rosterConstraintConfigurationRepository.findByTenantId(tenantId).get();
        return getAllShifts(tenantId).stream()
                .map(s -> indictmentUtils.getShiftViewWithIndictment(
                        rosterService.getRosterState(tenantId).getTimeZone(), s, configuration, indictmentMap.get(s),
                        (s.getEmployee() != null) ? indictmentMap.get(s.getEmployee()) : null))
                .collect(Collectors.toList());
    }

    private List<Shift> getAllShifts(Integer tenantId) {
        return shiftRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public ShiftView getShift(Integer tenantId, Long id) {
        Shift shift = shiftRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("No Shift entity found with ID (" + id + ")."));

        validateBean(tenantId, shift);
        Map<Object, Indictment<HardMediumSoftLongScore>> indictmentMap = indictmentUtils.getIndictmentMapForRoster(
                rosterService.buildRoster(tenantId));
        RosterConstraintConfiguration configuration = rosterConstraintConfigurationRepository.findByTenantId(tenantId).get();
        Indictment<HardMediumSoftLongScore> shiftIndictment = indictmentMap.get(shift);
        Indictment<HardMediumSoftLongScore> employeeIndictment =
                (shift.getEmployee() != null) ? indictmentMap.get(shift.getEmployee()) : null;

        return indictmentUtils.getShiftViewWithIndictment(rosterService.getRosterState(tenantId).getTimeZone(), shift,
                configuration, shiftIndictment, employeeIndictment);
    }

    private Shift convertFromView(Integer tenantId, ShiftView shiftView) {
        validateBean(tenantId, shiftView);

        Spot spot = spotRepository
                .findByIdOptional(shiftView.getSpotId())
                .orElseThrow(() -> new EntityNotFoundException("No Spot entity found with ID (" + shiftView.getSpotId()
                        + ")."));

        validateBean(tenantId, spot);

        Long rotationEmployeeId = shiftView.getRotationEmployeeId();
        Employee rotationEmployee = null;
        if (rotationEmployeeId != null) {
            rotationEmployee = employeeRepository
                    .findByIdOptional(rotationEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException("ShiftView (" + shiftView +
                            ") has an non-existing " +
                            "rotationEmployeeId (" +
                            rotationEmployeeId + ")."));
            validateBean(tenantId, rotationEmployee);
        }

        Long originalEmployeeId = shiftView.getOriginalEmployeeId();
        Employee originalEmployee = null;
        if (originalEmployeeId != null) {
            originalEmployee = employeeRepository
                    .findByIdOptional(originalEmployeeId)
                    .orElseThrow(() -> new EntityNotFoundException("ShiftView (" + shiftView +
                            ") has an non-existing " +
                            "originalEmployeeId (" +
                            originalEmployeeId + ")."));
            validateBean(tenantId, originalEmployee);
        }

        Set<Skill> requiredSkillSet = shiftView.getRequiredSkillSetIdList()
                .stream().map(id -> skillService.getSkill(tenantId, id))
                .collect(Collectors.toCollection(HashSet::new));

        Shift shift = new Shift(rosterService.getRosterState(tenantId).getTimeZone(), shiftView, spot,
                rotationEmployee, requiredSkillSet, originalEmployee);
        shift.setPinnedByUser(shiftView.isPinnedByUser());

        Long employeeId = shiftView.getEmployeeId();
        if (employeeId != null) {
            Employee employee = employeeRepository
                    .findByIdOptional(employeeId)
                    .orElseThrow(() -> new EntityNotFoundException("ShiftView (" + shiftView +
                            ") has an non-existing employeeId (" +
                            employeeId + ")."));
            validateBean(tenantId, employee);
            shift.setEmployee(employee);
        }

        validateBean(tenantId, shift);
        return shift;
    }

    @Transactional
    public ShiftView createShift(Integer tenantId, ShiftView shiftView) {
        Shift shift = convertFromView(tenantId, shiftView);
        shiftRepository.persist(shift);

        Map<Object, Indictment<HardMediumSoftLongScore>> indictmentMap = indictmentUtils.getIndictmentMapForRoster(
                rosterService.buildRoster(tenantId));
        RosterConstraintConfiguration configuration = rosterConstraintConfigurationRepository.findByTenantId(tenantId).get();
        Indictment<HardMediumSoftLongScore> shiftIndictment = indictmentMap.get(shift);
        Indictment<HardMediumSoftLongScore> employeeIndictment =
                (shift.getEmployee() != null) ? indictmentMap.get(shift.getEmployee()) : null;
        return indictmentUtils.getShiftViewWithIndictment(rosterService.getRosterState(tenantId).getTimeZone(),
                shift, configuration, shiftIndictment, employeeIndictment);
    }

    @Transactional
    public ShiftView updateShift(Integer tenantId, ShiftView shiftView) {
        Shift newShift = convertFromView(tenantId, shiftView);
        Shift oldShift = shiftRepository
                .findByIdOptional(newShift.getId())
                .orElseThrow(() -> new EntityNotFoundException("Shift entity with ID (" + newShift.getId() + ") not " +
                        "found."));

        if (!oldShift.getTenantId().equals(newShift.getTenantId())) {
            throw new IllegalStateException("Shift entity with tenantId (" + oldShift.getTenantId()
                    + ") cannot change tenants.");
        }

        oldShift.setRotationEmployee(newShift.getRotationEmployee());
        oldShift.setOriginalEmployee(newShift.getOriginalEmployee());
        oldShift.setSpot(newShift.getSpot());
        oldShift.setStartDateTime(newShift.getStartDateTime());
        oldShift.setEndDateTime(newShift.getEndDateTime());
        oldShift.setPinnedByUser(newShift.isPinnedByUser());
        oldShift.setEmployee(newShift.getEmployee());
        oldShift.setRequiredSkillSet(newShift.getRequiredSkillSet());

        // Flush to increase version number before we duplicate it to ShiftView
        shiftRepository.persistAndFlush(oldShift);

        Map<Object, Indictment<HardMediumSoftLongScore>> indictmentMap = indictmentUtils.getIndictmentMapForRoster(
                rosterService.buildRoster(tenantId));
        RosterConstraintConfiguration configuration = rosterConstraintConfigurationRepository.findByTenantId(tenantId).get();
        Indictment<HardMediumSoftLongScore> shiftIndictment = indictmentMap.get(newShift);
        Indictment<HardMediumSoftLongScore> employeeIndictment =
                (newShift.getEmployee() != null) ? indictmentMap.get(newShift.getEmployee()) : null;
        return indictmentUtils.getShiftViewWithIndictment(rosterService.getRosterState(tenantId).getTimeZone(),
                oldShift, configuration, shiftIndictment, employeeIndictment);
    }

    @Transactional
    public Boolean deleteShift(Integer tenantId, Long id) {
        Optional<Shift> shiftOptional = shiftRepository.findByIdOptional(id);
        if (!shiftOptional.isPresent()) {
            return false;
        }
        validateBean(tenantId, shiftOptional.get());
        shiftRepository.deleteById(id);
        return true;
    }
}
