package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.dto.PanDataDetailsRequest;
import com.ordertracking.verification.dto.PanDataDetailsResponse;
import com.ordertracking.verification.entity.PanData;
import com.ordertracking.verification.entity.VerificationDocument;
import com.ordertracking.verification.enums.DocumentType;
import com.ordertracking.verification.repository.PanDataRepository;
import com.ordertracking.verification.repository.VerificationDocumentRepository;
import com.ordertracking.verification.service.VerificationDocumentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationDocumentDataServiceImpl implements VerificationDocumentDataService {

    private final VerificationDocumentRepository documentRepository;
    private final PanDataRepository panDataRepository;

    @Override
    @Transactional
    public PanDataDetailsResponse addPanData(
            String documentId,
            PanDataDetailsRequest request) {

        log.info(
                "Adding PAN data. documentId={}",
                documentId
        );

        VerificationDocument document =
                documentRepository.findById(documentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "Verification document not found."
                                )
                        );

        if (document.getDocumentType() != DocumentType.PAN) {
            throw new IllegalStateException(
                    "Document is not a PAN document."
            );
        }

        if (panDataRepository.existsById(documentId)) {
            throw new IllegalStateException(
                    "PAN data already exists for this document."
            );
        }

        String panNumber =
                request.getPanNumber()
                        .trim()
                        .toUpperCase();

        if (panDataRepository.existsByPanNumberIgnoreCase(
                panNumber)) {

            log.warn(
                    "PAN data already exists. documentId={}",
                    documentId
            );

            throw new IllegalStateException(
                    "PAN number already exists in verification records."
            );
        }

        PanData panData =
                PanData.builder()
                        .documentId(documentId)
                        .verificationDocument(document)
                        .panNumber(panNumber)
                        .holderName(request.getHolderName().trim())
                        .build();

        PanData saved =
                panDataRepository.save(panData);

        log.info(
                "PAN data saved successfully. documentId={}",
                documentId
        );

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PanDataDetailsResponse getPanData(
            String documentId) {

        PanData panData =
                panDataRepository.findById(documentId)
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "PAN data not found."
                                )
                        );

        return toResponse(panData);
    }

    private PanDataDetailsResponse toResponse(
            PanData panData) {

        return PanDataDetailsResponse.builder()
                .documentId(panData.getDocumentId())
                .panNumber(panData.getPanNumber())
                .holderName(panData.getHolderName())
                .build();
    }
}