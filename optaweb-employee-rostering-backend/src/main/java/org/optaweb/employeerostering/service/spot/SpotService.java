package org.optaweb.employeerostering.service.spot;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Validator;

import org.optaweb.employeerostering.domain.spot.Spot;
import org.optaweb.employeerostering.domain.spot.view.SpotView;
import org.optaweb.employeerostering.service.common.AbstractRestService;

@ApplicationScoped
public class SpotService extends AbstractRestService {

    SpotRepository spotRepository;

    @Inject
    public SpotService(Validator validator, SpotRepository spotRepository) {
        super(validator);
        this.spotRepository = spotRepository;
    }

    public Spot convertFromView(Integer tenantId, SpotView spotView) {
        Spot spot = new Spot(spotView.getTenantId(), spotView.getName(), spotView.getRequiredSkillSet());
        spot.setId(spotView.getId());
        spot.setVersion(spotView.getVersion());
        validateBean(tenantId, spot);

        return spot;
    }

    @Transactional
    public List<Spot> getSpotList(Integer tenantId) {
        return spotRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Spot getSpot(Integer tenantId, Long id) {
        Spot spot = spotRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("No Spot entity found with ID (" + id + ")."));

        validateBean(tenantId, spot);
        return spot;
    }

    @Transactional
    public Boolean deleteSpot(Integer tenantId, Long id) {
        Optional<Spot> spotOptional = spotRepository.findByIdOptional(id);

        if (!spotOptional.isPresent()) {
            return false;
        }

        validateBean(tenantId, spotOptional.get());
        spotRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Spot createSpot(Integer tenantId, SpotView spotView) {
        Spot spot = convertFromView(tenantId, spotView);
        spotRepository.persist(spot);
        return spot;
    }

    @Transactional
    public Spot updateSpot(Integer tenantId, SpotView spotView) {
        Spot newSpot = convertFromView(tenantId, spotView);
        Spot oldSpot = spotRepository
                .findByIdOptional(newSpot.getId())
                .orElseThrow(() -> new EntityNotFoundException("Spot entity with ID (" + newSpot.getId() +
                        ") not found."));

        if (!oldSpot.getTenantId().equals(newSpot.getTenantId())) {
            throw new IllegalStateException("Spot entity with tenantId (" + oldSpot.getTenantId()
                    + ") cannot change tenants.");
        }

        oldSpot.setName(newSpot.getName());
        oldSpot.setRequiredSkillSet(newSpot.getRequiredSkillSet());
        spotRepository.persist(oldSpot);
        return oldSpot;
    }
}
