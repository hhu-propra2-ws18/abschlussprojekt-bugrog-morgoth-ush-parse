package de.hhu.propra.sharingplatform.service;

import de.hhu.propra.sharingplatform.dao.ConflictRepo;
import de.hhu.propra.sharingplatform.dao.ContractRepo;
import de.hhu.propra.sharingplatform.dto.Status;
import de.hhu.propra.sharingplatform.model.Conflict;
import de.hhu.propra.sharingplatform.model.Contract;
import de.hhu.propra.sharingplatform.model.Offer;
import de.hhu.propra.sharingplatform.service.payment.IPaymentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;

@Service
@Data
public class ContractService {

    final ContractRepo contractRepo;

    final IPaymentService paymentService;

    final ConflictRepo conflictRepo;

    @Autowired
    public ContractService(ContractRepo contractRepo, IPaymentService paymentService,
                           ConflictRepo conflictRepo) {
        this.contractRepo = contractRepo;
        this.paymentService = paymentService;
        this.conflictRepo = conflictRepo;
    }

    public void create(Offer offer) {
        Contract contract = new Contract(offer);
        // -> payment
        contract.setPayment(paymentService.createPayment(contract));
        contractRepo.save(contract);
    }

    public void returnItem(long contractId, String accountName) {
        Contract contract = contractRepo.findOneById(contractId);
        if (!userIsBorrower(contract, accountName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This contract does not involve you");
        }
        LocalDateTime current = LocalDateTime.now();
        contract.setRealEnd(current);
        paymentService.transferPayment(contract);
        contractRepo.save(contract);
    }

    public void acceptReturn(long contractId, String accountName) {
        Contract contract = contractRepo.findOneById(contractId);
        if (!userIsContractOwner(contract, accountName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This contract does not involve you");
        }
        paymentService.freeBailReservation(contract);
        contract.setFinished(true);
        contractRepo.save(contract);
    }

    public void punishBail(long contractId) {
        Contract contract = contractRepo.findOneById(contractId);
        paymentService.punishBailReservation(contract);
    }

    public void openConflict(long contractId, String accountName) {
        Contract contract = contractRepo.findOneById(contractId);
        if (!userIsContractOwner(contract, accountName)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "This contract does not involve you");
        }
        contract.setRealEnd(LocalDateTime.now());
        Conflict conflict = new Conflict();
        conflict.setStatus(Status.PENDING);
        conflictRepo.save(conflict);
        contract.setConflict(conflict);
        contractRepo.save(contract);
    }

    public void calcPrice(long contractId) {
        Contract contract = contractRepo.findOneById(contractId);
        paymentService.createPayment(contract);
    }

    private boolean userIsBorrower(Contract contract, String accountName) {
        return contract.getBorrower().getAccountName().equals(accountName);
    }

    public boolean userIsContractOwner(Contract contract, String userName) {
        return contract.getItem().getOwner().getAccountName().equals(userName);
    }

    public Collection<Contract> getContractsWithOpenConflicts() {
        Collection<Conflict> conflictsPending = conflictRepo.findAllByStatus(Status.PENDING);
        ArrayList<Contract> contracts = new ArrayList<>();
        for (Conflict conflict : conflictsPending) {
            contracts.add(conflict.getContract());
        }
        return contracts;
    }

    public Collection<Conflict> getOpenConflicts() {
        return conflictRepo.findAllByStatus(Status.PENDING);
    }

    /**
     * Resolves the conflict and gives the bail to borrower or item owner.
     *
     * @param accepted if accepted: item owner gets bail,
     *                 otherwise the conflict is rejected and borrower keeps bail.
     */
    public void resolveOwnerConflict(boolean accepted, long conflictId) {
        Conflict conflict = conflictRepo.findOneById(conflictId);
        Contract contract = conflict.getContract();
        contract.setFinished(true);
        if (accepted) {
            conflict.setStatus(Status.ACCEPTED);
            punishBail(conflictId);
        } else {
            conflict.setStatus(Status.REJECTED);
            paymentService.freeBailReservation(contract);
        }
        conflictRepo.save(conflict);
        contractRepo.save(contract);
    }
}
