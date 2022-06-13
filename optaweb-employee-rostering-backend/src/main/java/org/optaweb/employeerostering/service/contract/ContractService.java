package org.optaweb.employeerostering.service.contract;

import java.util.List;
import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import javax.validation.Validator;

import org.optaweb.employeerostering.domain.contract.Contract;
import org.optaweb.employeerostering.domain.contract.view.ContractView;
import org.optaweb.employeerostering.service.common.AbstractRestService;

@ApplicationScoped
public class ContractService extends AbstractRestService {

    final private ContractRepository contractRepository;

    @Inject
    public ContractService(Validator validator,
            ContractRepository contractRepository) {
        super(validator);
        this.contractRepository = contractRepository;
    }

    public Contract convertFromView(Integer tenantId, ContractView contractView) {
        validateBean(tenantId, contractView);
        Contract contract = new Contract(tenantId, contractView.getName(),
                contractView.getMaximumMinutesPerDay(),
                contractView.getMaximumMinutesPerWeek(),
                contractView.getMaximumMinutesPerMonth(),
                contractView.getMaximumMinutesPerYear());
        contract.setId(contractView.getId());
        contract.setVersion(contractView.getVersion());
        return contract;
    }

    public List<Contract> getContractList(Integer tenantId) {
        return contractRepository.findAllByTenantId(tenantId);
    }

    @Transactional
    public Contract getContract(Integer tenantId, Long id) {
        Contract contract = contractRepository
                .findByIdOptional(id)
                .orElseThrow(() -> new EntityNotFoundException("No Contract entity found with ID (" + id + ")."));

        validateBean(tenantId, contract);
        return contract;
    }

    @Transactional
    public Boolean deleteContract(Integer tenantId, Long id) {
        Optional<Contract> contractOptional = contractRepository.findByIdOptional(id);

        if (!contractOptional.isPresent()) {
            return false;
        }

        validateBean(tenantId, contractOptional.get());
        contractRepository.deleteById(id);
        return true;
    }

    @Transactional
    public Contract createContract(Integer tenantId, ContractView contractView) {
        Contract contract = convertFromView(tenantId, contractView);
        contractRepository.persist(contract);
        return contract;
    }

    @Transactional
    public Contract updateContract(Integer tenantId, ContractView contractView) {
        Contract newContract = convertFromView(tenantId, contractView);

        Contract oldContract = contractRepository
                .findByIdOptional(newContract.getId())
                .orElseThrow(() -> new EntityNotFoundException("Contract entity with ID (" + newContract.getId() +
                        ") not found."));

        if (!oldContract.getTenantId().equals(newContract.getTenantId())) {
            throw new IllegalStateException("Contract entity with tenantId (" + oldContract.getTenantId()
                    + ") cannot change tenants.");
        }

        oldContract.setName(newContract.getName());
        oldContract.setMaximumMinutesPerDay(newContract.getMaximumMinutesPerDay());
        oldContract.setMaximumMinutesPerWeek(newContract.getMaximumMinutesPerWeek());
        oldContract.setMaximumMinutesPerMonth(newContract.getMaximumMinutesPerMonth());
        oldContract.setMaximumMinutesPerYear(newContract.getMaximumMinutesPerYear());
        contractRepository.persist(oldContract);
        return oldContract;
    }

    @Transactional
    public Contract getOrCreateDefaultContract(Integer tenantId) {
        Optional<Contract> defaultContract = contractRepository.findAllByTenantId(tenantId)
                .stream().filter(contract -> contract.getName().equals("Default Contract"))
                .findAny();
        if (defaultContract.isPresent()) {
            return defaultContract.get();
        } else {
            Contract contract = new Contract();
            contract.setName("Default Contract");
            contract.setTenantId(tenantId);
            contractRepository.persist(contract);
            return contract;
        }
    }
}
