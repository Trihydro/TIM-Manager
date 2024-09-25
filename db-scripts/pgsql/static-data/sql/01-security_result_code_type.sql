
BEGIN;
ALTER TABLE security_result_code_type DISABLE TRIGGER USER;

SET client_encoding TO 'UTF8';
SET synchronous_commit TO off;

INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'success',1);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'unknown',2);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'inconsistentInputParameters',9);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduParsingInvalidInput',10);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduParsingUnsupportedCriticalInformationField',11);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduParsingCertificateNotFound',12);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduParsingGenerationTimeNotAvailable',13);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduParsingGenerationLocationNotAvailable',14);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainNotEnoughInformationToConstructChain',15);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainChainEndedAtUntrustedRoot',16);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainChainWasTooLongForImplementation',17);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainCertificateRevoked',18);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainOverdueCRL',19);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainInconsistentExpiryTimes',20);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainInconsistentStartTimes',21);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateChainInconsistentChainPermissions',22);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCryptoVerificationFailure',23);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyFutureCertificateAtGenerationTime',24);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyExpiredCertificateAtGenerationTime',25);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyExpiryDateTooEarly',26);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyExpiryDateTooLate',27);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyGenerationLocationOutsideValidityRegion',28);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyNoGenerationLocation',29);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduConsistencyUnauthorizedPSID',30);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduInternalConsistencyExpiryTimeBeforeGenerationTime',31);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduInternalConsistencyextDataHashDoesntMatch',32);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduInternalConsistencynoExtDataHashProvided',33);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduInternalConsistencynoExtDataHashPresent',34);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduLocalConsistencyPSIDsDontMatch',35);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduLocalConsistencyChainWasTooLongForSDEE',36);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduRelevanceGenerationTimeTooFarInPast',37);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduRelevanceGenerationTimeTooFarInFuture',38);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduRelevanceExpiryTimeInPast',39);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduRelevanceGenerationLocationTooDistant',40);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduRelevanceReplayedSpdu',41);
INSERT INTO security_result_code_type (security_result_code_type,security_result_code_type_id) VALUES (E'spduCertificateExpired',42);

COMMIT;

