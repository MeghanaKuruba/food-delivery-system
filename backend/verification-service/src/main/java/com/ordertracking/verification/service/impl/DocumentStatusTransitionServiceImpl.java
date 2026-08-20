package com.ordertracking.verification.service.impl;

import com.ordertracking.verification.enums.DocumentStatus;
import com.ordertracking.verification.exception.InvalidDocumentStatusTransitionException;
import com.ordertracking.verification.service.DocumentStatusTransitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DocumentStatusTransitionServiceImpl
        implements DocumentStatusTransitionService {

    @Override
    public void validateTransition(DocumentStatus currentStatus, DocumentStatus newStatus) {

        if (currentStatus == null || newStatus == null) {

            throw new InvalidDocumentStatusTransitionException("Document status cannot be null.");
        }

        /*
         * Setting the same status again is harmless.
         */
        if (currentStatus == newStatus) {
            return;
        }

        boolean allowed = switch (currentStatus) {

            /*
             * Newly uploaded documents must enter
             * the verification workflow.
             */
            case UPLOADED ->
                    newStatus == DocumentStatus.UNDER_REVIEW;

            /*
             * Automated/manual verification can produce
             * any of the supported verification outcomes.
             */
            case UNDER_REVIEW ->
                    newStatus == DocumentStatus.VERIFIED
                            || newStatus == DocumentStatus.EXPIRED
                            || newStatus == DocumentStatus.INVALID
                            || newStatus == DocumentStatus.MISMATCH
                            || newStatus == DocumentStatus.REJECTED
                            || newStatus == DocumentStatus.REUPLOAD_REQUIRED
                            || newStatus == DocumentStatus.MANUAL_REVIEW;

            /*
             * These outcomes require the applicant to
             * submit a corrected/replacement document.
             */
            case EXPIRED,
                 INVALID,
                 MISMATCH,
                 REUPLOAD_REQUIRED ->
                    newStatus == DocumentStatus.UNDER_REVIEW;

            /*
             * Manual review is resolved by the verification
             * workflow/reviewer.
             */
            case MANUAL_REVIEW ->
                    newStatus == DocumentStatus.VERIFIED
                            || newStatus == DocumentStatus.REUPLOAD_REQUIRED
                            || newStatus == DocumentStatus.REJECTED;

            /*
             * A verified document should not be silently
             * moved to another state.
             */
            case VERIFIED ->
                    false;

            /*
             * Rejected is treated as a terminal decision
             * for the current document submission.
             *
             * A new submission/re-upload should create the
             * appropriate workflow according to our business rules.
             */
            case REJECTED ->
                    false;
        };

        if (!allowed) {

            log.warn(
                    "Invalid verification document status transition. currentStatus={}, requestedStatus={}",
                    currentStatus,
                    newStatus
            );

            throw new InvalidDocumentStatusTransitionException(
                    "Invalid document status transition from "
                            + currentStatus
                            + " to "
                            + newStatus
            );
        }
    }
}