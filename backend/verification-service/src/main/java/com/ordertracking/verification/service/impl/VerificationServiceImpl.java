package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.CreateVerificationApplicationRequest;
import com.ordertracking.verification.dto.VerificationApplicationResponse;
import com.ordertracking.verification.entity.VerificationApplication;
import com.ordertracking.verification.enums.VerificationStatus;
import com.ordertracking.verification.exception.VerificationApplicationNotFoundException;
import com.ordertracking.verification.mapper.VerificationMapper;
import com.ordertracking.verification.repository.VerificationApplicationRepository;
import com.ordertracking.verification.service.VerificationApplicationValidationService;
import com.ordertracking.verification.service.VerificationDocumentVerificationService;
import com.ordertracking.verification.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationServiceImpl implements VerificationService {

    private final VerificationApplicationRepository applicationRepository;

    private final VerificationMapper verificationMapper;

    private final VerificationApplicationValidationService verificationApplicationValidationService;

    private final VerificationDocumentVerificationService verificationDocumentVerificationService;

    @Override
    public VerificationApplicationResponse createApplication(CreateVerificationApplicationRequest request) {

        log.info(
                "Creating verification application. authUserId={}, applicantType={}",
                request.getAuthUserId(),
                request.getApplicantType()
        );

        VerificationApplication application =
                VerificationApplication.builder()
                        .authUserId(request.getAuthUserId())
                        .applicantType(request.getApplicantType())
                        .referenceId(verificationMapper.generateReferenceId())
                        .build();

        VerificationApplication saved = applicationRepository.save(application);

        log.info(
                "Verification application created. id={}, authUserId={}, type={}, referenceId={}",
                saved.getId(),
                saved.getAuthUserId(),
                saved.getApplicantType(),
                saved.getReferenceId()
        );

        return verificationMapper.toVerificationAppResponse(saved);
    }

    @Override
    public VerificationApplicationResponse getApplication(Long applicationId) {

        log.debug(
                "Fetching verification application. applicationId={}",
                applicationId
        );

        VerificationApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Verification application not found. applicationId={}",
                                    applicationId
                            );

                            return new VerificationApplicationNotFoundException(
                                    "Verification application not found."
                            );
                        });

        return verificationMapper.toVerificationAppResponse(application);
    }

    @Override
    public List<VerificationApplicationResponse> getApplicationsByUser(Long authUserId) {

        log.debug(
                "Fetching verification applications. authUserId={}",
                authUserId
        );

        List<VerificationApplication> applications =
                applicationRepository.findByAuthUserId(authUserId);

        log.info(
                "Verification applications fetched. authUserId={}, count={}",
                authUserId,
                applications.size()
        );

        return applications
                .stream()
                .map(verificationMapper::toVerificationAppResponse)
                .toList();
    }

    @Override
    @Transactional
    public VerificationApplicationResponse submitApplication(Long applicationId) {

        log.info(
                "Submitting verification application. applicationId={}",
                applicationId
        );

        VerificationApplication application =
                applicationRepository.findById(applicationId)
                        .orElseThrow(() -> {

                            log.warn(
                                    "Verification application not found for submission. applicationId={}",
                                    applicationId
                            );

                            return new VerificationApplicationNotFoundException(
                                    "Verification application not found."
                            );
                        });

        verificationApplicationValidationService.validateApplicationCanBeSubmitted(application);

        verificationApplicationValidationService.validateRequiredDocuments(application);

        application.setStatus(VerificationStatus.UNDER_REVIEW);

        VerificationApplication saved = applicationRepository.save(application);

        log.info(
                "Verification application submitted successfully. applicationId={}, status={}",
                saved.getId(),
                saved.getStatus()
        );

        verificationDocumentVerificationService.verifyDocuments(saved.getId());

        return verificationMapper.toVerificationAppResponse(saved);
    }
}